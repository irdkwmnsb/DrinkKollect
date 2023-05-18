package db

import (
	"fmt"

	"github.com/genjidb/genji"
	"github.com/genjidb/genji/document"
	"go.uber.org/zap"
)

type migration struct {
	name string
	sql  string
}

var migrations = []migration{
	{
		"init",
		`
		create table user (
			username text primary key,
			password_hash text not null
		);

		create table post (
			id integer primary key,
			title text not null,
			description text not null,
			location text not null,
			image_bucket text not null,
			image_id text not null,
			creator_username text not null,
			created_at integer not null
		);

		create sequence post_id;

		create table post_likes (
			username text not null,
			post_id integer not null
		);

		create unique index on post_likes(username, post_id);
		`,
	},
	{
		"friends",
		`
		create table friends (
			from_username text not null,
			to_username text not null,
			created_at integer not null
		);

		create unique index on friends(from_username, to_username);

		create table friend_requests (
			from_username text not null,
			to_username text not null,
			created_at integer not null
		);

		create unique index on friend_requests(from_username, to_username);
		`,
	},
}

type migrator struct {
	db *genji.DB
}

// execute executes all of the pending migrations which haven't been marked in the migrations_version table yet
func (m migrator) execute() error {
	if err := m.initializeMigrationsTable(); err != nil {
		return err
	}

	curVersion, err := m.currentVersion()
	if genji.IsNotFoundError(err) {
		curVersion = -1
	} else if err != nil {
		return fmt.Errorf("getting version from migrations_version table: %w", err)
	}

	zap.S().Infow("current migrations version", "component", "storage", "version", curVersion)

	toapply := migrations[curVersion+1:]
	if len(toapply) == 0 {
		zap.S().Infow("no new migrations to apply", "component", "storage")
		return nil
	}

	for version, mi := range toapply {
		version_i := curVersion + version + 1
		if err := m.executeMigration(version_i, mi.name, mi.sql); err != nil {
			return err
		}

		zap.S().Infow(fmt.Sprintf("applied migration %q", mi.name), "component", "storage", "version", version_i)
	}
	return nil
}

func (m *migrator) currentVersion() (int, error) {
	const query = `select version from migrations_version order by version desc limit 1`

	doc, err := m.db.QueryDocument(query)
	if err != nil {
		return 0, err
	}

	var version int
	if err := document.Scan(doc, &version); err != nil {
		return 0, fmt.Errorf("reading migrations version: %w", err)
	}

	return version, nil
}

func (m *migrator) initializeMigrationsTable() error {
	const query = `
		create table if not exists migrations_version (
			version integer primary key,
			ts integer not null
		)
	`

	if err := m.db.Exec(query); err != nil {
		return fmt.Errorf("initializing migrations_version table: %w", err)
	}

	return nil
}

func (m *migrator) executeMigration(version int, name string, sql string) error {
	const setVersionQuery = `insert into migrations_version values (?, ?)`

	tx, err := m.db.Begin(true)
	if err != nil {
		return fmt.Errorf("beginning migration transaction: %w", err)
	}
	defer tx.Rollback()

	if err := tx.Exec(sql); err != nil {
		return fmt.Errorf("executing migration %q: %w", name, err)
	}

	if err := tx.Exec(setVersionQuery, version, now()); err != nil {
		return fmt.Errorf("setting migration %q version %d: %w", name, version, err)
	}

	if err := tx.Commit(); err != nil {
		return fmt.Errorf("committing migration %q changes: %w", name, err)
	}

	return nil
}
