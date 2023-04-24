package storage

import (
	"github.com/genjidb/genji"
	"github.com/genjidb/genji/document"
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
		return User{}, wrapError("GetUserPasswordHash", err)
	}

	var user User
	if err := document.StructScan(doc, &user); err != nil {
		return User{}, wrapScanError("GetUserPasswordHash", err)
	}

	return user, nil
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
		if err := g.db.Exec(deletePostsQuery, username); err != nil {
			return wrapError("DeleteUser.deletePosts", err)
		}

		if err := g.db.Exec(deleteUserQuery, username); err != nil {
			return wrapError("DeleteUser.deleteUser", err)
		}

		return nil
	})
}
