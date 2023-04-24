package storage

import (
	"time"

	"github.com/genjidb/genji"
	"github.com/genjidb/genji/document"
)

// Post is the DB model describing a single post.
type Post struct {
	ID              uint64
	Title           string
	Description     string
	Location        string
	ImageBucket     string
	ImageID         string
	CreatorUsername string
	CreatedAt       time.Time
	Likes           uint64
	Liked           bool
}

// CreatePost creates a new post. The ID and CreatedAt are autogenerated.
// If the user specified as the creator doesn't exist, a NotFound error is returned.
func (g *Genji) CreatePost(post Post) error {
	const query = `
			insert into post (
				id, title, description, location, image_bucket, image_id, creator_username, created_at
			)	values (
				next value for post_id,
				?, ?, ?, ?, ?, ?, ?
			)
		`

	return g.withTx(true, func(tx *genji.Tx) error {
		userExists, err := g.checkUserExists(tx, post.CreatorUsername)
		if err != nil {
			return err
		} else if !userExists {
			return notFound("user")
		}

		return wrapError("CreatePost", g.db.Exec(query,
			post.Title, post.Description, post.Location, post.ImageBucket, post.ImageID, post.CreatorUsername, now(),
		))
	})
}

// AddLikedPost marks a post as liked by the user with the specified username.
// If this post is already marked as liked by the user, an AlreadyExists error is returned.
// If no such post or user exists, a NotFound error is returned.
func (g *Genji) AddLikedPost(username string, postID uint64) error {
	const query = `insert into post_likes (username, post_id) values (?, ?)`

	return g.withTx(true, func(tx *genji.Tx) error {
		userExists, err := g.checkUserExists(tx, username)
		if err != nil {
			return err
		} else if !userExists {
			return notFound("user")
		}

		postExists, err := g.checkPostExists(tx, postID)
		if err != nil {
			return err
		} else if !postExists {
			return notFound("post")
		}

		return wrapError("AddLikedPost", g.db.Exec(query, username, postID))
	})
}

func (g *Genji) checkUserExists(tx *genji.Tx, username string) (bool, error) {
	const query = `select true from user where username = ?`

	var exists bool
	existsCheckDoc, err := tx.QueryDocument(query, username)
	if err != nil {
		return false, wrapError("checkUser", err)
	}

	if err := document.Scan(existsCheckDoc, &exists); err != nil {
		return false, wrapScanError("checkUser", err)
	}

	return exists, nil
}

func (g *Genji) checkPostExists(tx *genji.Tx, postID uint64) (bool, error) {
	const query = `select true from post where id = ?`

	var exists bool
	existsCheckDoc, err := tx.QueryDocument(query, postID)
	if err != nil {
		return false, wrapError("checkPost", err)
	}

	if err := document.Scan(existsCheckDoc, &exists); err != nil {
		return false, wrapScanError("checkPost", err)
	}

	return exists, nil
}
