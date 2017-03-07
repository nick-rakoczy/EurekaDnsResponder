package threads

import org.apache.commons.logging.LogFactory
import java.util.LinkedList
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.logging.LogManager
import java.util.logging.Logger
import kotlin.concurrent.thread

class ThreadPool(poolSize: Int, prefix: String) : Executor, Runnable {
	private val log = LogFactory.getLog(ThreadPool::class.java)

	private val threads = (0..poolSize).map {
		thread(name = "$prefix #$it") { run() }
	}.forEach {
		log.info("Started Thread: ${it.name}")
	}

	private val commandQueue = LinkedList<Runnable>()

	override fun execute(command: Runnable) {
		commandQueue.offer(command)
	}

	override fun run() {
		forever {
			while (commandQueue.isEmpty()) {
				Thread.sleep(100)
			}

			commandQueue.poll().run()
		}
	}

	companion object {
		fun forever(block: () -> Unit): Nothing {
			while (true) {
				block.invoke()
			}

			throw ExecutionException("forever block exited", null)
		}
	}
}


