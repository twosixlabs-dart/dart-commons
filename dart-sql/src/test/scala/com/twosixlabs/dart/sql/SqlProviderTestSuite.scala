package com.twosixlabs.dart.sql

import com.mchange.v2.c3p0.ComboPooledDataSource
import com.twosixlabs.dart.test.base.ScalaTestBase

class SqlProviderTestSuite extends ScalaTestBase {

    "Sql Provider" should "honor specified configurations for the connection pool" in {
        val props : Map[ String, String ] = Map( "sql.db.engine" -> "h2",
                                                 "sql.db.host" -> "localhost",
                                                 "sql.db.port" -> "1000",
                                                 "sql.db.name" -> "test",
                                                 "connection.pool.min.size" -> "2",
                                                 "connection.pool.max.size" -> "200",
                                                 "connection.pool.acquire.increment" -> "2",
                                                 "connection.pool.acquire.retry.delay" -> "3000",
                                                 "connection.pool.acquire.retry.attempts" -> "300" )

        val connectionPool : ComboPooledDataSource = SqlProvider.newConnectionPool( props )

        connectionPool.getMinPoolSize shouldBe 2
        connectionPool.getMaxPoolSize shouldBe 200
        connectionPool.getAcquireIncrement shouldBe 2
        connectionPool.getAcquireRetryDelay shouldBe 3000
        connectionPool.getAcquireRetryAttempts shouldBe 300
    }

    "Sql Provider" should "user default configurations for the connection pool" in {
        val props : Map[ String, String ] = Map( "sql.db.engine" -> "h2",
                                                 "sql.db.host" -> "localhost",
                                                 "sql.db.port" -> "1000",
                                                 "sql.db.name" -> "test" )


        val connectionPool : ComboPooledDataSource = SqlProvider.newConnectionPool( props )

        connectionPool.getMinPoolSize shouldBe 3
        connectionPool.getMaxPoolSize shouldBe 15
        connectionPool.getAcquireIncrement shouldBe 1
        connectionPool.getAcquireRetryDelay shouldBe 2000
        connectionPool.getAcquireRetryAttempts shouldBe 50
    }


}
