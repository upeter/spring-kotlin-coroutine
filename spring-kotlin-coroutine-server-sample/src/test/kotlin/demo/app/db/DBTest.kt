package demo.app.db

import com.firefly.db.SQLClient
import com.firefly.db.SQLConnection
import com.firefly.db.jdbc.JDBCClient
import com.firefly.kotlin.ext.db.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.experimental.future.await
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*
import demo.app.domain.User
import io.kotlintest.shouldBe


class DBTest {

    private val sqlClient: SQLClient

    init {
        val config = HikariConfig()
        config.jdbcUrl = "jdbc:h2:mem:${UUID.randomUUID().toString().replace("-", "")}"
        config.driverClassName = "org.h2.Driver"
        config.isAutoCommit = false
        val ds = HikariDataSource(config)
        sqlClient = JDBCClient(ds)
    }


    private suspend fun <T> exec(handler: suspend (conn: SQLConnection) -> T): T = sqlClient.connection.await().execSQL(handler)

    @Before
    fun before() = runBlocking {
        exec {
            it.asyncUpdate("DROP SCHEMA IF EXISTS test")
            it.asyncUpdate("CREATE SCHEMA IF NOT EXISTS test")
            val table = "CREATE TABLE IF NOT EXISTS `test`.`user`(" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "pt_first_name VARCHAR(255), " +
                    "pt_last_name VARCHAR(255), " +
                    "pt_avatar_url VARCHAR(255))"
            it.asyncUpdate(table)
            val params = Array<Array<Any>>(2) { Array(2) { } }
            (0..1).forEach{
                params[it][0] = "John " + it
                params[it][1] = "Doe " + it
            }
            val sql = "insert into `test`.`user`(pt_first_name, pt_last_name) values(?,?)"
            val id = it.insertBatch(sql, params) { it.map { it.getInt(1) } }.await()
            println(id)
        }
    }


    @Test
    fun testQueryById() = runBlocking {
        val user = exec { it.asyncQueryById<User>(1L) }
        user.firstName shouldBe "John 0"
    }

    @Test
    fun testInsertObject() = runBlocking {
        val newUser = User(null, "test insert", "test insert pwd", null)
        val newUserId = exec { it.asyncInsertObject<User, Long>(newUser) }
        newUserId shouldBe 3

        val user = exec { it.asyncQueryById<User>(newUserId) }
        user.firstName shouldBe "test insert"
    }

    @Test
    fun testUpdateObject() = runBlocking {
        val initialUser = exec { it.asyncQueryById<User>(1L) }
        val rows = exec { it.asyncUpdateObject(initialUser.copy(avatarUrl = "abc")) }
        rows shouldBe 1

        val updatedUser = exec { it.asyncQueryById<User>(1L) }
        updatedUser.avatarUrl shouldBe "abc"
    }


}



