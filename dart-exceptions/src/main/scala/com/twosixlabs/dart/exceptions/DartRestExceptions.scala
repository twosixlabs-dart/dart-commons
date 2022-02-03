package com.twosixlabs.dart.exceptions

/**
  * DartRestExceptions should be used to pass messages to DartScalatraServlet.handleOutput, which will produce an appropriate
  * http message. Using these exceptions will remove the need for complex flow control to handle the multitude of cases that
  * should be handled by a REST action, and will allow you to design your action around successful cases.
  */
trait DartRestException extends DartException

trait BadRequestException extends DartRestException

class BadQueryParameterException( problem : String ) extends Exception( s"invalid query: ${problem}" ) with BadRequestException {
   def this( param : String, value : Option[ String ], correctFormat : String ) =
      this( s"parameter ${param}=${value.getOrElse("<EMPTY>")} does not conform to required format: ${correctFormat}" )

   def this( params : List[ String ], problem : Option[ String ] = None ) =
      this( (if (params.length > 1) s"parameters ${params.mkString(", ")} are invalid" else s"parameter ${params.headOption.getOrElse("")} is invalid") +
            (if (problem.isDefined) s": ${problem.get}" else "" ) )
}

class BadRequestBodyException( problem: String ) extends Exception( s"invalid request body: ${problem}" ) with BadRequestException {
   def this( field : String, value : Option[ String ], correctFormat : String ) =
      this( s"field ${field}=${value.getOrElse("<EMPTY>")} does not conform to required format: ${correctFormat}" )

   def this( fields : List[ String ], problem : Option[ String ] = None ) =
      this( (if (fields.length > 1) s"parameters ${fields.mkString(", ")} are invalid" else s"parameter ${fields.headOption.getOrElse("")} is invalid") +
            (if (problem.isDefined) s": ${problem.get}" else "" ) )
}

class AuthenticationException( problem : String ) extends Exception( s"unable to authenticate request: ${problem}" ) with BadRequestException


class ServiceUnreachableException( service: String, problem: Option[ String ] = None )
  extends Exception( s"unable to reach ${service}" + ( if (problem.isDefined) s": ${problem.get}" else "" ) ) with DartRestException

class ResourceNotFoundException( resourceType: String, resourceIdentifier: Option[ String ] = None )
  extends Exception( if (resourceIdentifier.isDefined) s"""${resourceType} "${resourceIdentifier.get}" does not exist""" else resourceType )
    with DartRestException

/**
  * This exception should be used for failures that do not correspond to any status codes more specific than 500
  * but still ought to pass some details to the user. All other exceptions should simply be passed to
  * DartScalatraServlet without being translated to a DartRestException, which will result in a 500 response
  * and an "Internal Server Error" message.
  *
  * If you throw this after catching another exception, you can pass that exception as 'cause' and DartScalatraServlet
  * will log it for you.
  *
  * @param problem
  * @param cause
  */
class GenericServerException( problem : String, cause : Throwable = null ) extends Exception( problem ) with DartRestException {
   override def getCause : Throwable = cause
}



