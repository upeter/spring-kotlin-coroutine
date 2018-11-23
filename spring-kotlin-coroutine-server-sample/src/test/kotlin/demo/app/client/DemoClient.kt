package demo.app.client

import demo.app.domain.Avatar
import kotlinx.coroutines.*
import kotlinx.coroutines.experimental.*
import org.springframework.http.HttpMethod
import org.springframework.web.coroutine.function.client.CoroutineWebClient
import java.util.*
import kotlin.system.measureTimeMillis
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


fun main(args: Array<String>) {

    val coroutineThread = newFixedThreadPoolContext(10, "CoroutineThread")


    runBlocking {
        val avatar = CoroutineWebClient.create("http://localhost:8081")
                .get()
                .uri("/avatar")
                .retrieve()
                .body(Avatar::class.java)!!
        println(avatar)
    }

    val chars = ('a'..'z').toList()
    fun randomChar() = chars[Math.abs(Random().nextInt(chars.size - 1))].toString()
    val job = CoroutineScope(coroutineThread).launch {
        val millis = measureTimeMillis {
            val res = (1..1000).map {
                async {
                    val c = randomChar()
                    println("${Thread.currentThread().name} sending $it: $c")
                    CoroutineWebClient.create("http://localhost:8080/pass/${c}").get().retrieve().body(String::class.java)
                }
            }
            res.forEach { it.await() }
        }
        println("Took $millis ms")

    }
    runBlocking {
        job.join()
    }
}