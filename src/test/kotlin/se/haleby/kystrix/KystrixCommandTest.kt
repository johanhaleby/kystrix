package se.haleby.kystrix

import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy.THREAD
import io.restassured.RestAssured.get
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import se.haleby.kystrix.support.GreetService
import se.haleby.kystrix.support.Greeting
import se.haleby.kystrix.support.asJson

class KystrixCommandTest {

    @Rule
    @JvmField
    var greetService = GreetService()

    @Test
    fun `DSL works with Hystrix Commands`() {
        // Given
        val firstName = "John"
        val lastName = "Doe"

        // When
        val greeting = hystrixCommand<Greeting> {
            groupKey("Test")
            commandKey("Test-Command")
            command {
                get("/greeting?firstName=$firstName&lastName=$lastName").asJson()
            }
            commandProperties {
                withExecutionTimeoutInMilliseconds(10000)
                withExecutionIsolationStrategy(THREAD)
                withFallbackEnabled(false)
            }
            threadPoolProperties {
                withQueueSizeRejectionThreshold(5)
                withMaxQueueSize(10)
            }
        }.execute()

        // Then
        assertThat(greeting.message).isEqualTo("Have a nice day $firstName $lastName")
    }
}