package com.twosixlabs.dart.aws

import java.net.URI
import java.time.Instant
import java.util.concurrent.CompletableFuture

import org.slf4j.{Logger, LoggerFactory}
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.ec2.Ec2AsyncClient
import software.amazon.awssdk.services.ec2.model.{ActivityStatus, BatchState, CancelSpotFleetRequestsRequest, CancelSpotFleetRequestsResponse, CancelSpotInstanceRequestsRequest, CancelSpotInstanceRequestsResponse, CreateTagsRequest, CreateTagsResponse, DescribeInstancesRequest, DescribeInstancesResponse, DescribeSecurityGroupsRequest, DescribeSecurityGroupsResponse, DescribeSpotFleetInstancesRequest, DescribeSpotFleetInstancesResponse, DescribeSpotFleetRequestsRequest, DescribeSpotFleetRequestsResponse, DescribeSpotInstanceRequestsRequest, DescribeSpotInstanceRequestsResponse, Filter, FleetType, GroupIdentifier, Instance, InstanceNetworkInterfaceSpecification, InstanceType, RebootInstancesRequest, RebootInstancesResponse, RequestSpotFleetRequest, RequestSpotFleetResponse, RequestSpotInstancesRequest, RequestSpotInstancesResponse, RequestSpotLaunchSpecification, Reservation, ResourceType, RunInstancesRequest, RunInstancesResponse, SpotFleetLaunchSpecification, SpotFleetRequestConfig, SpotFleetRequestConfigData, StartInstancesRequest, StartInstancesResponse, StopInstancesRequest, StopInstancesResponse, Tag, TagSpecification, TerminateInstancesRequest, TerminateInstancesResponse}
import software.amazon.awssdk.services.sts.StsAsyncClient
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import scala.util.{Failure, Success, Try}


class EC2AsyncClient( credentials : AwsCredentialsProvider, endpoint : Option[ URI ] = None, region : Region = Region.US_EAST_1 ) {
    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    private val ec2 : Ec2AsyncClient = {
        if ( endpoint.isEmpty ) Ec2AsyncClient.builder().credentialsProvider( credentials ).region( region ).build()
        else Ec2AsyncClient.builder().credentialsProvider( credentials ).region( region ).endpointOverride( endpoint.get ).build()
    }

    private val sts : StsAsyncClient = {
        if ( endpoint.isEmpty ) StsAsyncClient.builder().credentialsProvider( credentials ).region( region ).build()
        else  StsAsyncClient.builder().credentialsProvider( credentials ).region( region ).endpointOverride( endpoint.get ).build()
    }

    def searchInstances( filters : List[ ( String, String ) ],
                         nextToken : String = "" ) : CompletableFuture[ DescribeInstancesResponse ]  = {
        val request : DescribeInstancesRequest.Builder = DescribeInstancesRequest.builder()
            .filters( filters.map( kv =>  Filter.builder().name( kv._1 ).values( kv._2 ).build() ).asJava  );
        if ( !nextToken.isEmpty() ) {
            request.nextToken( nextToken );
        }
        ec2.describeInstances( request.build() );
    }

    def searchSpotRequests( filters : List[ ( String, String ) ],
                        nextToken : String = "" ) : CompletableFuture[ DescribeSpotInstanceRequestsResponse ]  = {
        val request = DescribeSpotInstanceRequestsRequest.builder()
          .filters( filters.map( kv =>  Filter.builder().name( kv._1 ).values( kv._2 ).build() ).asJava  )
        if ( !nextToken.isEmpty ) {
            request.nextToken( nextToken )
        }
        ec2.describeSpotInstanceRequests( request.build() )
    }

    def searchSpotFleets( tags : List[ ( String, String ) ],
                          nextToken : String = "" ) : List[ (SpotFleetRequestConfig, DescribeSpotFleetInstancesResponse) ]  = {
        val request = DescribeSpotFleetRequestsRequest.builder()

        ec2.describeSpotFleetRequests( request.build() ).get.spotFleetRequestConfigs().asScala.toList.filter { sfrc =>
            sfrc.tags().asScala.map( t => (t.key(), t.value()) ).toSet.equals( tags.toSet )
        } map { sfrc =>
            val id = sfrc.spotFleetRequestId()
            val instances = ec2.describeSpotFleetInstances( DescribeSpotFleetInstancesRequest.builder().spotFleetRequestId( id ).build() ).get
            (sfrc, instances)
        }
    }

