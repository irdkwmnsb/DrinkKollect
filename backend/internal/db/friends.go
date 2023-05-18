package db

import (
	"strings"

	"github.com/genjidb/genji"
	"github.com/genjidb/genji/document"
	"github.com/genjidb/genji/types"
	"go.uber.org/zap"
)

type FriendRequest struct {
	FromUsername string `genji:"from_username"`
	ToUsername   string `genji:"to_username"`
	CreatedAt    int64  `genji:"created_at"`
}

// SendFriendRequest sends a friend request from one user to another.
// If the request already exists, this function returns an AlreadyExists error.
// If the from user does not exist, this function returns a NotFound user_from error.
// If the to user does not exist, this function returns a NotFound user_to error.
func (g *Genji) SendFriendRequest(username string, to string) error {
	const query = `insert into friend_requests (from_username, to_username, created_at) values (?, ?, ?)`

	return g.withTx(true, func(tx *genji.Tx) error {
		// Check if from user exists.
		fromExists, err := g.checkExists(tx, "user", "username", username)
		if err != nil {
			return wrapError("SendFriendRequest.UserExists.From", err)
		}
		if !fromExists {
			zap.L().Info("user does not exist", zap.String("username", username))
			return notFound("user_from")
		}

		// Check if to user exists.
		toExists, err := g.checkExists(tx, "user", "username", to)
		if err != nil {
			return wrapError("SendFriendRequest.UserExists.To", err)
		}
		if !toExists {
			zap.L().Info("user does not exist", zap.String("username", to))
			return notFound("user_to")
		}

		// Check if an incoming request already exists.
		requestExists, err := g.checkExists2(tx, "friend_requests", "from_username", to, "to_username", username)
		if err != nil {
			return wrapError("SendFriendRequest.Exists", err)
		}
		if requestExists {
			zap.L().Info("friend request already exists", zap.String("from", to), zap.String("to", username))
			return alreadyExists("friend_request")
		}

		return wrapError("SendFriendRequest.Exec", tx.Exec(query, username, to, now()))
	})
}

// AcceptFriendRequest accepts a friend request from one user to another.
// If the request does not exist, this function returns a NotFound error.
// If the from user does not exist, this function returns a NotFound user_from error.
// If the to user does not exist, this function returns a NotFound user_to error.
func (g *Genji) AcceptFriendRequest(username string, from string) error {
	const (
		deleteRequestQuery = `delete from friend_requests where from_username = ? and to_username = ?`
		insertFriendsQuery = `insert into friends (from_username, to_username, created_at) values (?, ?, ?)`
	)

	return g.withTx(true, func(tx *genji.Tx) error {
		// Check if from user exists.
		fromExists, err := g.checkExists(tx, "user", "username", from)
		if err != nil {
			return wrapError("AcceptFriendRequest.UserExists", err)
		}
		if !fromExists {
			return notFound("user_from")
		}

		// Check if an incoming request exists.
		requestExists, err := g.checkExists2(tx, "friend_requests", "from_username", from, "to_username", username)
		if err != nil {
			return wrapError("AcceptFriendRequest.RequestExists", err)
		}
		if !requestExists {
			return notFound("friend_request")
		}

		// Delete the request.
		err = wrapError("AcceptFriendRequest.ExecDelete", tx.Exec(deleteRequestQuery, from, username))
		if err != nil {
			return err
		}

		// Check if to user exists.
		toExists, err := g.checkExists(tx, "user", "username", username)
		if err != nil {
			return wrapError("AcceptFriendRequest.UserExists", err)
		}
		if !toExists {
			return notFound("user_to")
		}

		// Insert the friendship.
		return wrapError("AcceptFriendRequest.ExecInsert", tx.Exec(insertFriendsQuery, from, username, now()))
	})
}

