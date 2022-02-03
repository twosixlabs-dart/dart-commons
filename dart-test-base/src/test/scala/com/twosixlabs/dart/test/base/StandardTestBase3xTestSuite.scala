package com.twosixlabs.dart.test.base

class StandardTestBase3xTestSuite extends StandardTestBase3x {

    "test base" should "execute a normal test" in {
        val x = 1 + 1
        x shouldBe 2
    }

    "test base" should "mock something" in {
        trait TestService {
            def process( ) : String
        }

        val service = mock[ TestService ]
        when( service.process ).thenReturn( "hello" )
        service.process() shouldBe "hello"

    }


}