    def searchSecurityGroups( filters : List[ ( String, String ) ],
                              nextToken : String = "" ) : CompletableFuture[ DescribeSecurityGroupsResponse ] = {
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
        val describeRequest : DescribeSpotInstanceRequestsRequest = DescribeSpotInstanceRequestsRequest.builder()
            .spotInstanceRequestIds( requestId )
            .build();
        awaitRequest( ec2.describeSpotInstanceRequests( describeRequest ) ) match {
            case Failure( exception ) => Failure( exception );
            case Success( describeResult ) => Try(
                describeResult
                  .spotInstanceRequests.asScala.headOption
                  .getOrElse( throw new Exception( s"Describe result for spot instance request describe request ${requestId} has no requests" ) )
                  .stateAsString
            );
        }
    }

    def getSpotInstanceFromRequest( requestId : String ) : Try[ String ] = {
        val describeRequest : DescribeSpotInstanceRequestsRequest = DescribeSpotInstanceRequestsRequest.builder()
            .spotInstanceRequestIds( requestId )
            .build();
        awaitRequest( ec2.describeSpotInstanceRequests( describeRequest ) ) match {
            case Failure( exception ) => Failure( exception );
            case Success( describeResult ) => Try(
                describeResult
                  .spotInstanceRequests.asScala.headOption
                  .getOrElse( throw new Exception( s"Describe result for spot instance describe request ${requestId} has no spot instances" ) )
                  .instanceId
            );
        }
    }

    def getInstanceState( instanceId : String ) : Try[ String ] = {
        val request : DescribeInstancesRequest.Builder = DescribeInstancesRequest.builder()
            .instanceIds( instanceId );
        awaitRequest( ec2.describeInstances( request.build() ) ) match {
            case Failure( exception ) => Failure( exception );
            case Success( describeResult : DescribeInstancesResponse ) => Try(
                describeResult
                  .reservations.asScala.headOption.getOrElse( throw new Exception( s"Describe result for instance ${instanceId} has no reservations" ) )
                  .instances.asScala.headOption.getOrElse( throw new Exception( s"Describe result for instance ${instanceId} has no instances" ) )
                  .state.nameAsString
            );
        }
    }

    def getInstanceState( instance : Instance ) : Try[ String ] = {
        val request : DescribeInstancesRequest.Builder = DescribeInstancesRequest.builder()
            .instanceIds( instance.instanceId );
        awaitRequest( ec2.describeInstances( request.build() ) ) match {
            case Failure( exception ) => Failure( exception );
            case Success( describeResult ) => Try(
                describeResult
                  .reservations.asScala.headOption.getOrElse( throw new Exception( s"Describe result for instance ${instance.instanceId} has no reservations" ) )
                  .instances.asScala.headOption.getOrElse( throw new Exception( s"Describe result for instance ${instance.instanceId} has no instances" ) )
                  .state.nameAsString
            );
        }
    }

    def createInstance( name : String,
                        amiId : String,
                        instanceType : String,
                        securityGroups : List[ String ] = Nil,
                        userData : Option[ String ] = None ) : CompletableFuture[ RunInstancesResponse ] = {
        val runRequest : RunInstancesRequest.Builder = RunInstancesRequest.builder()
             .imageId( amiId )
             .instanceType( InstanceType.fromValue( instanceType ) )
             .maxCount( 1 )
             .minCount( 1 );
        if ( securityGroups.nonEmpty ) {
            runRequest.securityGroupIds( securityGroups.asJava );
        }
        userData foreach( runRequest.userData )
        ec2.runInstances( runRequest.build() );
    }

    def createSpotInstance( amiId : String,
                            instanceType : String,
                            securityGroups : List[ String ],
                            subnetId : String,
                            blockDurationMinutes : Option[ Int ] = Some( 120 ),
                            tags : List[ (String, String) ] = Nil,
                            userData : Option[ String ] = None ) : CompletableFuture[ RequestSpotInstancesResponse ] = {
        val instanceNetworkInterfaceSpecification = InstanceNetworkInterfaceSpecification.builder()
            .deviceIndex( 0 )
            .groups( securityGroups.asJava )
            .subnetId( subnetId )
            .build()
        val launchSpecBuilder = RequestSpotLaunchSpecification.builder()
            .imageId( amiId )
            .instanceType( InstanceType.fromValue( instanceType ) )
            .networkInterfaces( instanceNetworkInterfaceSpecification )
        userData foreach( launchSpecBuilder.userData )
        val launchSpec : RequestSpotLaunchSpecification = launchSpecBuilder.build()
        val tagSpec = TagSpecification.builder()
          .resourceType( ResourceType.SPOT_INSTANCES_REQUEST )
          .tags( tags.map( t => Tag.builder().key(t._1).value(t._2).build() ).asJava )
          .build()
        val requestBuilder = RequestSpotInstancesRequest.builder()
            .instanceCount( 1 )
            .launchSpecification( launchSpec )
            .tagSpecifications( tagSpec )
        blockDurationMinutes.foreach( bd => requestBuilder.blockDurationMinutes( bd ) )
        ec2.requestSpotInstances( requestBuilder.build() );
    }

