package app

import (
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/auth"
	"github.com/irdkwmnsb/DrinkKollect/backend/internal/db"
	desc "github.com/irdkwmnsb/DrinkKollect/backend/internal/pb/v1/drinkollect"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

var internalServerError = status.Error(codes.Internal, "Приложению плохо, попробуйте повторить немного позже!")

// Service implements the drinkollect v1 API service.
type Service struct {
	desc.UnimplementedDrinkollectServer
	storage    *db.Genji
	authorizer *auth.Authorizer
}

// NewService constructs a new service which uses the specifies storage.
func NewService(storage *db.Genji, authorizer *auth.Authorizer) *Service {
	return &Service{storage: storage, authorizer: authorizer}
}

// RegisterPB registers this service with the gRPC server.
func (s *Service) RegisterPB(server *grpc.Server) {
	desc.RegisterDrinkollectServer(server, s)
}
