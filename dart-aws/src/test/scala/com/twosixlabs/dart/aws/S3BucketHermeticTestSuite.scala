package com.twosixlabs.dart.aws

import better.files.{File, Resource}
import com.adobe.testing.s3mock.S3MockApplication
import com.twosixlabs.dart.test.base.StandardTestBase3x
import org.scalatest.BeforeAndAfterAll
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider

import java.net.URI
import scala.collection.JavaConverters._
import scala.util.{Failure, Random, Success}

class S3BucketHermeticTestSuite extends StandardTestBase3x with BeforeAndAfterAll {

    val TMP_DIR = "target/tmp"
    val S3_MOCK_BUCKET_NAME = "dart-commons-s3-test"
    val TARGET_DATA_DIR = s"target/${S3_MOCK_BUCKET_NAME}"
    val S3_MOCK_HTTP_PORT = "10080"
    val S3_MOCK_SERVER_ENDPOINT = new URI( s"http://localhost:${S3_MOCK_HTTP_PORT}" )

    val TEST_DATA_DIR = s"${System.getProperty( "user.dir" )}/dart-aws/src/test/resources/files"

    var s3Mock : S3MockApplication = new S3MockApplication()


    override def beforeAll( ) : Unit = {
        val props : Map[ String, AnyRef ] = Map( S3MockApplication.PROP_ROOT_DIRECTORY -> TARGET_DATA_DIR,
                                                 S3MockApplication.PROP_HTTP_PORT -> S3_MOCK_HTTP_PORT,
                                                 S3MockApplication.PROP_INITIAL_BUCKETS -> S3_MOCK_BUCKET_NAME )
        s3Mock = S3MockApplication.start( props.asJava )

        if ( !File( TMP_DIR ).exists() ) File( TMP_DIR ).createDirectory()
    }

    override def afterAll( ) : Unit = {
        s3Mock.stop()
        File( TARGET_DATA_DIR ).delete()
        File( TMP_DIR ).delete()
    }

    val s3Bucket = {
        System.setProperty( "aws.accessKeyId", "test" )
        System.setProperty( "aws.secretAccessKey", "test" )
        new S3Bucket( bucket = S3_MOCK_BUCKET_NAME, credentials = SystemPropertyCredentialsProvider.create(), tmpDir = TMP_DIR, endpoint = Some( S3_MOCK_SERVER_ENDPOINT ) )
    }

    "S3 Bucket" should "upload a small file" in {
        val content = Resource.getAsString( "files/small.txt" )

        s3Bucket.create( "small.txt", content.getBytes() ) match {
            case Success( filename ) => filename shouldBe s"s3://${S3_MOCK_BUCKET_NAME}/small.txt"
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        File( TMP_DIR ).list.size shouldBe 0
    }

    // TODO - temporarily disable this test until we can figure out the GitHub large file limit
    "S3 Bucket" should "upload a large file" ignore {
        val content = Resource.getAsString( "files/large.bin" )
        s3Bucket.create( "large.bin", content.getBytes() ) match {
            case Success( filename ) => filename shouldBe s"s3://${S3_MOCK_BUCKET_NAME}/large.bin"
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        File( TMP_DIR ).list.size shouldBe 0
    }

    "S3 Bucket" should "be able to retrieve a file that has been uploaded" in {
        val content = Resource.getAsString( "files/small.txt" )

        s3Bucket.create( "roundtrip.txt", content.getBytes() ) match {
            case Success( filename ) => filename shouldBe s"s3://${S3_MOCK_BUCKET_NAME}/roundtrip.txt"
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        val results : Option[ Array[ Byte ] ] = s3Bucket.get( "roundtrip.txt" ) match {
            case Success( bytes ) => bytes
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        results.isDefined shouldBe true
        "Hello World!" shouldBe new String( results.get )

        File( TMP_DIR ).list.size shouldBe 0

    }

    "S3 Bucket" should "be able to retrieve the list of files that have been uploaded" in {
        val content = Resource.getAsString( "files/small.txt" )

        val fileOne = s"test_${Math.abs( Random.nextInt )}"
        val fileTwo = s"test_${Math.abs( Random.nextInt )}"

        s3Bucket.create( fileOne, content.getBytes() ) match {
            case Success( filename ) => filename shouldBe s"s3://${S3_MOCK_BUCKET_NAME}/${fileOne}"
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        s3Bucket.create( fileTwo, content.getBytes() ) match {
            case Success( filename ) => filename shouldBe s"s3://${S3_MOCK_BUCKET_NAME}/${fileTwo}"
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        val results : Option[ Array[ String ] ] = s3Bucket.list() match {
            case Success( files ) => files
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        results.isDefined shouldBe true
        results.get should contain( fileOne )
        results.get should contain( fileTwo )

        File( TMP_DIR ).list.toList should not contain fileOne
        File( TMP_DIR ).list.toList should not contain fileTwo
    }

    "S3 Bucket" should "be able to retrieve the list of files with a prefix that have been uploaded" in {
        val content = Resource.getAsString( "files/small.txt" )

        val fileOne = s"with-filter-test_${Math.abs( Random.nextInt )}"
        val fileTwo = s"with-filter-test_${Math.abs( Random.nextInt )}"
        val fileThree = "other_file.txt"

        s3Bucket.create( fileOne, content.getBytes() ) match {
            case Success( filename ) => filename shouldBe s"s3://${S3_MOCK_BUCKET_NAME}/${fileOne}"
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        s3Bucket.create( fileTwo, content.getBytes() ) match {
            case Success( filename ) => filename shouldBe s"s3://${S3_MOCK_BUCKET_NAME}/${fileTwo}"
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        s3Bucket.create( fileThree, content.getBytes() ) match {
            case Success( filename ) => filename shouldBe s"s3://${S3_MOCK_BUCKET_NAME}/${fileThree}"
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        val results : Option[ Array[ String ] ] = s3Bucket.list( "with-filter-test" ) match {
            case Success( files ) => files
            case Failure( e : Throwable ) => {
                e.printStackTrace()
                fail( e )
            }
        }

        results.isDefined shouldBe true
        results.get.size shouldBe 2

        results.get should contain( fileOne )
        results.get should contain( fileTwo )
        results.get should not contain ( fileThree )

        File( TMP_DIR ).list.toList should not contain fileOne
        File( TMP_DIR ).list.toList should not contain fileTwo
        File( TMP_DIR ).list.toList should not contain fileThree
    }
}
