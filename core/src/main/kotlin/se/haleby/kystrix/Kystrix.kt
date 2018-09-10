package se.haleby.kystrix

import com.netflix.hystrix.*
import rx.Observable

/**
 * This is the entry-point of the Kystrix DSL for [HystrixObservableCommand]'s.
 *
 *
 * For example:
 *
 * ```kotlin
 * val greetingObservable = hystrixObservableCommand<Greeting> {
 *    groupKey("GreetingService")
 *    commandKey("Greeting")
 *    command {
 *        // This is what you want Hystrix to wrap
 *        get("/greeting?firstName=John&lastName=Doe").asJson().toObservable()
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
 *
 * If you're using Spring you should also have a look at the [Kystrix Spring](http://kystrix.haleby.se) project.
 */
inline fun <reified T : Any> hystrixObservableCommand(block: KystrixObservableCommand<T>.() -> Unit): Observable<T> = KystrixObservableCommand<T>().apply(block).build()

/**
 * This is the entry-point of the Kystrix DSL for [HystrixCommand]'s.
 *
 *
 * For example:
 *
 * ```kotlin
 * val greeting = hystrixCommand<Greeting> {
 *    groupKey("GreetingService")
 *    commandKey("Greeting")
 *    command {
 *        // This is what you want Hystrix to wrap
 *        get("/greeting?firstName=John&lastName=Doe").asJson()
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
 */
inline fun <reified T : Any> hystrixCommand(block: KystrixCommand<T>.() -> Unit): HystrixCommand<T> = KystrixCommand<T>().apply(block).build()

/**
 * A base-class for shared Kystrix DSL functionality. This should never be used directly.
 */
abstract class KystrixBase {
    internal lateinit var commandKey: HystrixCommandKey
    internal lateinit var groupKey: HystrixCommandGroupKey
    internal var commandProperties: HystrixCommandProperties.Setter = HystrixCommandProperties.Setter()

    /**
     * Specify the group key, see [HystrixCommandGroupKey].
     */
    fun groupKey(key: HystrixCommandGroupKey) {
        groupKey = key
    }

    /**
     * Specify the group key as a String, see [HystrixCommandGroupKey].
     */
    fun groupKey(key: String) {
        groupKey(HystrixCommandGroupKey.Factory.asKey(key))
    }

    /**
     * Specify the command key, see [HystrixCommandKey].
     */
    fun commandKey(key: HystrixCommandKey) {
        commandKey = key
    }

    /**
     * Specify the command key as a String, see [HystrixCommandKey].
     */
    fun commandKey(key: String) {
        commandKey(HystrixCommandKey.Factory.asKey(key))
    }

    /**
     * Configure the Hystrix command properties, see [HystrixCommandProperties].
     */
    fun commandProperties(block: HystrixCommandProperties.Setter.() -> Unit) {
        commandProperties = HystrixCommandProperties.Setter().apply(block)
    }
}

/**
 * Class used in the Kystrix DSL to define aspects of [HystrixObservableCommand]'s. Should not be used directly, rather refer to [hystrixObservableCommand].
 */
class KystrixObservableCommand<T> : KystrixBase() {

    private lateinit var command: () -> Observable<T?>
    private var fallback: (() -> Observable<T?>)? = null

    /**
     * Define the actual logic that will be called by Hystrix and thus wrapped in a circuit-breaker. This is typically an HTTP or database request.
     *
     * @param block The code block that will be executed by Hystrix.
     * @see HystrixObservableCommand.construct
     */
    fun command(block: () -> Observable<T?>) {
        command = block
    }

    /**
     * Define the logic that will be called when the circuit-breaker is open.
     *
     * @param block The code block that will be executed by Hystrix when the circuit-breaker is open.
     * @see HystrixObservableCommand.resumeWithFallback
     */
    fun fallback(block: () -> Observable<T?>) {
        fallback = block
    }

    /**
     * Build the [HystrixObservableCommand]. Should not be used directly, rather use the DSL [hystrixObservableCommand].
     */
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

/**
 * Class used in the Kystrix DSL to define aspects of [HystrixCommand]'s. Should not be used directly, rather refer to [hystrixCommand].
 */
class KystrixCommand<T> : KystrixBase() {

    private lateinit var command: () -> T?
    private var threadPoolKey: HystrixThreadPoolKey? = null
    private var definedFallback: (() -> T?)? = null
    private var threadPoolProperties: HystrixThreadPoolProperties.Setter = HystrixThreadPoolProperties.Setter()

    /**
     * Define the actual logic that will be called by Hystrix and thus wrapped in a circuit-breaker. This is typically an HTTP or database request.
     *
     * @param block The code block that will be executed by Hystrix.
     * @see HystrixCommand.run
     */
    fun command(block: () -> T?) {
        command = block
    }

    /**
     * Define the logic that will be called when the circuit-breaker is open.
     *
     * @param block The code block that will be executed by Hystrix when the circuit-breaker is open.
     * @see HystrixCommand.getFallback
     */
    fun fallback(block: () -> T?) {
        definedFallback = block
    }

    /**
     * Specify the thread-pool key, see [HystrixThreadPoolKey].
     */
    fun threadPoolKey(key: HystrixThreadPoolKey) {
        threadPoolKey = key
    }

    /**
     * Specify the thread-pool key as a String, see [HystrixThreadPoolKey].
     */
    fun threadPoolKey(key: String) {
        threadPoolKey(HystrixThreadPoolKey.Factory.asKey(key))
    }

    /**
     * Configure the Hystrix thread-pool properties, see [HystrixThreadPoolProperties].
     */
    fun threadPoolProperties(block: HystrixThreadPoolProperties.Setter.() -> Unit) {
        threadPoolProperties = HystrixThreadPoolProperties.Setter().apply(block)
    }

    /**
     * Build the [HystrixCommand]. Should not be used directly, rather use the DSL [hystrixCommand].
     */
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