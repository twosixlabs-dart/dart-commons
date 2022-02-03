package com.twosixlabs.dart.utils

import com.twosixlabs.dart.test.base.ScalaTestBase

class IdGeneratorTest extends ScalaTestBase {

    "IdGenerator" should "create a 32 character length hash for the given input" in {
        val input = "hello world!".getBytes

        val result = IdGenerator.getMd5Hash( input )

        result.length shouldBe 32
    }

    "IdGenerator" should "create deterministic hash strings" in {
        val inputOne : Array[ Byte ] = "hello world!".getBytes
        val inputTwo : Array[ Byte ] = Array( 104.byteValue(), 101.byteValue(),
                                              108.byteValue(), 108.byteValue(),
                                              111.byteValue(), 32.byteValue(),
                                              119.byteValue(), 111.byteValue(),
                                              114.byteValue(), 108.byteValue(),
                                              100.byteValue(), 33.byteValue() ) // reconstruct the string as a raw byte array...

        val resultOne = IdGenerator.getMd5Hash( inputOne )
        val resultTwo = IdGenerator.getMd5Hash( inputTwo )

        resultOne shouldBe resultTwo
    }

}