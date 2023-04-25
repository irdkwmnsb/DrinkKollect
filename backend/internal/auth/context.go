package auth

import "context"

type usernameCtxKey struct{}

func usernameToCtx(ctx context.Context, username string) context.Context {
	return context.WithValue(ctx, usernameCtxKey{}, username)
}

// UsernameFromCtx returns the username stored in the context. If no username is stored, an empty string is returned.
func UsernameFromCtx(ctx context.Context) string {
	value := ctx.Value(usernameCtxKey{})
	username, ok := value.(string)

	if !ok {
		return ""
	}
	return username
}
