package com.twosixlabs.dart.commons.config

import com.twosixlabs.dart.test.base.StandardTestBase3x

import java.util.Properties

class StandardCliConfigTestSuite extends StandardTestBase3x with StandardCliConfig {

    private val TEST_PROP_NAME : String = "my.prop"
    private val FILE_PATH_PREFIX : String = s"${System.getProperty( "user.dir" )}/dart-cli/src/test/resources/env"


    "Standard CLI Config" should "process the default environmental configuration when nothing is specifed" in {
        val args : Array[ String ] = Array()
        val props : Properties = processConfig( args )

        props.getProperty( TEST_PROP_NAME ) shouldBe "default"
    }

    "Standard CLI Config" should "process the supplied internal environmental configuration" in {
        val args : Array[ String ] = Array( "--env", "internal" )
        val props : Properties = processConfig( args )

        props.getProperty( TEST_PROP_NAME ) shouldBe "internal"
    }

    "Standard CLI Config" should "load the specified external file by path" in {
        val args : Array[ String ] = Array( "--config", s"${FILE_PATH_PREFIX}/file.conf" )
        val props : Properties = processConfig( args )

        props.getProperty( TEST_PROP_NAME ) shouldBe "file"

    }

    "Standard CLI Config" should "prefer the external environmental configuration even if the --env property is set by accident" in {
        val args : Array[ String ] = Array( "--env", "internal", "--config", s"${FILE_PATH_PREFIX}/file.conf" )
        val props : Properties = processConfig( args )

        props.getProperty( TEST_PROP_NAME ) shouldBe "file"
    }

    //@formatter:off
    "Standard CLI Config" should "fail for a missing external file" in {
        val thrown = intercept[DartConfigException] {
            val args : Array[ String ] = Array( "--config", s"${FILE_PATH_PREFIX}/missing.conf" )
            processConfig( args )
        }
        thrown.getMessage shouldBe "The configuration/environment file does not exist"
    }
    //@formatter:on

    //@formatter:off
    "Standard CLI Config" should "fail for a non existent environment" in {
        val thrown = intercept[DartConfigException] {
            val args : Array[ String ] = Array( "--env", s"missing" )
            processConfig( args )
        }
        thrown.getMessage shouldBe "The configuration/environment file does not exist"
    }
    //@formatter:on

    //
    // TODO: This test does not work, as it requires an environment variable
    // set by SBT. However, this in turn requires us to activate forking for
    // tests, which breaks  other tests. If you want to manually check this,
    // uncomment and set MY_PROP=environment within your environment.
    //
    // "Standard CLI Config" should "load the configuration property from the environment" in {
    //     val args : Array[ String ] = Array( "--env", "env" )
    //     val props : Properties = processConfig( args )

    //     props.getProperty( TEST_PROP_NAME ) shouldBe "environment"
    // }

    //@formatter:off
    "Standard CLI Config" should "fail for a missing environment variable property" in {
        val thrown = intercept[ DartConfigException ] {
            val args : Array[ String ] = Array( "--env", "envbad" )
            processConfig( args )
        }
        thrown.getMessage shouldBe "The environment variable does not exist - my.bad.prop"
    }
    //@formatter:on

    "Standard CLI Config" should "load JVM props with the default environmental configuration when nothing is specifed" in {
        val args : Array[ String ] = Array()
        loadEnvironment( args )

        System.getProperty( TEST_PROP_NAME ) shouldBe "default"
    }

    "Standard CLI Config" should "load the JVM opts with the supplied internal environmental configuration" in {
        val args : Array[ String ] = Array( "--env", "internal" )
        loadEnvironment( args )

        System.getProperty( TEST_PROP_NAME ) shouldBe "internal"
    }

    "Standard CLI Config" should "load the JVM opts with the specified external file by path" in {
        val args : Array[ String ] = Array( "--config", s"${FILE_PATH_PREFIX}/file.conf" )
        loadEnvironment( args )

        System.getProperty( TEST_PROP_NAME ) shouldBe "file"

    }

    "Standard CLI Config" should "load the JVM opts and prefer the external environmental configuration even if the --env property is set by accident" in {
        val args : Array[ String ] = Array( "--env", "internal", "--config", s"${FILE_PATH_PREFIX}/file.conf" )
        loadEnvironment( args )

        System.getProperty( TEST_PROP_NAME ) shouldBe "file"
    }

    //@formatter:off
    "Standard CLI Config" should "fail for a missing external file when loading JVM properties" in {
        val thrown = intercept[DartConfigException] {
            val args : Array[ String ] = Array( "--config", s"${FILE_PATH_PREFIX}/missing.conf" )
            loadEnvironment( args )
        }
        thrown.getMessage shouldBe "The configuration/environment file does not exist"
    }
    //@formatter:on

    //@formatter:off
    "Standard CLI Config" should "fail for a non existent environment loading JVM properties" in {
        val thrown = intercept[DartConfigException] {
            val args : Array[ String ] = Array( "--env", s"missing" )
            loadEnvironment( args )
        }
        thrown.getMessage shouldBe "The configuration/environment file does not exist"
    }
    //@formatter:on


}
