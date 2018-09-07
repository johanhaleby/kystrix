package se.haleby.kystrix

import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy.THREAD
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import se.haleby.kystrix.support.GreetService
import se.haleby.kystrix.support.Greeting


class KystrixSpringFluxObservableCommandTest {

    @Rule
    @JvmField
    var greetService = GreetService()

    @Test
    fun `Spring DSL works with Hystrix Flux Commands`() {
        // Given
        val firstName = "John"
        val lastName = "Doe"

        // When
        val greeting = hystrixObservableCommand<Greeting> {
            groupKey("Test")
            commandKey("Test-Command")
            fluxCommand {
                val webClient = WebClient.builder().baseUrl("http://localhost:8080").defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE).build()
                webClient.get().uri("/greeting?firstName=$firstName&lastName=$lastName")
                        .retrieve().bodyToFlux()
            }
            commandProperties {
                withExecutionTimeoutInMilliseconds(10000)
                withExecutionIsolationStrategy(THREAD)
                withFallbackEnabled(false)
            }
        }

        // Then
        assertThat(greeting.toBlocking().first().message).isEqualTo("Have a nice day $firstName $lastName")
    }
}