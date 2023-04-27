package auth

import (
	"context"
	"crypto/ecdsa"
	"fmt"

	"github.com/go-jose/go-jose/v3"
	"github.com/go-jose/go-jose/v3/jwt"
	"github.com/grpc-ecosystem/go-grpc-middleware/v2/interceptors/auth"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

const bearerScheme = "Bearer"

type usernameClaims struct {
	Username string `json:"username"`
}

// Authorizer performs authorization actions on JWTs containing the authorized user's username signed with ecdsa.
type Authorizer struct {
	key    *ecdsa.PublicKey
	signer jose.Signer
}

// NewAuthorizer creates a new authorizer using an ecdsa private key.
func NewAuthorizer(key *ecdsa.PrivateKey) (*Authorizer, error) {
	signer, err := jose.NewSigner(jose.SigningKey{Algorithm: jose.ES256, Key: key}, nil)
	if err != nil {
		return nil, fmt.Errorf("creating signer: %w", err)
	}

	return &Authorizer{key: &key.PublicKey, signer: signer}, nil
}

// UnaryInterceptor returns a unary gRPC interceptor which authorizes requests to all endpoints except the whitelisted ones.
// The authorized user's username can be then retrieved
func (a *Authorizer) UnaryInterceptor(whitelist ...string) grpc.UnaryServerInterceptor {
	whitelistedEndpoints := make(map[string]struct{})
	for _, endpoint := range whitelist {
		whitelistedEndpoints[endpoint] = struct{}{}
	}

	return func(ctx context.Context, req interface{}, info *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (resp interface{}, err error) {
		if _, ok := whitelistedEndpoints[info.FullMethod]; ok {
			return handler(ctx, req)
		}

		token, err := auth.AuthFromMD(ctx, bearerScheme)
		if err != nil {
			return nil, status.Error(codes.Unauthenticated, "Missing token")
		}

		username, ok := a.Extract(token)
		if !ok {
			return nil, status.Error(codes.Unauthenticated, "Invalid token")
		}

		return handler(usernameToCtx(ctx, username), req)
	}
}

// Construct constructs a new JWT containing the specified username.
func (a *Authorizer) Construct(username string) (string, error) {
	token, err := jwt.Signed(a.signer).Claims(usernameClaims{username}).CompactSerialize()
	if err != nil {
		return "", fmt.Errorf("constructing token: %w", err)
	}

	return token, nil
}

// Extract extracts a username from the JWT claims. A boolean value is returned indicating whether
// the token was parsed and verified successfully.
func (a *Authorizer) Extract(jwtString string) (string, bool) {
	token, err := jwt.ParseSigned(jwtString)
	if err != nil {
		return "", false
	}

	var claims usernameClaims
	if err := token.Claims(a.key, &claims); err != nil {
		return "", false
	} else if claims.Username == "" {
		return "", false
	}

	return claims.Username, true
}
