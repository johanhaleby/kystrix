# ![Logo](https://raw.githubusercontent.com/johanhaleby/kystrix/master/kystrix-logo.png "Kystrix")
[![Build Status](https://travis-ci.org/johanhaleby/kystrix.svg)](https://travis-ci.org/johanhaleby/kystrix)

Kystrix is a small DSL that makes working with [Hystrix](https://github.com/Netflix/Hystrix) easier from Kotlin.

For example:

```kotlin
val greeting = hystrixCommand<Greeting> {
    groupKey("GreetingService")
    commandKey("Greeting")
    command {
        // This is what you want Hystrix to wrap
        get("/greeting?firstName=John&lastName=Doe").asJson()
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
```

This is a work in progress, expect more soon.
