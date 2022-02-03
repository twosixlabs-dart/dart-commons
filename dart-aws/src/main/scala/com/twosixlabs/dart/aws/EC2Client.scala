package com.twosixlabs.dart.aws

import java.net.URI

import org.slf4j.{Logger, LoggerFactory}
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.Ec2Client
import software.amazon.awssdk.services.ec2.model.{CancelSpotInstanceRequestsRequest, CancelSpotInstanceRequestsResponse}
import software.amazon.awssdk.services.ec2.model.{CreateTagsRequest, CreateTagsResponse}
import software.amazon.awssdk.services.ec2.model.{DescribeSpotInstanceRequestsRequest, DescribeSpotInstanceRequestsResponse}
import software.amazon.awssdk.services.ec2.model.{DescribeInstancesRequest, DescribeInstancesResponse}
import software.amazon.awssdk.services.ec2.model.{DescribeSecurityGroupsRequest, DescribeSecurityGroupsResponse}
import software.amazon.awssdk.services.ec2.model.{RebootInstancesRequest, RebootInstancesResponse}
import software.amazon.awssdk.services.ec2.model.{RequestSpotInstancesRequest, RequestSpotInstancesResponse, SpotInstanceRequest}
import software.amazon.awssdk.services.ec2.model.{RunInstancesRequest, RunInstancesResponse}
import software.amazon.awssdk.services.ec2.model.{StartInstancesRequest, StartInstancesResponse}
import software.amazon.awssdk.services.ec2.model.{StopInstancesRequest, StopInstancesResponse}
import software.amazon.awssdk.services.ec2.model.{TerminateInstancesRequest, TerminateInstancesResponse}
import software.amazon.awssdk.services.ec2.model.{Ec2Exception, Filter, Instance, InstanceNetworkInterfaceSpecification, InstanceType, RequestSpotLaunchSpecification, Reservation, Tag, TagSpecification}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}
import scala.concurrent.java8.FuturesConvertersImpl.P

class EC2Client( credentials : AwsCredentialsProvider, endpoint : Option[ URI ] = None, region : Region = Region.US_EAST_1 ) {
    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    private val ec2 : Ec2Client = {
        if ( endpoint.isEmpty ) Ec2Client.builder().credentialsProvider( credentials ).region( region ).build()
        else Ec2Client.builder().credentialsProvider( credentials ).region( region ).endpointOverride( endpoint.get ).build()
    }

    def searchInstances( filters : List[ ( String, String ) ],
                         nextToken : String = "" ) : DescribeInstancesResponse = {
        val request : DescribeInstancesRequest.Builder = DescribeInstancesRequest.builder()
            .filters( filters.map( kv =>  Filter.builder().name( kv._1 ).values( kv._2 ).build() ).asJava  )
        if ( !nextToken.isEmpty() ) {
            request.nextToken( nextToken );
        }
        ec2.describeInstances( request.build() );
    }

    def searchSecurityGroups( filters : List[ ( String, String ) ],
                              nextToken : String = "" ) : DescribeSecurityGroupsResponse = {
        val request : DescribeSecurityGroupsRequest.Builder = DescribeSecurityGroupsRequest.builder()
            .filters( filters.map( kv =>  Filter.builder().name( kv._1 ).values( kv._2 ).build() ).asJava  );
        if ( !nextToken.isEmpty() ) {
            request.nextToken( nextToken );
        }
        ec2.describeSecurityGroups( request.build() );
    }

    def getInstancesFromDescribeResponse( response : DescribeInstancesResponse ) : Array[ Instance ] = {
        val instances = new ArrayBuffer[ Instance ]();
        for ( reservation : Reservation <- response.reservations.asScala ) {
            instances ++= reservation.instances.asScala;
        }
        instances.toArray;
    }

    def getSpotRequestState( requestId : String ) : Try[ String ] = {
        Try {
            val describeRequest : DescribeSpotInstanceRequestsRequest = DescribeSpotInstanceRequestsRequest.builder()
                .spotInstanceRequestIds( requestId )
                .build();
            val describeResult : DescribeSpotInstanceRequestsResponse = ec2.describeSpotInstanceRequests( describeRequest );
            describeResult.spotInstanceRequests.asScala( 0 ).stateAsString;
        }
    }

