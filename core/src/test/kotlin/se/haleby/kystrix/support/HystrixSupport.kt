package se.haleby.kystrix.support

import com.netflix.config.ConfigurationManager
import com.netflix.hystrix.Hystrix
import org.junit.rules.ExternalResource
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Reset the Hystrix to its default values before and after the test.
 *
 *
 * This is useful in situations where you need to change a Hystrix property between two different tests. Hystrix caches properties
 * the first time they are applied which means that even though it looks like you've changed properties the old ones are applied.
 */
class HystrixSupport : ExternalResource() {
    private val cleanUp = CopyOnWriteArraySet<() -> Unit>()

    override fun before() {
        Hystrix.reset()
    }

    fun forceOpen(circuitBreakName: String, forceOpened: Boolean = true): HystrixSupport {
        doForceOpen(circuitBreakName, forceOpened)
        cleanUp.add { doForceOpen(circuitBreakName, false) }
        return this
    }

    fun timeout(circuitBreakName: String, timeout: Long): HystrixSupport {
        require(timeout > 0) { "Timeout must be > 0" }
        val previousTimeout = ConfigurationManager.getConfigInstance().getLong("hystrix.command.$circuitBreakName.execution.isolation.thread.timeoutInMilliseconds", 5000)
        doSetTimeout(circuitBreakName, timeout)
        cleanUp.add { doSetTimeout(circuitBreakName, previousTimeout) }
        return this
    }


    override fun after() {
        Hystrix.reset()
        cleanUp.forEach(::run)
        cleanUp.clear()
    }

    private fun doSetTimeout(circuitBreakName: String, timeout: Long) {
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.$circuitBreakName.execution.isolation.thread.timeoutInMilliseconds", timeout)
    }

    private fun doForceOpen(circuitBreakName: String, forceOpened: Boolean) {
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.$circuitBreakName.circuitBreaker.forceOpen", forceOpened)
    }
}
