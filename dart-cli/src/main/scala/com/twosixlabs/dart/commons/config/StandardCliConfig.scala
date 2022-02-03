package com.twosixlabs.dart.commons.config

import java.util.Properties

import better.files.{File, Resource}
import org.slf4j.{Logger, LoggerFactory}
import scopt.OParser

import scala.collection.JavaConverters._

sealed case class DefaultConfig( environment : String = "default", configFile : Option[ String ] = None ) {}

/**
  *
  * This class represents a standard way to congfigure a microservice like application.
  *
  * All properties are specified in typical bash style <code>my.prop=value</code> in a file. You can choose between loading a file via
  * configuration that is embedded in the classpath of the application OR you can specify a specific file on the file system. If you
  * specify an external file it will take precedence over any internal configuration.
  *
  * The embedded configurations are stored in a standard directory src/{main,test}/resources/env and must be the name of the environment (ie: dev, test, aws, etc...)
  * with a .conf extensions
  *
  */
trait StandardCliConfig extends CliConfig {

    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    def processConfig( args : Array[ String ] ) : Properties = {
        val config : Option[ DefaultConfig ] = processCliArgs( args )
        if ( config.isEmpty ) throw new DartConfigException( "The supplied CLI arguments are invalid" )
        loadConfig( config.get )
    }

    private def processCliArgs( args : Array[ String ] ) : Option[ DefaultConfig ] = {
        //@formatter:off
        val builder = OParser.builder[ DefaultConfig ]
        val parser = {
            import builder._
            OParser.sequence(
                note( "--env will use an internala configuration file, use --conf to specify an external config file" ),
                opt[ String ]( "env" )
                    .optional
                    .valueName( "name of environmental config" )
                    .action( ( env, config ) => config.copy( environment = env  ) ),
                opt[ Option[ String ] ]( "config" )
                  .optional
                  .text("if specified, this setting will override the <env> setting")
                  .valueName( "external config file to use" )
                  .action( ( conf, config ) => config.copy( configFile = conf  ) )
            )
        }
        //@formatter:on
        OParser.parse( parser, args, DefaultConfig() ) match {
            case Some( config : DefaultConfig ) => Some( config )
            case _ => {
                LOG.error( "Configuration was invalid... exiting" )
                None
            }

        }
    }

    private def loadConfig( config : DefaultConfig ) : Properties = {
        val props : Properties = new Properties()
        try {
            if ( config.configFile.isDefined ) {
                val cfg : File = File( config.configFile.get )
                props.load( cfg.newFileInputStream )
            }
            else {
                props.load( Resource.getAsStream( s"env/${config.environment}.conf" ) )
            }
        } catch {
            case e : Exception => throw new DartConfigException( "The configuration/environment file does not exist" )
        }
        props.asScala.foreach( kv => {
            if ( kv._2.toLowerCase == "_env_" ) {
                try {
                    props.setProperty( kv._1, System.getenv( kv._1.replaceAll( "\\.", "_" ).toUpperCase ) )
                } catch {
                    case e : NullPointerException => throw new DartConfigException( s"The environment variable does not exist - ${kv._1}" )
                }
            }
        } )
        props
    }


}