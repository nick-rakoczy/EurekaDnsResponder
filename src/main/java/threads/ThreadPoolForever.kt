package threads

import org.apache.commons.logging.LogFactory
import threads.ThreadPool.Companion.forever
import kotlin.concurrent.thread

class ThreadPoolForever(poolSize: Int, prefix: String, private val block: () -> Unit) {
	private val log = LogFactory.getLog(ThreadPoolForever::class.java)

	private val threads = (0..poolSize).map {
		thread(name = "$prefix #$it") { run() }
	}.forEach {
		log.info("Started Thread: ${it.name}")
	}

	private fun run() {
		forever { block() }
	}
}