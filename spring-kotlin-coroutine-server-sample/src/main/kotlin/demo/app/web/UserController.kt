package demo.app.web

import com.firefly.db.SQLConnection
import com.firefly.db.jdbc.JDBCClient
import com.firefly.kotlin.ext.db.asyncQueryById
import com.firefly.kotlin.ext.db.asyncUpdateObject
import com.firefly.kotlin.ext.db.execSQL
import demo.app.domain.Avatar
import demo.app.domain.User
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.channels.produce
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.future.await
import org.springframework.http.MediaType
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
open class AvatarDao {

    private val client by lazy { CoroutineWebClient.create("http://localhost:8081") }

    open suspend fun randomAvatar(): Avatar =
            client.get()
                    .uri("/avatar")
                    .retrieve()
                    .body(Avatar::class.java)!!
}


@RestController
open class UserController(
        private val userDao: UserDao,
        private val avatarDao: AvatarDao
) {


    @GetMapping("/users/{user-id}")
    @ResponseBody
    open suspend fun getUser(@PathVariable("user-id") id: Long = 0): User? =
            userDao.findUserById(id)

    @PatchMapping("/users/{user-id}/sync-avatar")
    @ResponseBody
    open suspend fun syncAvatar(@PathVariable("user-id") id: Long = 0): User? =
            userDao.findUserById(id)?.let {
                val avatar = avatarDao.randomAvatar()
                userDao.updateUser(it.copy(avatarUrl = avatar.url))
            }















    @GetMapping("/sse/avatars", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    @ResponseBody
    open fun sseAvatars(@RequestParam count: Int?, @RequestParam delayMs: Long?) = GlobalScope.produce {
        repeat(count ?: 100) {
            send(avatarDao.randomAvatar())
            delay(delayMs ?: 500)
        }
    }


}














