package app

import (
	"context"
	"regexp"

	"github.com/irdkwmnsb/DrinkKollect/backend/internal/auth"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/crypto"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/db"
	desc "github.com/irdkwmnsb/DrinkKollect/backend/internal/pb/v1/drinkollect"
	"go.uber.org/zap"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
	"google.golang.org/protobuf/types/known/emptypb"
)

var usernameRe = regexp.MustCompile(`^[a-zA-Z]\w+$`)

var (
	errLoginTooShort         = status.Error(codes.InvalidArgument, "Выберите логин не короче 3 символов")
	errLoginTooLong          = status.Error(codes.InvalidArgument, "Выберите логин не длиннее 16 символов")
	errLoginContainsBadChars = status.Error(codes.InvalidArgument, "Выберите логин, состоящий из символов латинского алфавита, цифр, или нижнего подчёркивания")
	errPasswordTooShort      = status.Error(codes.InvalidArgument, "Выберите пароль не короче 5 символов")
	errLoginTaken            = status.Error(codes.AlreadyExists, "Выбранный логин уже занят, попробуйте выбрать иной")

	errInvalidLoginOrPassword = status.Error(codes.InvalidArgument, "Проверьте учётные данные, вы ввели несуществующее имя пользователя или неправильный пароль")
	errInvalidPassword        = status.Error(codes.InvalidArgument, "Проверьте указанный текущий пароль, он введён неправильно")
)

// Register performs registration of a new user.
func (s *Service) Register(ctx context.Context, req *desc.RegisterRequest) (*desc.RegisterResponse, error) {
	// Validate request parameters
	if len(req.Username) < 3 {
		return nil, errLoginTooShort
	} else if len(req.Username) > 16 {
		return nil, errLoginTooLong
	} else if !usernameRe.MatchString(req.Username) {
		return nil, errLoginContainsBadChars
	}

	if len(req.Password) < 5 {
		return nil, errPasswordTooShort
	}

	// Hash password to be stored in the DB
	passwordHash, err := crypto.HashPassword(req.Password)
	if err != nil {
		zap.S().Errorw("hashing user password", "error", err)
		return nil, internalServerError
	}

	// Try creating user and handle such user already existing
	if err := s.storage.CreateUser(req.Username, passwordHash); db.IsAlreadyExists(err) {
		return nil, errLoginTaken
	} else if err != nil {
		zap.S().Errorw("creating user in db", "error", err)
		return nil, internalServerError
	}

	// Generate JWT so that the new user is immediately authorized
	token, err := s.authorizer.Construct(req.Username)
	if err != nil {
		zap.S().Errorw("constructing JWT after registration", "username", req.Username, "error", err)
		return nil, internalServerError
	}

	return &desc.RegisterResponse{Token: token}, nil
}

// Login performs login logic for an already registered user.
func (s *Service) Login(ctx context.Context, req *desc.LoginRequest) (*desc.LoginResponse, error) {
	// If user doesn't exist in the DB, the message shouldn't hint at this,
	// instead returning the same message as during a wrong password
	user, err := s.storage.GetUser(req.Username)
	if db.IsNotFound(err) {
		return nil, errInvalidLoginOrPassword
	} else if err != nil {
		zap.S().Errorw("getting user from db", "username", req.Username, "error", err)
		return nil, internalServerError
	}

	if !crypto.ValidateHashedPassword(req.Password, user.PasswordHash) {
		return nil, errInvalidLoginOrPassword
	}

	// Generate JWT after user has properly authenticated
	token, err := s.authorizer.Construct(req.Username)
	if err != nil {
		zap.S().Errorw("constructing JWT after login", "username", req.Username, "error", err)
		return nil, internalServerError
	}

	return &desc.LoginResponse{Token: token}, nil
}

// ChangePassword changes an authorized user's password.
func (s *Service) ChangePassword(ctx context.Context, req *desc.ChangePasswordRequest) (*emptypb.Empty, error) {
	username := auth.UsernameFromCtx(ctx)

	// Note: race condition between GetUser and UpdateUserPasswordHash. Too bad!
	user, err := s.storage.GetUser(username)
	if err != nil {
		zap.S().Errorw("getting user from db during pasword change", "username", username, "error", err)
		return nil, internalServerError
	}

	if !crypto.ValidateHashedPassword(req.Password, user.PasswordHash) {
		return nil, errInvalidPassword
	}

	// Hash new password to be stored in the DB
	passwordHash, err := crypto.HashPassword(req.NewPassword)
	if err != nil {
		zap.S().Errorw("hashing new password", "error", err)
		return nil, internalServerError
	}

	if err := s.storage.UpdateUserPasswordHash(username, passwordHash); err != nil {
		return nil, internalServerError
	}

	return &emptypb.Empty{}, nil
}

// DeleteAccount deletes an authorized user's account.
func (s *Service) DeleteAccount(ctx context.Context, _ *emptypb.Empty) (*emptypb.Empty, error) {
	username := auth.UsernameFromCtx(ctx)
	if err := s.storage.DeleteUser(username); err != nil {
		return nil, internalServerError
	}

	return &emptypb.Empty{}, nil
}

func (s *Service) ListUsers(ctx context.Context, req *desc.ListUsersRequest) (*desc.ListUsersResponse, error) {

	users, err := s.storage.GetUsersWildcard("%" + req.Username + "%")
	if err != nil {
		zap.S().Errorw("listing users", "error", err)
		return nil, internalServerError
	}

	usernames := make([]string, len(users))
	for i, user := range users {
		usernames[i] = user.Username
	}

	return &desc.ListUsersResponse{Usernames: usernames}, nil
}
