package se.haleby.kystrix

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.asynchttpclient.Dsl
import org.asynchttpclient.Response
import org.junit.Rule
import org.junit.Test
import rx.Observable
import se.haleby.kystrix.support.GreetService
import se.haleby.kystrix.support.Greeting

class KystrixObservableCommandTest {

    @Rule
    @JvmField
    var greetService = GreetService()

    @Test
    fun `DSL works with Hystrix Observable Commands`() {
        // Given
        val firstName = "John"
        val lastName = "Doe"

        // When
        val greeting = hystrixObservableCommand<Greeting> {
            groupKey("Test")
            commandKey("Test-Command")
            command {
                val response = Dsl.asyncHttpClient().executeRequest(Dsl.get("http://localhost:${greetService.port()}/greeting?firstName=$firstName&lastName=$lastName").build())
                Observable.from(response).map(Response::getResponseBody).map { body -> ObjectMapper().registerKotlinModule().readValue<Greeting>(body) }
            }
            commandProperties {
                withExecutionTimeoutInMilliseconds(2000)
                withExecutionIsolationSemaphoreMaxConcurrentRequests(3)
                withFallbackEnabled(false)
            }
        }.toBlocking().first()

        // Then
        assertThat(greeting.message).isEqualTo("Have a nice day $firstName $lastName")
    }
}