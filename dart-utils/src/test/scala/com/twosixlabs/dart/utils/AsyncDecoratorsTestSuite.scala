package com.twosixlabs.dart.utils

import com.twosixlabs.dart.test.base.StandardTestBase3x
import com.twosixlabs.dart.utils.AsyncDecorators.DecoratedFuture

import scala.concurrent.Future
import scala.util.{Failure, Success}

class AsyncDecoratorsTestSuite extends StandardTestBase3x {

    "Async Decorators" should "turn a successful async call into a successful synchronous call" in {
        def doWork( ) : Future[ Boolean ] = {
            Thread.sleep( 1000 )
            Future.successful( true )
        }

        doWork() synchronously match {
            case Success( value ) => value shouldBe true
            case Failure( e ) => fail( e )
        }

    }

    "Async Decorators" should "turn an unsuccessful async call into a unsuccessful synchronous call" in {
        def doWork( ) : Future[ Boolean ] = {
            Thread.sleep( 1000 )
            Future.failed( new Exception( "test exception" ) )
        }

        doWork() synchronously match {
            case Success( value ) => fail( "test expected an error scenario" )
            case Failure( e ) => e.getMessage shouldBe "test exception"
        }
    }

}
