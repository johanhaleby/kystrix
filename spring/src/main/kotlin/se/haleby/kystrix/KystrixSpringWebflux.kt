package se.haleby.kystrix

import net.javacrumbs.futureconverter.java8rx.FutureConverter.toCompletableFuture
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.fromFuture
import rx.Observable
import rx.RxReactiveStreams

fun <T> KystrixCommand<T>.monoCommand(block: () -> Mono<T>) {
    command {
        block().block()
    }
}

fun <T> KystrixObservableCommand<T>.monoCommand(block: () -> Mono<T>) {
    command {
        Observable.from(block().toFuture())
    }
}

fun <T> KystrixObservableCommand<T>.fluxCommand(block: () -> Flux<T>) {
    command {
        RxReactiveStreams.toObservable(block())
    }
}

fun <T> Observable<T>.toMono(): Mono<T> = fromFuture(toCompletableFuture(toSingle()))

// TODO Add a toFlux() extension method on observable