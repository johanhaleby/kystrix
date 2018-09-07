package se.haleby.kystrix

import com.netflix.hystrix.*
import rx.Observable

inline fun <reified T : Any> hystrixObservableCommand(block: KystrixObservableCommand<T>.() -> Unit): Observable<T> = KystrixObservableCommand<T>().apply(block).build()

inline fun <reified T : Any> hystrixCommand(block: KystrixCommand<T>.() -> Unit): HystrixCommand<T> = KystrixCommand<T>().apply(block).build()

open class KystrixBase {
    internal lateinit var commandKey: HystrixCommandKey
    internal lateinit var groupKey: HystrixCommandGroupKey
    internal var commandProperties: HystrixCommandProperties.Setter = HystrixCommandProperties.Setter()

    fun groupKey(key: HystrixCommandGroupKey) {
        groupKey = key
    }

    fun groupKey(key: String) {
        groupKey(HystrixCommandGroupKey.Factory.asKey(key))
    }

    fun commandKey(key: HystrixCommandKey) {
        commandKey = key
    }

    fun commandKey(key: String) {
        commandKey(HystrixCommandKey.Factory.asKey(key))
    }

    fun commandProperties(block: HystrixCommandProperties.Setter.() -> Unit) {
        commandProperties = HystrixCommandProperties.Setter().apply(block)
    }
}

class KystrixObservableCommand<T> : KystrixBase() {

    private lateinit var command: () -> Observable<T?>
    private var fallback: (() -> Observable<T?>)? = null

    fun command(block: () -> Observable<T?>) {
        command = block
    }

    fun fallback(block: () -> Observable<T?>) {
        fallback = block
    }

    fun build(): Observable<T> {
        val settings = HystrixObservableCommand.Setter.withGroupKey(groupKey)
                .andCommandKey(commandKey)
                .andCommandPropertiesDefaults(commandProperties)
        return object : HystrixObservableCommand<T>(settings) {
            override fun construct(): Observable<T?> = command()
            override fun resumeWithFallback(): Observable<T?> = if (fallback == null) super.resumeWithFallback() else fallback!!()
        }.toObservable()
    }
}

class KystrixCommand<T> : KystrixBase() {

    private lateinit var command: () -> T?
    private var threadPoolKey: HystrixThreadPoolKey? = null
    private var definedFallback: (() -> T?)? = null
    private var threadPoolProperties: HystrixThreadPoolProperties.Setter = HystrixThreadPoolProperties.Setter()

    fun command(block: () -> T?) {
        command = block
    }

    fun fallback(block: () -> T?) {
        definedFallback = block
    }

    fun threadPoolKey(key: HystrixThreadPoolKey) {
        threadPoolKey = key
    }

    fun threadPoolKey(key: String) {
        threadPoolKey(HystrixThreadPoolKey.Factory.asKey(key))
    }

    fun threadPoolProperties(block: HystrixThreadPoolProperties.Setter.() -> Unit) {
        threadPoolProperties = HystrixThreadPoolProperties.Setter().apply(block)
    }

    fun build(): HystrixCommand<T> {
        val settings = HystrixCommand.Setter.withGroupKey(groupKey)
                .andCommandKey(commandKey)
                .andCommandPropertiesDefaults(commandProperties)
                .andThreadPoolKey(threadPoolKey)
                .andThreadPoolPropertiesDefaults(threadPoolProperties)

        return object : HystrixCommand<T>(settings) {
            override fun run() = command()
            override fun getFallback(): T? = if (definedFallback == null) super.getFallback() else definedFallback!!()
        }
    }
}