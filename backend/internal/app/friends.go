package app

import (
	"context"

	"github.com/irdkwmnsb/DrinkKollect/backend/internal/auth"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/db"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/pagination"
	desc "github.com/irdkwmnsb/DrinkKollect/backend/internal/pb/v1/drinkollect"
	"github.com/samber/lo"
	"go.uber.org/zap"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/emptypb"
)

var (
	friendNotFoundError        = status.Error(codes.NotFound, "Пользователь не найден!")
	friendRequestNotFoundError = status.Error(codes.NotFound, "Запрос на дружбу не найден!")
	friendRequestAlreadyExists = status.Error(codes.AlreadyExists, "Вы уже отправили запрос на дружбу этому пользователю!")
)

func (s *Service) SendFriendRequest(ctx context.Context, req *desc.SendFriendRequestRequest) (*emptypb.Empty, error) {
	username := auth.UsernameFromCtx(ctx)
	zap.S().Infow("sending friend request", "username", username, "friend", req.Username)

	// Token might be used for a deleted user. In this case we shouldn't do anything.
	if err := s.storage.SendFriendRequest(username, req.Username); db.IsNotFound(err) {
		if db.ErrDetails(err) == "user_from" {
			return &emptypb.Empty{}, nil
		}
		zap.S().Errorw("sending friend request", "error", err)
		return nil, friendNotFoundError
	} else if db.IsAlreadyExists(err) {
		return nil, friendRequestAlreadyExists
	} else if err != nil {
		zap.S().Errorw("sending friend request", "error", err)
		return nil, internalServerError
	}

	return &emptypb.Empty{}, nil
}

func (s *Service) AcceptFriendRequest(ctx context.Context, req *desc.AcceptFriendRequestRequest) (*emptypb.Empty, error) {
	username := auth.UsernameFromCtx(ctx)

	// Token might be used for a deleted user. In this case we shouldn't do anything.
	if err := s.storage.AcceptFriendRequest(username, req.Username); db.IsNotFound(err) {
		if db.ErrDetails(err) == "user_from" {
			return &emptypb.Empty{}, nil
		}
		return nil, friendRequestNotFoundError
	} else if err != nil {
		zap.S().Errorw("accepting friend request", "error", err)
		return nil, internalServerError
	}

	return &emptypb.Empty{}, nil
}

func (s *Service) RejectFriendRequest(ctx context.Context, req *desc.RejectFriendRequestRequest) (*emptypb.Empty, error) {
	username := auth.UsernameFromCtx(ctx)

	// Token might be used for a deleted user. In this case we shouldn't do anything.
	if err := s.storage.RejectFriendRequest(username, req.Username); db.IsNotFound(err) {
		if db.ErrDetails(err) == "user_from" {
			return &emptypb.Empty{}, nil
		}
		return nil, friendRequestNotFoundError
	} else if err != nil {
		zap.S().Errorw("declining friend request", "error", err)
		return nil, internalServerError
	}

	return &emptypb.Empty{}, nil
}

func (s *Service) ListFriendRequests(ctx context.Context, req *desc.ListFriendRequestsRequest) (*desc.ListFriendRequestsResponse, error) {
	username := auth.UsernameFromCtx(ctx)
	limit, before := pagination.FromRequest[int64](req)

	var usernames []db.FriendRequest
	var next int64
	var err error
	if before == 0 {
		usernames, next, err = s.storage.ListIncomingFriendRequestsFirst(username, limit)
	} else {
		usernames, next, err = s.storage.ListIncomingFriendRequestsNext(username, before, limit)
	}

	if err != nil {
		zap.S().Errorw("listing friend requests", "before", before, "limit", limit, "error", err)
		return nil, internalServerError
	}

	string_usernames := lo.Map(usernames, func(f db.FriendRequest, _ int) string {
		if f.FromUsername == username {
			return f.ToUsername
		} else {
			return f.FromUsername
		}
	})

	return &desc.ListFriendRequestsResponse{
		Usernames:     string_usernames,
		NextPageToken: pagination.EncodePageToken(next),
	}, nil
}

func (s *Service) ListFriends(ctx context.Context, req *desc.ListFriendsRequest) (*desc.ListFriendsResponse, error) {
	username := req.Username
	limit, before := pagination.FromRequest[int64](req)

	var usernames []db.FriendRequest
	var next int64
	var err error
	if before == 0 {
		usernames, next, err = s.storage.ListFriendsFirst(username, limit)
	} else {
		usernames, next, err = s.storage.ListFriendsNext(username, before, limit)
	}

	if err != nil {
		zap.S().Errorw("listing friends", "before", before, "limit", limit, "error", err)
		return nil, internalServerError
	}

	string_usernames := lo.Map(usernames, func(f db.FriendRequest, _ int) string {
		if f.FromUsername == username {
			return f.ToUsername
		} else {
			return f.FromUsername
		}
	})

	return &desc.ListFriendsResponse{
		Usernames:     string_usernames,
		NextPageToken: pagination.EncodePageToken(next),
	}, nil
}

func (s *Service) RemoveFriend(ctx context.Context, req *desc.RemoveFriendRequest) (*emptypb.Empty, error) {
	username := auth.UsernameFromCtx(ctx)

	// Token might be used for a deleted user. In this case we shouldn't do anything.
	if err := s.storage.RemoveFriend(username, req.Username); db.IsNotFound(err) {
		if db.ErrDetails(err) == "user_from" {
			return &emptypb.Empty{}, nil
		}
		return nil, friendNotFoundError
	} else if err != nil {
		zap.S().Errorw("removing friend", "error", err)
		return nil, internalServerError
	}

	return &emptypb.Empty{}, nil
}
