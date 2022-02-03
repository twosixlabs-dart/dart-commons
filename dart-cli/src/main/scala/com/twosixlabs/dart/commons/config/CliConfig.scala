package com.twosixlabs.dart.commons.config

import java.util.Properties

import scala.collection.JavaConverters._

trait CliConfig {

    /**
      *
      * Takes in the CLI program arguments to the file and processes them, the result should be a set of key:values
      * that can be loaded into a java.util.Properties
      *
      * @param args - cmd line args
      * @return
      */
    def processConfig( args : Array[ String ] ) : Properties


    /**
      *
      * This will take the key:values java.util.Properties and set them as JVM environment variables
      * via <code>System.setProperty( "key","value" )</code>
      *
      * @param args - cmd line args
      */
    final def loadEnvironment( args : Array[ String ] ) : Unit = {
        val props : Properties = processConfig( args )
        props.keySet().asScala.map( _.asInstanceOf[ String ] ).foreach( key => System.setProperty( key, props.get( key ).asInstanceOf[ String ] ) )
    }

}