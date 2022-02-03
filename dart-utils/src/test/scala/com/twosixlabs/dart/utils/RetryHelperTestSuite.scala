package com.twosixlabs.dart.utils

import java.io.IOException

import com.twosixlabs.dart.test.base.ScalaTestBase
import org.scalamock.scalatest.MockFactory

import scala.util.{Failure, Success, Try}


class RetryHelperTestSuite extends ScalaTestBase with MockFactory {

    "RetryHelper.retry" should "retry when IOException is thrown" in {
        val specFunctionMock = mockFunction[ String, Try[ String ] ]
        specFunctionMock.expects( * ).onCall( { arg : String => Failure( new IOException( "message" ) ) } ).repeat( 3 )
        RetryHelper.retry( 3 )( specFunctionMock( "test" ) )
    }

    "RetryHelper.retry" should "not retry when function returns successfully" in {
        val specFunctionMock = mockFunction[ String, Try[ String ] ]
        specFunctionMock.expects( * ).onCall( { arg : String => Success( "Success" ) } ).repeat( 1 )
        RetryHelper.retry( 3 )( specFunctionMock( "" ) )
    }

    "RetryHelper.retry" should "pause for the approrpiate amount of time" in {
        val numRetries = 3
        val testPauseMillis = 1000

        val specFunctionMock = mockFunction[ String, Try[ String ] ]
        specFunctionMock.expects( * ).onCall( { arg : String => Failure( new IOException( "message" ) ) } ).repeat( 3 )

        val startTime = System.currentTimeMillis()

        RetryHelper.retry( numRetries, testPauseMillis )( specFunctionMock( "test" ) )

        val expectedTime : Long = testPauseMillis * numRetries
        val totalTime : Long = System.currentTimeMillis() - startTime

        totalTime should be >= expectedTime
    }

}
