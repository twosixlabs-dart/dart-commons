package com.twosixlabs.dart.aws

//@formatter:off
import java.net.URI
import java.nio.ByteBuffer
import java.nio.file.Paths

import better.files.File
import org.slf4j.{Logger, LoggerFactory}
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{CompleteMultipartUploadRequest, CompletedMultipartUpload, CompletedPart, CreateMultipartUploadRequest, GetObjectRequest, ListObjectsRequest, ListObjectsResponse, PutObjectRequest, UploadPartRequest}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}
//@formatter:on

class S3Bucket( bucket : String, credentials : AwsCredentialsProvider, tmpDir : String = "/tmp", endpoint : Option[ URI ] = None ) {
    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    private val MULTIPART_CUTOFF : Long = 100 * 1024 * 1024

    private val region : Region = Region.US_EAST_1
    private val s3 : S3Client = {
        if ( endpoint.isEmpty ) S3Client.builder().credentialsProvider( credentials ).region( region ).build()
        else S3Client.builder().credentialsProvider( credentials ).region( region ).endpointOverride( endpoint.get ).build()
    }

    def create( filename : String, file : Array[ Byte ] ) : Try[ String ] = {
        val tmpFile = File( s"${tmpDir}/${filename}" )
        tmpFile.outputStream.foreach( tempFile => tempFile.write(file) )
        val result : Try[ String ] = {
            if ( tmpFile.size < MULTIPART_CUTOFF ) Try( doSmallFileUpload( filename, tmpFile ) )
            else Try( doMultipartUpload( filename, tmpFile ) )
        }
        tmpFile.delete()
        result
    }

    def get( filename : String ) : Try[ Option[ Array[ Byte ] ] ] = {
        try {
            val request = GetObjectRequest.builder().bucket( bucket ).key( filename ).build()
            val tmpFilename : String = s"${tmpDir}/${System.currentTimeMillis()}"
            s3.getObject( request, Paths.get( tmpFilename ) )
            val tmpFile = File( tmpFilename )
            val bytes = tmpFile.byteArray
            tmpFile.delete()
            Success( Some( bytes ) )
        } catch {
            case awse : AwsServiceException => {
                awse statusCode match {
                    case 404 => Success( None )
                    case _ => {
                        LOG.error( s"Caught AWS Service exception ${awse.awsErrorDetails}" )
                        Failure( awse )
                    }
                }
            }
            case e : Exception => {
                LOG.error( s"unexpected exception encountered!!! ${e.getClass.getName} :: ${e.getMessage} :: ${e.getCause}" )
                Failure( e )
            }
        }
    }

    def list( prefix : String = "" ) : Try[ Option[ Array[ String ] ] ] = {
        try {
            val request = ListObjectsRequest.builder().bucket( bucket ).prefix( prefix ).build()
            val objectList : ListObjectsResponse = s3.listObjects( request )
            val keys = for ( o <- objectList.contents.asScala ) yield o.key
            Success( Some( keys.toArray ) )
        } catch {
            case awse : AwsServiceException => {
                awse statusCode match {
                    case 404 => Success( None )
                    case _ => {
                        LOG.error( s"Caught AWS Service exception ${awse.awsErrorDetails}" )
                        Failure( awse )
                    }
                }
            }
            case e : Exception => {
                LOG.error( s"unexpected exception encountered!!! ${e.getClass.getName} :: ${e.getMessage} :: ${e.getCause}" )
                Failure( e )
            }
        }
    }

    @throws( classOf[ AwsServiceException ] )
    private def doSmallFileUpload( filename : String, file : File ) : String = {
        val request = PutObjectRequest.builder().bucket( bucket ).key( filename ).build()
        val response = s3.putObject( request, RequestBody.fromBytes( file.byteArray ) )
        LOG.debug( s"successfully created s3 object ${filename} - ${bucket} : etag=${response.eTag}" )
        s"s3://${bucket}/${filename}"
    }

    @throws( classOf[ AwsServiceException ] )
    private def doMultipartUpload( filename : String, file : File ) : String = {
        val multipartRequest = CreateMultipartUploadRequest.builder().bucket( bucket ).key( filename ).build()
        val multipartResponse = s3.createMultipartUpload( multipartRequest )
        val uid = multipartResponse.uploadId()
        var part = 0
        val completedParts : Seq[ CompletedPart ] = file.byteArray.grouped( 5 * 1024 * 1024 ).map( bytes => {
            part = part + 1
            val response = s3.uploadPart( UploadPartRequest
                                            .builder()
                                            .bucket( bucket )
                                            .key( filename )
                                            .uploadId( uid )
                                            .partNumber( part )
                                            .build(), RequestBody.fromByteBuffer( ByteBuffer.wrap( bytes ) ) )

            CompletedPart.builder().partNumber( part ).eTag( response.eTag() ).build()
        } ).toSeq

        val completedMultiUpload = CompletedMultipartUpload.builder().parts( completedParts.asJava ).build()

        val completedMultipartUploadRequest = CompleteMultipartUploadRequest
          .builder()
          .bucket( bucket )
          .key( filename )
          .uploadId( uid )
          .multipartUpload( completedMultiUpload )
          .build()

        val multipartUploadResponse = s3.completeMultipartUpload( completedMultipartUploadRequest )

        LOG.debug( s"successfully created multipart s3 object ${filename} - ${bucket} : etag=${multipartUploadResponse.eTag}" )
        s"s3://${bucket}/${filename}"
    }
}
