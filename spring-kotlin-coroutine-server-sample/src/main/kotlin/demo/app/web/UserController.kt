package demo.app.web

import com.firefly.db.SQLConnection
import com.firefly.db.jdbc.JDBCClient
import com.firefly.kotlin.ext.db.asyncQueryById
import com.firefly.kotlin.ext.db.asyncUpdateObject
import com.firefly.kotlin.ext.db.execSQL
import demo.app.domain.Awatar
import demo.app.domain.User
import kotlinx.coroutines.experimental.future.await
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.web.coroutine.function.client.CoroutineWebClient

@Component
open class UserDao(val sqlClient: JDBCClient) {

    suspend fun findUserById(id: Long): User? = exec { it.asyncQueryById<User>(id) }

    suspend fun updateUser(user: User): User = exec {
        it.asyncUpdateObject(user)
        user
    }

    suspend fun <T> exec(handler: suspend (conn: SQLConnection) -> T): T = exec(sqlClient, handler)

    companion object {
        suspend fun <T> exec(sqlClient: JDBCClient, handler: suspend (conn: SQLConnection) -> T): T = sqlClient.connection.await().execSQL(handler)

    }

}

@Component
open class AwatarDao {

    private val client by lazy { CoroutineWebClient.create("http://localhost:8081") }

    open suspend fun randomAvatar(): Awatar =
            client.get()
                    .uri("/awatar")
                    .retrieve()
                    .body(Awatar::class.java)!!
}


@RestController
open class UserController(
        private val userDao: UserDao,
        private val avatarDao: AwatarDao
) {

    @GetMapping("/users/{user-id}")
    @ResponseBody
    open suspend fun getUser(@PathVariable("user-id") id: Long = 0): User? =
            userDao.findUserById(id)

    @PatchMapping("/users/{user-id}/sync-awatar")
    @ResponseBody
    open suspend fun syncAwatar(@PathVariable("user-id") id: Long = 0): User? =
            userDao.findUserById(id)?.let {
                val update = it.copy(awatarUrl = avatarDao.randomAvatar().url)
                userDao.updateUser(update)
            }
}