    def getSpotInstanceFromRequest( requestId : String ) : Try[ String ] = {
        Try {
            val describeRequest : DescribeSpotInstanceRequestsRequest = DescribeSpotInstanceRequestsRequest.builder()
                .spotInstanceRequestIds( requestId )
                .build();
            val describeResult : DescribeSpotInstanceRequestsResponse = ec2.describeSpotInstanceRequests( describeRequest )
            describeResult.spotInstanceRequests.asScala( 0 ).instanceId;
        }
    }

    def getInstanceState( instanceId : String ) : Try[ String ] = {
        Try {
            val request : DescribeInstancesRequest.Builder = DescribeInstancesRequest.builder()
                .instanceIds( instanceId );
            ec2.describeInstances( request.build() ).reservations.asScala( 0 ).instances.asScala( 0 ).state.nameAsString;
        }
    }

    def getInstanceState( instance : Instance ) : Try[ String ] = {
        Try {
            val request : DescribeInstancesRequest.Builder = DescribeInstancesRequest.builder()
                .instanceIds( instance.instanceId );
            ec2.describeInstances( request.build() ).reservations.asScala( 0 ).instances.asScala( 0 ).state.nameAsString;
        }
    }

    def createInstance( name : String,
                        amiId : String,
                        instanceType : String,
                        securityGroups : List[ String ] = Nil ) : RunInstancesResponse = {
        val runRequest : RunInstancesRequest.Builder = RunInstancesRequest.builder()
             .imageId( amiId )
             .instanceType( InstanceType.fromValue( instanceType ) )
             .maxCount( 1 )
             .minCount( 1 );
        if ( securityGroups != Nil ) {
            runRequest.securityGroupIds( securityGroups.asJava );
        }
        ec2.runInstances( runRequest.build() );
    }

    def createSpotInstance( amiId : String,
                            instanceType : String,
                            securityGroups : List[ String ],
                            subnetId : String,
                            blockDuration : Int = 120 ) : RequestSpotInstancesResponse = {
        val instanceNetworkInterfaceSpecification = InstanceNetworkInterfaceSpecification.builder()
            .deviceIndex( 0 )
            .groups( securityGroups.asJava )
            .subnetId( subnetId )
            .build()
        val launchSpec : RequestSpotLaunchSpecification = RequestSpotLaunchSpecification.builder()
            .imageId( amiId )
            .instanceType( InstanceType.fromValue( instanceType ) )
            .networkInterfaces( instanceNetworkInterfaceSpecification )
            .build();
        val requestRequest : RequestSpotInstancesRequest = RequestSpotInstancesRequest.builder()
            .instanceCount( 1 )
            .launchSpecification( launchSpec )
            .blockDurationMinutes( blockDuration )
            .build();
        ec2.requestSpotInstances( requestRequest );
    }

    def cancelSpotRequest( requestId : String ) : CancelSpotInstanceRequestsResponse = {
        val cancelRequest : CancelSpotInstanceRequestsRequest.Builder = CancelSpotInstanceRequestsRequest.builder()
            .spotInstanceRequestIds( requestId )
        ec2.cancelSpotInstanceRequests( cancelRequest.build() )
    }

    def startInstance( instanceId : String ) : StartInstancesResponse = {
        val request : StartInstancesRequest = StartInstancesRequest.builder()
            .instanceIds( instanceId )
            .build();
        ec2.startInstances( request );
    }

    def rebootInstance( instanceId : String ) : RebootInstancesResponse = {
        val request : RebootInstancesRequest = RebootInstancesRequest.builder()
            .instanceIds( instanceId )
            .build();
        ec2.rebootInstances( request );
    }

    def stopInstance( instanceId : String ) : StopInstancesResponse = {
        val request : StopInstancesRequest = StopInstancesRequest.builder()
            .instanceIds( instanceId )
            .build();
        ec2.stopInstances( request );
    }

    def terminateInstance( instanceId : String ) : TerminateInstancesResponse = {
        val request : TerminateInstancesRequest = TerminateInstancesRequest.builder()
            .instanceIds( instanceId )
            .build();
        ec2.terminateInstances( request );
    }

    def tagInstance( instanceId : String, tags : List[ ( String, String ) ] ) : CreateTagsResponse = {
        val tagRequest : CreateTagsRequest = CreateTagsRequest.builder()
            .resources( instanceId )
            .tags( tags.map( kv => Tag.builder().key( kv._1 ).value( kv._2 ).build() ).asJava )
            .build();
        ec2.createTags( tagRequest );
    }
}
