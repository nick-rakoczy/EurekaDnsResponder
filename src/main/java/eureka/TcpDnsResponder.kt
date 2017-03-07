package eureka

import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Component
import org.xbill.DNS.Message
import org.xbill.DNS.Name
import threads.ThreadPool
import threads.ThreadPool.Companion.forever
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import kotlin.concurrent.thread

@Component
class TcpDnsResponder(private val eurekaResolver: EurekaResolver,
					  private val config: Config) : Runnable {
	private val log = LogFactory.getLog(TcpDnsResponder::class.java)

	private val server = config.run {
		ServerSocket(tcpPort, tcpBacklog)
	}

	private val eurekaTld by lazy {
		Name.fromString(config.eurekaTld)
	}

	private val threadPool by lazy {
		ThreadPool(poolSize = config.tcpThreadCount, prefix = "TcpDns Thread")
	}

	override fun run() {
		forever {
			val socket = server.accept()
			threadPool.execute { process(socket) }
		}
	}

	fun process(s: Socket) {
		s.use { client ->
			val inputStream = client.inputStream.let(::DataInputStream)
			val len = inputStream.readUnsignedShort()
			val payload = ByteArray(size = len)
			inputStream.readFully(payload)

			val query = Message(payload)
			val response = eurekaResolver.process(query).toWire()
			val outputStream = client.outputStream.let(::DataOutputStream)
			outputStream.writeShort(response.size)
			outputStream.write(response)
		}
	}

	@PostConstruct
	fun start() {
		log.info("Starting TcpDnsOperator Thread")
		thread(block = {run()}, name = "TcpDnsOperator")
	}
}