package com.twosixlabs.dart.sql

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.{Logger, LoggerFactory}

import java.sql.Connection
import java.util.Properties
import scala.collection.JavaConverters._

object SqlProvider {

    private object Engines {
        val POSTGRES = "postgresql"
        val H2 = "h2"
    }

    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    def newConnectionPool( properties : Properties ) : ComboPooledDataSource = newConnectionPool( properties.asScala.toMap )

    def newConnectionPool( properties : Map[ String, String ] ) : ComboPooledDataSource = {
        try {
            // required props
            val engine : String = properties( "sql.db.engine" )
            val database : String = properties( "sql.db.name" )

            // optionalish props
            val host : String = properties.getOrElse( "sql.db.host", null )
            val port : Int = properties.getOrElse( "sql.db.port", "-1" ).toInt
            val user : Option[ String ] = properties.get( "sql.db.user" )
            val password : Option[ String ] = properties.get( "sql.db.password" )

            val connectionPoolProps = properties.filter( _._1.startsWith( "connection.pool" ) )

            newConnectionPool( engine, database, host, port, user, password, connectionPoolProps )
        }
        catch {
            case e : NoSuchElementException => {
                LOG.error( s"required property was missing from configuration: ${e.getMessage}" )
                throw e
            }
        }
    }

    //@formatter:off
    def newConnectionPool( engine : String, database : String, host : String, port : Int, user : Option[ String ] = None, password : Option[ String ] = None, connectionPoolProps : Map[ String, String ] = Map() ) : ComboPooledDataSource = {
        val connectionPool : ComboPooledDataSource = new ComboPooledDataSource()

        engine.toLowerCase match {
            case Engines.POSTGRES => {
                connectionPool.setDriverClass( "org.postgresql.Driver" )
                connectionPool.setJdbcUrl( s"jdbc:${engine}://${host}:${port}/${database}" )
            }
            case Engines.H2 => {
                connectionPool.setDriverClass( "org.h2.Driver" )
                connectionPool.setJdbcUrl( s"jdbc:${engine}:mem:${database};MODE=PostgreSQL;DB_CLOSE_DELAY=-1" )
            }
            case other => unsupportedEngine( other )
        }


        if ( connectionPoolProps.nonEmpty ) {
            connectionPool.setMinPoolSize( connectionPoolProps.getOrElse( "connection.pool.min.size", "1" ).toInt )
            connectionPool.setMaxPoolSize( connectionPoolProps.getOrElse( "connection.pool.max.size", "25" ).toInt )
            connectionPool.setAcquireIncrement( connectionPoolProps.getOrElse( "connection.pool.acquire.increment", "1" ).toInt )
            connectionPool.setAcquireRetryDelay( connectionPoolProps.getOrElse( "connection.pool.acquire.retry.delay", "2000" ).toInt )
            connectionPool.setAcquireRetryAttempts( connectionPoolProps.getOrElse( "connection.pool.acquire.retry.attempts", "50" ).toInt )
        } else{
            connectionPool.setMinPoolSize( 3 )
            connectionPool.setMaxPoolSize( 15 )
            connectionPool.setAcquireIncrement( 1 )
            connectionPool.setAcquireRetryDelay( 2000 )
            connectionPool.setAcquireRetryAttempts( 50 )
        }


        if ( user.isDefined ) connectionPool.setUser( user.get )
        if ( password.isDefined ) connectionPool.setPassword( password.get )

        connectionPool
    }
    //@formatter:on

    @throws[ IllegalArgumentException ]
    private def unsupportedEngine( engine : String ) = throw new IllegalArgumentException( s"unsupported database engine: ${engine}" )
}

trait SqlProvider {

    protected val connectionPool : ComboPooledDataSource

    def connect( ) : Connection = connectionPool.getConnection()

}
