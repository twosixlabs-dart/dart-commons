package com.twosixlabs.dart.sql

import java.util.Properties

import com.twosixlabs.dart.test.base.ScalaTestBase
import org.scalatest.BeforeAndAfterAll

class SqlClientTestSuite extends ScalaTestBase with BeforeAndAfterAll {

    private val ENGINE : String = "h2"
    private val DB_NAME : String = "client_test"

    private val props : Properties = {
        val p = new Properties()
        p.setProperty( "sql.db.engine", ENGINE )
        p.setProperty( "sql.db.name", DB_NAME )
        p
    }

    "SQL Client" should "be able to perform INSERT sql" in {
        val table = "insert_test"
        val client = SqlClient.newClient( props )

        createTestTable( table, client )

        val connection = client.connect()
        try {
            val insertSql = s"INSERT into ${table}(document_id, content) VALUES ('doc_1', 'one'), ('doc_2', 'two')"
            val results = client.executeInsert( insertSql, connection )
            results.next() shouldBe true
            results.getLong( "ID" ) shouldBe 1L
            results.next() shouldBe true
            results.getLong( 1 ) shouldBe 2L
        } catch {
            case e : Throwable => {
                e.printStackTrace
                fail( e )
            }
        } finally connection.close()

    }

    "SQL Client" should "be able to perform UPDATE sql" in {
        val table = "update_test"
        val client = SqlClient.newClient( props )

        createTestTable( table, client )

        val connection = client.connect()
        try {
            val insertSql = s"INSERT into ${table}(document_id, content) VALUES ('doc_1', 'one'), ('doc_2', 'two')"
            client.executeInsert( insertSql, connection )

            val updateSql = s"UPDATE ${table} SET content = 'update' WHERE document_id = 'doc_1'"
            val updateResults = client.executeUpdate( updateSql, connection )
            updateResults shouldBe 1
        } catch {
            case e : Throwable => {
                e.printStackTrace
                fail( e )
            }
        } finally connection.close()
    }

    "SQL Client" should "be able to perform SELECT sql" in {
        val table = "select_test"
        val client = SqlClient.newClient( props )

        createTestTable( table, client )

        val connection = client.connect()
        try {
            val insertSql = s"INSERT into ${table}(document_id, content) VALUES ('doc_1', 'one'), ('doc_2', 'two'), ('doc_3', 'three')"
            client.executeInsert( insertSql, connection )

            val selectSql = s"SELECT * FROM ${table} WHERE id < 3"
            val selectResults = client.executeQuery( selectSql, connection )
            selectResults.next() shouldBe true
            selectResults.getString( "document_id" ) shouldBe "doc_1"
            selectResults.getString( "content" ) shouldBe "one"

            selectResults.next() shouldBe true
            selectResults.getString( "document_id" ) shouldBe "doc_2"
            selectResults.getString( "content" ) shouldBe "two"

            selectResults.next() shouldBe false


        } catch {
            case e : Throwable => {
                e.printStackTrace
                fail( e )
            }
        } finally connection.close()

    }

    "SQL Client" should "be able to perform COUNT sql" in {
        val table = "count_test"
        val client = SqlClient.newClient( props )

        createTestTable( table, client )

        val connection = client.connect()
        try {
            val insertSql = s"INSERT into ${table}(document_id, content) VALUES ('doc_1', 'one'), ('doc_2', 'two'), ('doc_3', 'three')"
            client.executeInsert( insertSql, connection )

            val countSql = s"SELECT count(*) FROM ${table}"
            val countResults = client.executeCount( countSql, connection )
            countResults shouldBe 3L

        } catch {
            case e : Throwable => {
                e.printStackTrace
                fail( e )
            }
        } finally connection.close()
    }

    private def createTestTable( name : String, client : SqlClient ) : Unit = {
        val tableSql =
            s"""CREATE TABLE ${name} (
               |    id SERIAL PRIMARY KEY,
               |    document_id       TEXT NOT NULL,
               |    content TEXT NOT NULL
               |);""".stripMargin
        val connection = client.connect()
        try connection.prepareStatement( tableSql ).executeUpdate()
        catch {
            case e : Throwable => {
                e.printStackTrace()
                fail( e )
            }
        }

    }
}
