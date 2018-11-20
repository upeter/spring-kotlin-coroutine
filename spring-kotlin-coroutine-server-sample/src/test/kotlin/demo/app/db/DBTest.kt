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
            //it.asyncUpdate("set mode MySQL")
            val table = "CREATE TABLE IF NOT EXISTS `test`.`user`(" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "pt_first_name VARCHAR(255), " +
                    "pt_last_name VARCHAR(255), " +
                    "pt_avatar_url VARCHAR(255))"
            it.asyncUpdate(table)
            val params = Array<Array<Any>>(3) { Array(3) { } }
            for (i in 0 until 3) {
                params[i][0] = "test transaction " + i
                params[i][1] = "pwd transaction " + i
                params[i][2] = "info transaction " + i
            }
            val sql = "insert into `test`.`user`(pt_first_name, pt_last_name, pt_avatar_url) values(?,?,?)"
            val id = it.insertBatch(sql, params, { rs ->
                rs.map { r -> r.getInt(1) }
            }).await()
            println(id)
        }
    }





    @Test
    fun testQueryById() = runBlocking {
        val user = exec { it.asyncQueryById<User>(1L) }
        user.firstName shouldBe "test transaction 0"
    }

    @Test
    fun testUpdateObject() = runBlocking {
        val initialUser = exec { it.asyncQueryById<User>(1L) }
        val rows = exec { it.asyncUpdateObject(initialUser.copy(awatarUrl = "abc")) }
        rows shouldBe 1

        val updatedUser = exec { it.asyncQueryById<User>(1L) }
        updatedUser.awatarUrl shouldBe "abc"
    }

    @Test
    fun testInsertObject() = runBlocking {
        val newUser = User(null, "test insert", "test insert pwd", null)
        val newUserId = exec { it.asyncInsertObject<User, Long>(newUser) }
        newUserId shouldBe 4

        val user = exec { it.asyncQueryById<User>(newUserId) }
        user.firstName shouldBe "test insert"
    }


}



