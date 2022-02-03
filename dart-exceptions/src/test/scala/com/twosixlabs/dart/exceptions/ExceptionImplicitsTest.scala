package com.twosixlabs.dart.exceptions

import java.util.concurrent.CompletableFuture
import java.util.function.Supplier

import com.twosixlabs.dart.exceptions.ExceptionImplicits._
import com.twosixlabs.dart.test.base.ScalaTestBase

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try


class ExceptionImplicitsTest extends ScalaTestBase {

    behavior of "ThrowableImplicits.withErrorString"

    it should "allow a side effect with an entire error string, including classname, message, and full stack trace" in {
        val exceptionCause = new NullPointerException( "This is the test exception that caused the test exception" )
        val exception = new IllegalStateException( "This is a test exception", exceptionCause )
        var result : String = "this should become the output from the test exception"
        exception.withErrorString( result = _ )
        println( result )
        result should include( "IllegalStateException: This is a test exception" )
        result should include( "This is the test exception that caused the test exception" )
        result should include( "NullPointerException" )
    }

    behavior of "FutureImplicits.withError"

    it should "allow a side effect with an exception" in {
        val exceptionCause = new NullPointerException( "This is the test exception that caused the test exception" )
        val exception = new IllegalStateException( "This is a test exception", exceptionCause )
        var resultException : Throwable = new Exception( "this should eventually be `exception`" )
        val future = Future ( throw exception )
        val newFuture = future.withError( resultException = _ )
        newFuture shouldBe future
        Try ( Await.result( future, 10 seconds ) )
        resultException shouldBe exception
    }

    behavior of "FutureImplicits.withErrorString"

    it should "allow a side effect with an exception message" in {
        val exceptionCause = new NullPointerException( "This is the test exception that caused the test exception" )
        val exception = new IllegalStateException( "This is a test exception", exceptionCause )
        var result : String = "this should eventually be error message"
        val future = Future ( throw exception )
        val newFuture = future.withErrorString( result = _ )
        newFuture shouldBe future
        Try ( Await.result( future, 10 seconds ) )
        Thread.sleep( 1000 ) // need this because it's a different future that handles the error
        println( result )
        result should include( "IllegalStateException: This is a test exception" )
        result should include( "This is the test exception that caused the test exception" )
        result should include( "NullPointerException" )
    }

    behavior of "TryImplicits.withError"

    it should "allow a side effect with an exception" in {
        val exceptionCause = new NullPointerException( "This is the test exception that caused the test exception" )
        val exception = new IllegalStateException( "This is a test exception", exceptionCause )
        var resultException : Throwable = new Exception( "this should eventually be `exception`" )
        val originalTry = Try ( throw exception )
        val newTry = originalTry.withError( resultException = _ )
        newTry shouldBe originalTry
        resultException shouldBe exception
    }

    behavior of "TryImplicits.withErrorString"

    it should "allow a side effect with an exception message" in {
        val exceptionCause = new NullPointerException( "This is the test exception that caused the test exception" )
        val exception = new IllegalStateException( "This is a test exception", exceptionCause )
        var result : String = "this should eventually be error message"
        val originalTry = Try ( throw exception )
        val newTry = originalTry.withErrorString( result = _ )
        println( result )
        newTry shouldBe originalTry
        result should include( "IllegalStateException: This is a test exception" )
        result should include( "This is the test exception that caused the test exception" )
        result should include( "NullPointerException" )
    }

    behavior of "CompletableFutureImplicits.withError"

    it should "allow a side effect with an exception" in {
        val exceptionCause = new NullPointerException( "This is the test exception that caused the test exception" )
        val exception = new IllegalStateException( "This is a test exception", exceptionCause )
        var resultException : Throwable = new Exception( "this should eventually be `exception`" )
        val future : CompletableFuture[ Int ] = CompletableFuture.supplyAsync( new Supplier[ Int ]() {
            override def get( ) : Int = {
                Thread.sleep( 1000 )
                throw exception
                10
            }
        } )
        val newFuture = future.withError( resultException = _ )
        Try ( newFuture.get )
        resultException shouldBe exception
    }

    behavior of "CompletableFutureImplicits.withErrorString"

    it should "allow a side effect with an exception message" in {
        val exceptionCause = new NullPointerException( "This is the test exception that caused the test exception" )
        val exception = new IllegalStateException( "This is a test exception", exceptionCause )
        var result : String = "this should eventually be error message"
        val future : CompletableFuture[ Int ] = CompletableFuture.supplyAsync( new Supplier[ Int ]() {
            override def get( ) : Int = {
                Thread.sleep( 1000 )
                throw exception
                10
            }
        } )
        val newFuture = future.withErrorString( result = _ )
        Try ( newFuture.get )
        println( result )
        result should include( "IllegalStateException: This is a test exception" )
        result should include( "This is the test exception that caused the test exception" )
        result should include( "NullPointerException" )
    }
}
