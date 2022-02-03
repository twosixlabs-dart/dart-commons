package com.twosixlabs.dart.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.util.Try

@deprecated( "see `com.twosixlabs.dart.json.JsonFormat` in the `dart-json` project" )
object JsonHelper {

    private val mapper : ObjectMapper = {
        val m = new ObjectMapper()
        m.registerModule( DefaultScalaModule )
        m.registerModule( new JavaTimeModule )
        m
    }

    def unmarshal[ A ]( json : String, valueType : Class[ A ] ) : Try[ A ] = Try {
                                                                                     mapper.readValue( json, valueType )
                                                                                 }

    def marshal( obj : Any ) : Try[ String ] = Try {
                                                       mapper.writeValueAsString( obj )
                                                   }

    def unmarshalStringMap( json : String ) : Option[ Map[ String, String ] ] = {
        if ( json == null || json.trim().length == 0 || json.trim() == "" ) return None
        else {
            try {
                Some( mapper.readValue( json, classOf[ Map[ String, String ] ] ) )
            } catch {
                case e : Exception => None
            }
        }
    }

    def marshalStringMap( mapIn : Map[ String, String ] ) : Option[ String ] = {
        if ( mapIn == null || mapIn.isEmpty ) return None
        else {
            try {
                Some( mapper.writeValueAsString( mapIn ) )
            } catch {
                case e : Exception => None
            }
        }
    }


}