// RejectFriendRequest rejects a friend request from one user to another.
// If the request does not exist, this function returns a NotFound error.
// If the from user does not exist, this function returns a NotFound user_from error.
// If the to user does not exist, this function returns a NotFound user_to error.
func (g *Genji) RejectFriendRequest(username string, from string) error {
	const query = `delete from friend_requests where from_username = ? and to_username = ?`

	return g.withTx(true, func(tx *genji.Tx) error {
		// Check if from user exists.
		fromExists, err := g.checkExists(tx, "user", "username", from)
		if err != nil {
			return wrapError("RejectFriendRequest.UserExists", err)
		}
		if !fromExists {
			return notFound("user_from")
		}

		// We don't need to check if to user exists because we don't need it.

		// Check if an incoming request exists.
		requestExists, err := g.checkExists2(tx, "friend_requests", "from_username", from, "to_username", username)
		if err != nil {
			return wrapError("RejectFriendRequest.RequestExists", err)
		}
		if !requestExists {
			return notFound("friend_request")
		}

		// Delete the request.
		return wrapError("RejectFriendRequest.Exec", tx.Exec(query, from, username))
	})
}

func (g *Genji) ListFriendsFirst(username string, limit int32) ([]FriendRequest, int64, error) {
	const query = `select * from friends where (to_username = ? or from_username = ?) order by created_at desc limit ?`

	res, err := g.db.Query(query, username, username, limit)
	if err != nil {
		return nil, 0, wrapError("ListFriendRequests", err)
	}

	return g.scanListFriendRequestsLikeResult(res)
}

func (g *Genji) ListFriendsNext(username string, before int64, limit int32) ([]FriendRequest, int64, error) {
	const query = `select * from friend_requests where (to_username = ? or from_username = ?) and created_at < ? order by created_at desc limit ?`

	res, err := g.db.Query(query, username, username, before, limit)
	if err != nil {
		return nil, 0, wrapError("ListFriendRequests", err)
	}

	return g.scanListFriendRequestsLikeResult(res)
}

func (g *Genji) ListIncomingFriendRequestsFirst(username string, limit int32) ([]FriendRequest, int64, error) {
	const query = `select * from friend_requests where (to_username = ?) order by created_at desc limit ?`

	res, err := g.db.Query(query, username, limit)
	if err != nil {
		return nil, 0, wrapError("ListFriendRequests", err)
	}

	return g.scanListFriendRequestsLikeResult(res)
}

func (g *Genji) ListIncomingFriendRequestsNext(username string, before int64, limit int32) ([]FriendRequest, int64, error) {
	const query = `select * from friend_requests where (to_username = ?) and created_at < ? order by created_at desc limit ?`

	res, err := g.db.Query(query, username, before, limit)
	if err != nil {
		return nil, 0, wrapError("ListFriendRequests", err)
	}

	return g.scanListFriendRequestsLikeResult(res)
}

func (g *Genji) scanListFriendRequestsLikeResult(res *genji.Result) ([]FriendRequest, int64, error) {
	var friends []FriendRequest
	if err := res.Iterate(func(d types.Document) error {
		var request FriendRequest
		if err := document.StructScan(d, &request); err != nil {
			return err
		}

		request.FromUsername = strings.Clone(request.FromUsername)
		request.ToUsername = strings.Clone(request.ToUsername)

		friends = append(friends, request)
		return nil
	}); err != nil {
		return nil, 0, err
	}

	// Prepare next "page id" if any results are available
	var nextBefore int64
	if len(friends) > 0 {
		nextBefore = friends[len(friends)-1].CreatedAt
	}

	zap.L().Info("ListFriends", zap.Any("d", friends))

	return friends, nextBefore, nil
}

func (g *Genji) getFriendList(username string) ([]string, error) {
	const query = `select * from friends where (username = ? or friend_username = ?)`

	var friends []string
	res, err := g.db.Query(query, username, username)
	if err != nil {
		return nil, wrapError("ListFriends", err)
	}
	if err := res.Iterate(func(d types.Document) error {
		var friend FriendRequest
		if err := document.StructScan(d, &friend); err != nil {
			return err
		}
		if friend.FromUsername == username {
			friends = append(friends, friend.ToUsername)
		} else {
			friends = append(friends, friend.FromUsername)
		}
		return nil
	}); err != nil {
		return nil, err
	}
	return friends, nil
}

func (g *Genji) RemoveFriend(username string, friendUsername string) error {
	const query = `delete from friends where (username = ? and friend_username = ?) or (username = ? and friend_username = ?)`

	return wrapError("RemoveFriend.Exec", g.db.Exec(query, username, friendUsername, friendUsername, username))
}
