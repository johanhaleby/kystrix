package se.haleby.kystrix

import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy.THREAD
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import se.haleby.kystrix.support.GreetService
import se.haleby.kystrix.support.Greeting
import se.haleby.kystrix.support.HystrixSupport


class KystrixSpringFluxObservableCommandTest {

    @Rule
    @JvmField
    var greetService = GreetService()

    @Rule
    @JvmField
    var hystrixSupport = HystrixSupport()

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

    @Test
    fun `Kystrix provides a Kotlin extension function for converting an Observable to a Flux`() {
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
        }.toFlux()

        // Then
        assertThat(greeting.blockFirst()?.message).isEqualTo("Have a nice day $firstName $lastName")
    }

    @Test
    fun `Kystrix provides a Kotlin extension function for converting an Observable to a Mono`() {
        // Given
        val firstName = "John"
        val lastName = "Doe"

        // When
        val greeting = hystrixObservableCommand<Greeting> {
            groupKey("Test")
            commandKey("Test-Command")
            monoCommand {
                val webClient = WebClient.builder().baseUrl("http://localhost:8080").defaultHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE).build()
                webClient.get().uri("/greeting?firstName=$firstName&lastName=$lastName")
                        .retrieve().bodyToMono()
            }
            commandProperties {
                withExecutionTimeoutInMilliseconds(10000)
                withExecutionIsolationStrategy(THREAD)
                withFallbackEnabled(false)
            }
        }.toMono()

        // Then
        assertThat(greeting.block()?.message).isEqualTo("Have a nice day $firstName $lastName")
    }
}