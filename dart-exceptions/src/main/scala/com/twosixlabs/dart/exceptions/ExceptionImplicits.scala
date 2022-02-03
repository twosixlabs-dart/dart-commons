package com.twosixlabs.dart.exceptions

import java.util.concurrent.CompletableFuture

import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

/**
  * A set of implicit conversions providing methods for exceptions and exception-handling classes
  * (e.g., Future, Try, CompletableFuture) that allow performing side-effects (i.e., logging) on
  * the exceptions and exception message. Each method returns the object they are called on, so
  * they are useful for logging without interrupting method chaining.
  *
  * Basic usages:
  *
  *     import com.twosixlabs.dart.exceptions.ExceptionImplicits._
  *
  *     1) val future = dbClient.queryReturningFuture( query ).logged
  *     2) val completableFuture = javaClient.queryReturningCompletableFuture( query ).logged
  *     3) val try = dbClient.queryReturningTry( query ).logged
  *     4) val result : Option[ Int ] = queryResponse match {
  *            case Success( value ) => Some( value )
  *            case Failure( e ) =>
  *                e.log
  *        }
  *
  * Log level is error, to change log level use .loggedInfo, .loggedWarning, or .loggedDebug
  * Stack traces can be logged with your own logger using .withErrorString( LOG.info ), and
  * custom error handling can be achieved using .withError() instead of .withErrorString()
  */
object ExceptionImplicits {

    val LOG : Logger = LoggerFactory.getLogger( getClass )

    implicit class ThrowableLogging( e : Throwable ) {
        def withErrorString( fn : String => Unit ): Unit = {
            fn( Exceptions.getStackTraceText( e ) )
        }

        def log() : Unit = e.withErrorString( LOG.error )
        def logInfo() : Unit = e.withErrorString( LOG.info )
        def logWarning() : Unit = e.withErrorString( LOG.warn )
        def logDebug() : Unit = e.withErrorString( LOG.debug )
    }

    implicit class FutureExceptionLogging[ T ]( f : Future[ T ] ) {
        def withError( fn : Throwable => Unit )( implicit ex : ExecutionContext ) : Future[ T ] = {
            f.failed.foreach( fn )
            f
        }

        def withErrorString( fn : String => Unit )( implicit ex : ExecutionContext ) : Future[ T ] = {
            f.withError( _.withErrorString( fn ) )
        }

        def logged( implicit ex : ExecutionContext ) : Future[ T ] = withErrorString( LOG.error )
        def loggedInfo( implicit ex : ExecutionContext ) : Future[ T ] = withErrorString( LOG.info )
        def loggedWarning( implicit ex : ExecutionContext ) : Future[ T ] = withErrorString( LOG.warn )
        def loggedDebug( implicit ex : ExecutionContext ) : Future[ T ] = withErrorString( LOG.debug )
    }

    implicit class TryExceptionLogging[ T ]( t : Try[ T ] ) {
        def withError( fn : Throwable => Unit ) : Try[ T ] = {
            t recoverWith {
                case e : Throwable =>
                    fn( e )
                    Failure( e )
            }
        }

        def withErrorString( fn : String => Unit ) : Try[ T ] = {
            t.withError( _.withErrorString( fn ) )
        }

        def logged : Try[ T ] = withErrorString( LOG.error )
        def loggedInfo : Try[ T ] = withErrorString( LOG.info )
        def loggedWarning : Try[ T ] = withErrorString( LOG.warn )
        def loggedDebug : Try[ T ] = withErrorString( LOG.debug )
    }

    implicit class CompletableFutureExceptionLogging[ T ]( f : CompletableFuture[ T ] ) {
        def withError( fn : Throwable => Unit ) : CompletableFuture[ T ] = {
            f.whenComplete( new java.util.function.BiConsumer[ T, Throwable]() {
                override def accept( t : T, u : Throwable ) : Unit =
                    if ( u != null ) {
                        // exception will always be CompletionException -- the cause should be the
                        // actually exception
                        fn( u.getCause )
                    }
            } )
        }

        def withErrorString( fn : String => Unit ) : CompletableFuture[ T ] = {
            f.withError( _.withErrorString( fn ) )
        }

        def logged : CompletableFuture[ T ] = withErrorString( LOG.error )
        def loggedInfo : CompletableFuture[ T ] = withErrorString( LOG.info )
        def loggedWarning : CompletableFuture[ T ] = withErrorString( LOG.warn )
        def loggedDebug : CompletableFuture[ T ] = withErrorString( LOG.debug )
    }

}
