package com.twosixlabs.dart.exceptions

trait DartException

class MalformedCdrException( message : String, cause : Throwable = null ) extends Exception( message, cause ) with DartException

class CdrPersistenceException( message : String, cause : Throwable = null ) extends Exception( message, cause ) with DartException

class MissingCdrException( message : String, cause : Throwable = null ) extends Exception( message, cause ) with DartException

class InconsistentCdrDataException( message : String, cause : Throwable = null ) extends Exception( message, cause ) with DartException

class AuthorizationException( message: String, cause : Throwable = null ) extends Exception( message, cause ) with DartException
