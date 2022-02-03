package com.twosixlabs.dart.json

import com.fasterxml.jackson.databind.node.ObjectNode
import com.twosixlabs.dart.test.base.ScalaTestBase

import scala.util.Try

class JsonFormatTestSuite extends ScalaTestBase with JsonFormat {

    "Json Format generic marshalling methods" should "marshal arbitrarily typed objects into JSON" in {
        val obj : ObjectNode = {
            val root = objectMapper.createObjectNode()
            root.put( "name", "michael" )
            root
        }

        val result : Try[ String ] = JsonFormat.marshalFrom( obj )

        result.isSuccess shouldBe true
        result.get shouldBe """{"name":"michael"}"""
    }

    "Json Format generic marshalling methods" should "unmarshal JSON into specified type" in {
        val json = """{"name":"michael"}"""

        val result : Try[ ObjectNode ] = JsonFormat.unmarshalTo[ ObjectNode ]( json, classOf[ ObjectNode ] )

        result.isSuccess shouldBe true
        result.get.get( "name" ).asText() shouldBe "michael"

    }

}
