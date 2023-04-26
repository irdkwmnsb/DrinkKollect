package app

import (
	"context"

	"github.com/irdkwmnsb/DrinkKollect/backend/internal/auth"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/db"
	desc "github.com/irdkwmnsb/DrinkKollect/backend/internal/pb/v1/drinkollect"
	"go.uber.org/zap"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/emptypb"
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
