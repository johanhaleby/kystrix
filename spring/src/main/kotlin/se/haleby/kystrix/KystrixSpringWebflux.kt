package se.haleby.kystrix

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import rx.Observable
import rx.RxReactiveStreams

/**
 * Extension function that enables calling a code block that returns a [Mono] in a *blocking* fashion when using the [hystrixCommand].
 * For example:
 *
 * ```kotlin
 * val greeting = hystrixCommand<Greeting> {
 *    groupKey("Test")
 *    commandKey("Test-Command")
 *    // Here's the use of monoCommand
 *    monoCommand {
 *        webClient.get().uri("/somewhere").retrieve().bodyToMono()
 *    }
 *    commandProperties {
 *        withExecutionTimeoutInMilliseconds(10000)
 *        withExecutionIsolationStrategy(THREAD)
 *        withFallbackEnabled(false)
 *    }
 *    threadPoolProperties {
 *        withQueueSizeRejectionThreshold(5)
 *        withMaxQueueSize(10)
 *    }
 *}.execute()
 * ```
 *
 * **NOTE:** This command will block the Mono! You might want to look at using a [KystrixObservableCommand] (i.e. creating a DSL from [hystrixObservableCommand] instead).
 */
fun <T> KystrixCommand<T>.monoCommand(block: () -> Mono<T>) {
    command {
        block().block()
    }
}

/**
 * Extension function that enables calling a code block that returns a [Mono] in a *non-blocking* fashion when using the [hystrixObservableCommand] DSL.
 * For example:
 *
 * ```kotlin
 * val greetingObservable = hystrixObservableCommand<Greeting> {
 *    groupKey("Test")
 *    commandKey("Test-Command")
 *    // Here's the use of monoCommand
 *    monoCommand {
 *        webClient.get().uri("/somewhere").retrieve().bodyToMono()
 *    }
 *    commandProperties {
 *        withExecutionTimeoutInMilliseconds(10000)
 *        withExecutionIsolationStrategy(THREAD)
 *        withFallbackEnabled(false)
 *    }
 *    threadPoolProperties {
 *        withQueueSizeRejectionThreshold(5)
 *        withMaxQueueSize(10)
 *    }
 *}
 * ```
 */
fun <T> KystrixObservableCommand<T>.monoCommand(block: () -> Mono<T>) {
    command {
        Observable.from(block().toFuture())
    }
}

/**
 * Extension function that enables calling a code block that returns a [Flux] in a *non-blocking* fashion when using the [hystrixObservableCommand] DSL.
 *
 * * For example:
 *
 * ```kotlin
 * val greetingObservable = hystrixObservableCommand<Greeting> {
 *    groupKey("Test")
 *    commandKey("Test-Command")
 *    // Here's the use of monoCommand
 *    fluxCommand {
 *        webClient.get().uri("/somewhere").retrieve().bodyToFlux()
 *    }
 *    commandProperties {
 *        withExecutionTimeoutInMilliseconds(10000)
 *        withExecutionIsolationStrategy(THREAD)
 *        withFallbackEnabled(false)
 *    }
 *    threadPoolProperties {
 *        withQueueSizeRejectionThreshold(5)
 *        withMaxQueueSize(10)
 *    }
 *}
 * ```
 */
fun <T> KystrixObservableCommand<T>.fluxCommand(block: () -> Flux<T>) {
    command {
        RxReactiveStreams.toObservable(block())
    }
}

/**
 * Extension function converts an [Observable] into a [Mono]. Useful if you'd like to continue working with [Mono] data structures after you've used the Kystrix DSL ([hystrixObservableCommand]).
 * For example:
 *
 * ```kotlin
 * val greetingObservable : Observable<Greeting> = hystrixObservableCommand<Greeting> { ... }
 * val greetingMono : Mono<Greeting> = greetingObservable.toMono()
 * ```
 */
fun <T> Observable<T>.toMono(): Mono<T> = Mono.from(RxReactiveStreams.toPublisher(this))

/**
 * Extension function converts an [Observable] to a [Flux]. Useful if you'd like to continue working with [Flux] data structures after you've used the Kystrix DSL ([hystrixObservableCommand]).
 * For example:
 *
 * ```kotlin
 * val greetingObservable : Observable<Greeting> = hystrixObservableCommand<Greeting> { ... }
 * val greetingFlux : Flux<Greeting> = greetingObservable.toFlux()
 * ```
 */
fun <T> Observable<T>.toFlux(): Flux<T> = Flux.from(RxReactiveStreams.toPublisher(this))