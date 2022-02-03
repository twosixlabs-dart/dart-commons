package com.twosixlabs.dart.json

import com.fasterxml.jackson.core.{JsonParseException, JsonProcessingException}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

object JsonFormat {

    val MAPPER : ObjectMapper = {
        val m = new ObjectMapper()
        m.registerModule( DefaultScalaModule )
        m.registerModule( new JavaTimeModule )
    }

    def marshalFrom( obj : Any ) : Try[ String ] = {
        try Success( MAPPER.writeValueAsString( obj ) )
        catch {
            case e : JsonProcessingException => Failure( e )
            case e : MismatchedInputException => Failure( e )
        }
    }

    def unmarshalTo[ T: ClassTag ]( json : String, clazz : Class[ T ] ) : Try[ T ] = {
        try Success( MAPPER.readValue( json, clazz ) )
        catch {
            case e : JsonParseException => Failure( e )
            case e : MismatchedInputException => Failure( e )
        }
    }

}

trait JsonFormat {

    protected def objectMapper : ObjectMapper = JsonFormat.MAPPER

    /**
      *
      * Utility method that is very useful for mapping collections
      *
      * @param list       - the list of From elements
      * @param conversion - the conversion function, defined in the format implementation
      * @tparam From - the source type
      * @tparam To   - the target type
      * @return
      */
    protected def convertList[ From, To ]( list : List[ From ], conversion : From => To ) : List[ To ] = {
        if ( list == null ) List.empty
        else {
            val result : List[ To ] = list.map( conversion )
            if ( result.count( elem => elem != null && elem != None ) == 0 ) List.empty
            else result
        }
    }
}
