package storage

import "time"

func now() int64 {
	return time.Now().UnixMilli()
}
