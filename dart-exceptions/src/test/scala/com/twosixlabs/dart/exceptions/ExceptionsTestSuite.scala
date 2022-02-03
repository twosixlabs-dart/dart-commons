package com.twosixlabs.dart.exceptions

import com.twosixlabs.dart.test.base.ScalaTestBase

import scala.util.{Failure, Try}

class ExceptionsTestSuite extends ScalaTestBase {

    "Exceptions utility" should "convert a stack trace into a printable string" in {
        try {
            throw new IllegalStateException( "this is bad!!!" )
        } catch {
            case e : Throwable => {
                val stackTrace = Exceptions.getStackTraceText( e )
                stackTrace.contains( "java.lang.IllegalStateException: this is bad!!!" ) shouldBe true
            }
        }
    }

    behavior of "DartRestException"

    it should "match classes that extend it" in {
        Try( throw new MalformedCdrException( "malformed cdr" ) ) match {
            case Failure( e : DartException ) => e.getMessage shouldBe "malformed cdr"
            case _ => fail( "MalformedCdrException didn't match DartException" )
        }

        Try( throw new BadRequestBodyException( "test_field", Some( "test_value" ), "test_field should have integer value" ) ) match {
            case Failure( e : DartRestException ) => e.getMessage shouldBe "invalid request body: field test_field=test_value does not conform to required format: test_field should have integer value"
            case _ => fail( "BadRequestBodyException (a DartRestException) didn't match DartException")
        }
    }

    behavior of "DartException"

    it should "match classes that extend it" in {
        Try( throw new BadRequestBodyException( "test_field", Some( "test_value" ), "test_field should have integer value" ) ) match {
            case Failure( e : DartRestException ) => e.getMessage shouldBe "invalid request body: field test_field=test_value does not conform to required format: test_field should have integer value"
            case _ => fail( "BadRequestBodyException didn't match DartRestException")
        }
    }

}
