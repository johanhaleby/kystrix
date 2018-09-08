package se.haleby.kystrix

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
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

fun <T> Observable<T>.toMono(): Mono<T> = Mono.from(RxReactiveStreams.toPublisher(this))

fun <T> Observable<T>.toFlux(): Flux<T> = Flux.from(RxReactiveStreams.toPublisher(this))