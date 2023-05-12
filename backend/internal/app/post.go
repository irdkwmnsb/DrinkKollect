package app

import (
	"context"
	"time"

	"github.com/irdkwmnsb/DrinkKollect/backend/internal/auth"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/db"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/pagination"
	desc "github.com/irdkwmnsb/DrinkKollect/backend/internal/pb/v1/drinkollect"
	"github.com/samber/lo"
	"go.uber.org/zap"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/emptypb"
	"google.golang.org/protobuf/types/known/timestamppb"
)

var (
	errEmptyPostTitle = status.Error(codes.InvalidArgument, "Дайте какое-нибудь название вашему посту")
	errPostNotFound   = status.Error(codes.NotFound, "Указан ID несуществующего поста")
)

// CreatePost creates a new post by the authorized user.
func (s *Service) CreatePost(ctx context.Context, req *desc.CreatePostRequest) (*emptypb.Empty, error) {
	if req.Title == "" {
		return nil, errEmptyPostTitle
	}

	username := auth.UsernameFromCtx(ctx)

	post := db.Post{
		Title:           req.Title,
		Description:     req.Description,
		Location:        req.Location,
		ImageBucket:     req.Image.Bucket,
		ImageID:         req.Image.Id,
		CreatorUsername: username,
	}

	// Token might be used after deletion of the user, in which case we shouldn't do anything
	if err := s.storage.CreatePost(post); db.IsNotFound(err) && db.ErrDetails(err) == "user" {
		return &emptypb.Empty{}, nil
	} else if err != nil {
		zap.S().Errorw("creating new post", "post", post, "error", err)
		return nil, internalServerError
	}

	return &emptypb.Empty{}, nil
}

// TogglePostLike toggles a post's "liked" label by the authorized user.
func (s *Service) TogglePostLike(ctx context.Context, req *desc.TogglePostLikeRequest) (*emptypb.Empty, error) {
	username := auth.UsernameFromCtx(ctx)

	// Token might be used for deleted user, or an invalid post ID might be specified. The first case
	// isn't an error, while the second one is.
	if err := s.storage.ToggleLikedPost(username, req.Id); db.IsNotFound(err) {
		if db.ErrDetails(err) == "user" {
			return &emptypb.Empty{}, nil
		}

		zap.S().Errorw("invalid post ID liked", "username", username, "id", req.Id)
		return nil, errPostNotFound
	} else if err != nil {
		zap.S().Errorw("adding liked post", "username", username, "id", req.Id, "error", err)
		return nil, internalServerError
	}

	return &emptypb.Empty{}, nil
}

// ListPosts provides a paginated list of posts.
func (s *Service) ListPosts(ctx context.Context, req *desc.ListPostsRequest) (*desc.ListPostsResponse, error) {
	username := auth.UsernameFromCtx(ctx)
	limit, before := pagination.FromRequest[int64](req)

	var posts []db.Post
	var next int64
	var err error
	if before == 0 {
		posts, next, err = s.storage.ListPostsFirst("", limit, username)
	} else {
		posts, next, err = s.storage.ListPostsNext("", before, limit, username)
	}

	if err != nil {
		zap.S().Errorw("listing posts", "before", before, "limit", limit, "error", err)
		return nil, internalServerError
	}

	return &desc.ListPostsResponse{
		Posts:         dbPostsToPB(posts),
		NextPageToken: pagination.EncodePageToken(next),
	}, nil
}

// ListUserPosts provides a paginated list of a specific user's posts.
func (s *Service) ListUserPosts(ctx context.Context, req *desc.ListUserPostsRequest) (*desc.ListUserPostsResponse, error) {
	ourUsername := auth.UsernameFromCtx(ctx)
	limit, before := pagination.FromRequest[int64](req)

	var posts []db.Post
	var next int64
	var err error
	if before == 0 {
		posts, next, err = s.storage.ListPostsFirst(req.Username, limit, ourUsername)
	} else {
		posts, next, err = s.storage.ListPostsNext(req.Username, before, limit, ourUsername)
	}

	if err != nil {
		zap.S().Errorw("listing posts", "before", before, "limit", limit, "error", err)
		return nil, internalServerError
	}

	return &desc.ListUserPostsResponse{
		Posts:         dbPostsToPB(posts),
		NextPageToken: pagination.EncodePageToken(next),
	}, nil
}

func dbPostsToPB(posts []db.Post) []*desc.Post {
	const (
		millisecondsInSeconds    = int64(time.Second / time.Millisecond)
		nanosecondsInMillisecond = int64(time.Millisecond / time.Nanosecond)
	)

	return lo.Map(posts, func(post db.Post, _ int) *desc.Post {
		return &desc.Post{
			Id:          post.ID,
			Title:       post.Title,
			Description: post.Description,
			Location:    post.Location,
			Image: &desc.S3Resource{
				Bucket: post.ImageBucket,
				Id:     post.ImageID,
			},
			Likes:     post.Likes,
			Liked:     post.Liked,
			Creator:   post.CreatorUsername,
			Timestamp: timestamppb.New(time.Unix(post.CreatedAt/millisecondsInSeconds, post.CreatedAt%millisecondsInSeconds*nanosecondsInMillisecond)),
		}
	})
}
