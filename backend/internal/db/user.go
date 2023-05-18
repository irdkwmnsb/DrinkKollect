package db

import (
	"fmt"

	"github.com/genjidb/genji"
	"github.com/genjidb/genji/document"
	"github.com/genjidb/genji/types"
)

// User is the DB model describing a single user.
type User struct {
	Username     string
	PasswordHash string `genji:"password_hash"`
}

// CreateUser creates a new user. If a user with the given username already exists, an AlreadyExists error will be returned.
func (g *Genji) CreateUser(username, passwordHash string) error {
	const query = `insert into user (username, password_hash) values (?, ?)`

	return wrapError("CreateUser", g.db.Exec(query, username, passwordHash))
}

// GetUser gets a user with given the username. If no such user exists, a NotFound error will be returned.
func (g *Genji) GetUser(username string) (User, error) {
	const query = `select * from user where username = ?`

	doc, err := g.db.QueryDocument(query, username)
	if err != nil {
		return User{}, wrapError("GetUser", err)
	}

	var user User
	if err := document.StructScan(doc, &user); err != nil {
		return User{}, wrapScanError("GetUser", err)
	}

	return user, nil
}

const wildcardLimit = 20

// GetUsersWildcard gets a list of users with usernames matching the given wildcard.
func (g *Genji) GetUsersWildcard(username string) ([]User, error) {
	query := fmt.Sprintf(`select * from user where username like ? limit %d`, wildcardLimit)

	var users []User
	res, err := g.db.Query(query, username)
	if err != nil {
		return nil, wrapError("GetUsersWildcard", err)
	}
	if err := res.Iterate(func(d types.Document) error {
		var user User
		if err := document.StructScan(d, &user); err != nil {
			return wrapScanError("GetUsersWildcard", err)
		}

		users = append(users, user)
		return nil
	}); err != nil {
		return nil, wrapError("GetUsersWildcard", err)
	}
	return users, nil
}

// UpdateUserPasswordHash updates the given user's password hash. If no such user exists, a NotFound error will be returned.
func (g *Genji) UpdateUserPasswordHash(username, passwordHash string) error {
	const query = `update user set password_hash = ? where username = ?`

	return wrapError("UpdateUserPasswordHash", g.db.Exec(query, passwordHash, username))
}

// DeleteUser deletes a user with the specified username. Additionally, all posts of this user are deleted.
// If no such user exists, a NotFound error will be returned.
func (g *Genji) DeleteUser(username string) error {
	const (
		deletePostsQuery = `delete from post where creator_username = ?`
		deleteUserQuery  = `delete from user where username = ?`
	)

	return g.withTx(true, func(tx *genji.Tx) error {
		if err := tx.Exec(deletePostsQuery, username); err != nil {
			return wrapError("DeleteUser.deletePosts", err)
		}

		if err := tx.Exec(deleteUserQuery, username); err != nil {
			return wrapError("DeleteUser.deleteUser", err)
		}

		return nil
	})
}