    def createSpotFleet( amiId : String,
                         instanceTypes : List[ String ],
                         securityGroups : List[ String ],
                         subnetIds : List[ String ],
                         durationMinutes : Option[ Int ] = None,
                         allocationStrategy : String = "lowestPrice",
                         tags : List[ (String, String) ] = Nil,
                         targetCapacity : Int = 1,
                         userData : Option[ String ] = None ) : CompletableFuture[ RequestSpotFleetResponse ] = {

        val accountId = sts.getCallerIdentity( GetCallerIdentityRequest.builder().build() ).get.account()
        val iamRole = s"arn:aws:iam::${accountId}:role/aws-ec2-spot-fleet-tagging-role"

        val launchSpecifications = for {
            instanceType <- instanceTypes
            subnetId <- subnetIds
        } yield {
            val builder = SpotFleetLaunchSpecification.builder()
              .imageId( amiId )
              .securityGroups( securityGroups.map( sg => GroupIdentifier.builder().groupId( sg ).build() ).asJava )
              .instanceType( instanceType )
              .subnetId( subnetId )
            userData.foreach( builder.userData )
            builder.build()
        }
        val requestConfigBuilder = SpotFleetRequestConfigData.builder()
          .targetCapacity( targetCapacity )
          .allocationStrategy( allocationStrategy )
          .iamFleetRole( iamRole )
          .tagSpecifications( TagSpecification.builder()
                                .tags( tags.map( tt => Tag.builder().key(tt._1).value(tt._2).build() ).asJava )
                                .resourceType( ResourceType.SPOT_FLEET_REQUEST ).build() )
          .launchSpecifications( launchSpecifications.asJava )
          .terminateInstancesWithExpiration( true )
          .`type`( FleetType.MAINTAIN )
        durationMinutes.foreach( d => requestConfigBuilder.validUntil( Instant.now().plusSeconds( d.toLong * 60L ) ) )
        val request = RequestSpotFleetRequest.builder()
          .spotFleetRequestConfig( requestConfigBuilder.build() ).build()
        ec2.requestSpotFleet( request )
    }

    def cancelSpotRequest( requestId : String ) : CompletableFuture[ CancelSpotInstanceRequestsResponse ] = {
        val cancelRequest : CancelSpotInstanceRequestsRequest.Builder = CancelSpotInstanceRequestsRequest.builder()
            .spotInstanceRequestIds( requestId )
        ec2.cancelSpotInstanceRequests( cancelRequest.build() )
    }

    def cancelSpotFleetRequest( requestId : String, terminateInstances : Boolean = true ) : CompletableFuture[ CancelSpotFleetRequestsResponse ] = {
        val cancelRequest : CancelSpotFleetRequestsRequest.Builder = CancelSpotFleetRequestsRequest.builder()
          .spotFleetRequestIds( requestId )
          .terminateInstances( terminateInstances )
        ec2.cancelSpotFleetRequests( cancelRequest.build() )
    }

    def startInstance( instanceId : String ) : CompletableFuture[ StartInstancesResponse ] = {
        val request : StartInstancesRequest = StartInstancesRequest.builder()
            .instanceIds( instanceId )
            .build();
        ec2.startInstances( request );
    }

    def rebootInstance( instanceId : String ) : CompletableFuture[ RebootInstancesResponse ] = {
        val request : RebootInstancesRequest = RebootInstancesRequest.builder()
            .instanceIds( instanceId )
            .build();
        ec2.rebootInstances( request );
    }

    def stopInstance( instanceId : String ) : CompletableFuture[ StopInstancesResponse ] = {
        val request : StopInstancesRequest = StopInstancesRequest.builder()
            .instanceIds( instanceId )
            .build();
        ec2.stopInstances( request );
    }

    def terminateInstance( instanceId : String ) : CompletableFuture[ TerminateInstancesResponse ] = {
        val request : TerminateInstancesRequest = TerminateInstancesRequest.builder()
            .instanceIds( instanceId )
            .build();
        ec2.terminateInstances( request );
    }

    def tagInstance( instanceId : String, tags : List[ ( String, String ) ] ) : CompletableFuture[ CreateTagsResponse ] = {
        val tagRequest : CreateTagsRequest = CreateTagsRequest.builder()
            .resources( instanceId )
            .tags( tags.map( kv => Tag.builder().key( kv._1 ).value( kv._2 ).build() ).asJava )
            .build();
        ec2.createTags( tagRequest );
    }

    def awaitRequest[ R ]( request: CompletableFuture[ R ], sleepMs : Int = 1000, maxRetries : Int = 10 ) : Try[ R ] = {
        Try {
            for ( i <- 1 to maxRetries ) {
                if ( request.isDone() ) {
                    return Success( request.get );
                }
                Thread.sleep( sleepMs );
            }
            throw new Exception( "Request not done!" );
        }
    }
}
