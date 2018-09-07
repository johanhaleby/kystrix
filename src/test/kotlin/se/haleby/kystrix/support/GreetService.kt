package se.haleby.kystrix.support

import io.javalin.Javalin
import org.junit.rules.ExternalResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory

inline fun <reified T : Any> loggerFor(): Logger = LoggerFactory.getLogger(T::class.java)

class GreetService : ExternalResource() {

    private val log = loggerFor<GreetService>()

    private lateinit var server: Javalin

    override fun before() {
        server = Javalin.create().port(8080).disableStartupBanner().start()
        server.get("/greeting") { ctx ->
            val firstName = ctx.queryParam("firstName")
            val lastName = ctx.queryParam("lastName")
            log.info("About to greet $firstName $lastName")
            ctx.json(Greeting("Have a nice day $firstName $lastName"))
        }
    }

    fun port() = server.port()

    override fun after() {
        server.stop()
    }
}