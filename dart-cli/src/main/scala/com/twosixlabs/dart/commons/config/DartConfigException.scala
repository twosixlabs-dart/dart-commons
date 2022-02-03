package com.twosixlabs.dart.commons.config

class DartConfigException( message : String, cause : Throwable ) extends Exception( message, cause ) {
    def this( message : String ) = this( message, null )
}
