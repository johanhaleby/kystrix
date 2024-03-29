# ![Logo](https://raw.githubusercontent.com/johanhaleby/kystrix/master/kystrix-logo.png "Kystrix")
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.haleby.kystrix/kystrix-core/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.haleby.kystrix/kystrix-core)


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
}
```

## Contents
1. [Getting Started](#getting-started)
1. [Spring Support](#spring-support)
1. [More Examples](#more-examples)

## Getting Started

The project contains two modules, `kystrix-core` and `kystrix-spring`. You only need `kystrix-spring` if you're using components from Spring's reactive stack such as [spring-webflux](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html). See [Kystrix Spring](#spring-support) for more info.

The project is available in Maven central as well as JCenter.

#### Maven

```xml
<dependency>
    <groupId>se.haleby.kystrix</groupId>
    <artifactId>kystrix-core</artifactId>
    <version>0.1.16</version>
</dependency>
```

#### Gradle

```groovy
compile 'se.haleby.kystrix:kystrix-core:0.1.16'
```

#### Kobalt

```kotlin
dependencies {
    compile("se.haleby.kystrix:kystrix-core:0.1.16")
}
```

#### Imports

Once the `kystrix-core` DSL is included in the build there are two different entry points, `hystrixCommand` and `hystrixObservableCommand`, and both can be imported like this:

```kotlin
import se.haleby.kystrix.hystrixCommand
import se.haleby.kystrix.hystrixObservableCommand
```

Use the `hystrixCommand` for blocking IO and `hystrixObservableCommand` for non-blocking IO using RxJava Observables.

See [this blog post](http://code.haleby.se/2018/09/16/kystrix-a-kotlin-dsl-for-hystrix/) for more information.

## Spring Support

Kystrix provides a module named `kystrix-spring` makes it easier to integrate and work with [Mono](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html) and [Flux](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html) and Hystrix.

#### Maven

```xml
<dependency>
    <groupId>se.haleby.kystrix</groupId>
    <artifactId>kystrix-spring</artifactId>
    <version>0.1.16</version>
</dependency>
```

#### Gradle

```groovy
compile 'se.haleby.kystrix:kystrix-spring:0.1.16'
```

#### Kobalt

```kotlin
dependencies {
    compile("se.haleby.kystrix:kystrix-spring:0.1.16")
}
```

#### Imports

Once the `kystrix-spring` DSL is included in the classpath you can start using the extension functions in the `se.haleby.kystrix` package:

```
import se.haleby.kystrix.monoCommand
import se.haleby.kystrix.toMono
import se.haleby.kystrix.fluxCommand
import se.haleby.kystrix.toFlux
```

#### Mono Example

```kotlin
val greeting = hystrixObservableCommand<Greeting> {
    groupKey("Test")
    commandKey("Test-Command")
    monoCommand {
        webClient.get().uri("/greetings/1").retrieve().bodyToMono()
    }
    commandProperties {
        withExecutionTimeoutInMilliseconds(10000)
        withFallbackEnabled(false)
    }
}.toMono()
```

Using the `monoCommand` extension function makes it easy to integrate a `Mono` response with Hystrix. Also note the call to `toMono()` at the end, this will convert the `Observable` returned by Hystrix back to a `Mono` instance.

#### Flux Example

```kotlin
val greeting = hystrixObservableCommand<Greeting> {
    groupKey("Test")
    commandKey("Test-Command")
    fluxCommand {
        webClient.get().uri("/greetings").retrieve().bodyToFlux()
    }
    commandProperties {
        withExecutionTimeoutInMilliseconds(10000)
        withFallbackEnabled(false)
    }
}.toFlux()
```

Here Kystrix returns a non-blocking stream of `Greeting` wrapped in a `Flux`.

## More Examples

Have a look in the [test package](https://github.com/johanhaleby/kystrix/tree/master/core/src/test/kotlin/se/haleby/kystrix) (see here for [spring](https://github.com/johanhaleby/kystrix/tree/master/spring/src/test/kotlin/se/haleby/kystrix) examples).

## Support my work

<a href="https://www.buymeacoffee.com/johanhaleby" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/arial-blue.png" alt="Buy Me A Coffee" style="height: 42px !important;width: 180px !important;" height="42px" width="180px"></a>
