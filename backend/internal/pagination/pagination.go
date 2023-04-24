package pagination

import (
	"encoding/base64"
	"encoding/json"
)

const (
	defaultPageSize = 10
	maxPageSize     = 100
)

type paginated interface {
	GetPageSize() int32
	GetPageToken() string
}

// CalculateLimit calculates the actual limit to use based on the page size received.
// It is equal to min(pageSize < 1 ? defaultPageSize : pageSize, maxPageSize)
func CalculateLimit(pageSize int32) int32 {
	limit := pageSize
	if limit < 1 {
		limit = defaultPageSize
	} else if limit > maxPageSize {
		limit = maxPageSize
	}
	return limit
}

// DecodePageToken tries to decode the page token using base64+json, otherwise returning a nil slice.
func DecodePageToken[T any](pageToken string) T {
	var v T

	data, err := base64.RawStdEncoding.DecodeString(pageToken)
	if err != nil {
		return v
	}

	_ = json.Unmarshal(data, &v)
	return v
}

// EncodePageToken encodes a page token using json+base64.
func EncodePageToken[T any](pageToken T) string {
	data, err := json.Marshal(pageToken)
	if err != nil {
		return ""
	}

	return base64.RawStdEncoding.EncodeToString(data)
}

// FromRequest calculates the limit and page token from an API request containing page_size and page_token fields.
func FromRequest[T any](request paginated) (int32, T) {
	return CalculateLimit(request.GetPageSize()), DecodePageToken[T](request.GetPageToken())
}
