package storage

import (
	"errors"
	"fmt"

	"github.com/genjidb/genji"
)

// errCode is a code describing the type of DB error that occurred
type errCode int

const (
	errCodeNotFound errCode = iota
	errCodeAlreadyExists
)

var (
	errAlreadyExists = alreadyExists("")
	errNotFound      = notFound("")
)

// dbError is a simple wrapper around errCode and additional possible details
type dbError struct {
	code    errCode
	details string
}

func (e dbError) Error() string {
	if e.details == "" {
		return e.fmtCode()
	}

	return fmt.Sprintf("%s: %s", e.details, e.fmtCode())
}

func (e dbError) fmtCode() string {
	switch e.code {
	case errCodeNotFound:
		return "entity not found"
	case errCodeAlreadyExists:
		return "entity already exists"
	default:
		return "unknown error"
	}
}

// notFound is a constructor for a NotFound error with additional details.
func notFound(details string) dbError {
	return dbError{code: errCodeNotFound, details: details}
}

// alreadyExists is a constructor for an AlreadyExists error with additional details.
func alreadyExists(details string) dbError {
	return dbError{code: errCodeAlreadyExists, details: details}
}

// IsAlreadyExists determines whether the given error signifies that an entity already exists.
func IsAlreadyExists(err error) bool {
	var e dbError
	return errors.As(err, &e) && e.code == errCodeAlreadyExists
}

// IsNotFound determines whether the given error signifies that an entity is not found.
func IsNotFound(err error) bool {
	var e dbError
	return errors.As(err, &e) && e.code == errCodeNotFound
}

// ErrDetails extracts details from a db error.
func ErrDetails(err error) string {
	var e dbError
	_ = errors.As(err, &e)
	return e.details
}

func wrapError(query string, err error) error {
	if err == nil {
		return nil
	}

	if genji.IsAlreadyExistsError(err) {
		return errAlreadyExists
	} else if genji.IsNotFoundError(err) {
		return errNotFound
	}

	return fmt.Errorf("executing %s query: %w", query, err)
}

func wrapScanError(query string, err error) error {
	if err == nil {
		return nil
	}

	return fmt.Errorf("scanning result of %s query: %w", query, err)
}
