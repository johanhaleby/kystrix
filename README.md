# ![Logo](https://raw.githubusercontent.com/johanhaleby/kystrix/master/kystrix-logo.png "Kystrix")

Kystrix is a small DSL that allows working with [Hystrix](https://github.com/Netflix/Hystrix) easier from Kotlin.

For example:

```kotlin
val greeting = hystrixObservableCommand<Greeting> {
        groupKey("Test")
        commandKey("Test-Command")
        command {
            val response = Dsl.asyncHttpClient().executeRequest(Dsl.get("http://localhost:8080/greeting?firstName=John&lastName=Doe").build())
            Observable.from(response).map(Response::getResponseBody).map { body -> ObjectMapper().registerKotlinModule().readValue<Greeting>(body) }
        }
        commandProperties {
            withExecutionTimeoutInMilliseconds(2000)
            withExecutionIsolationSemaphoreMaxConcurrentRequests(3)
            withFallbackEnabled(false)
        }
    }.toBlocking().first()
```

This is a work in progress!