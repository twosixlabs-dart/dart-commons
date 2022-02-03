package com.twosixlabs.dart.sql

import java.sql.{Connection, ResultSet, SQLException, Statement}
import java.util.Properties

import com.mchange.v2.c3p0.ComboPooledDataSource
import org.slf4j.{Logger, LoggerFactory}

object SqlClient {

    private val LOG : Logger = LoggerFactory.getLogger( getClass )

    def newClient( props : Properties ) : SqlClient = {
        new SqlClient( SqlProvider.newConnectionPool( props ) )
    }

    def newClient( engine : String, database : String, host : String, port : Int, user : Option[ String ] = None, password : Option[ String ] = None ) : SqlClient = {
        new SqlClient( SqlProvider.newConnectionPool( engine, database, host, port, user, password ) )
    }

}

class SqlClient( cp : ComboPooledDataSource ) extends SqlProvider {

    override protected val connectionPool : ComboPooledDataSource = cp

    @throws[ SQLException ]
    def executeInsert( sql : String, connection : Connection ) : ResultSet = {
        val statement = connection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS )
        statement.executeUpdate()
        statement.getGeneratedKeys
    }

    @throws[ SQLException ]
    def executeUpdate( sql : String, connection : Connection ) : Int = {
        val statement = connection.prepareStatement( sql, Statement.RETURN_GENERATED_KEYS )
        statement.executeUpdate()
    }

    @throws[ SQLException ]
    def executeQuery( sql : String, connection : Connection ) : ResultSet = {
        val statement = connection.prepareStatement( sql )
        statement.executeQuery()
    }

    @throws[ SQLException ]
    def executeCount( sql : String, connection : Connection ) : Long = {
        val results = executeQuery( sql, connection )
        if ( results.next() ) results.getLong( 1 )
        else throw new SQLException( "Unable to perform count operation" )
    }

}
