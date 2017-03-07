package eureka

import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Component
import org.xbill.DNS.Message
import org.xbill.DNS.Name
import threads.ThreadPool
import threads.ThreadPool.Companion.forever
import threads.ThreadPoolForever
import java.net.DatagramPacket
import java.net.DatagramSocket
import javax.annotation.PostConstruct
import kotlin.concurrent.thread

@Component
class UdpDnsResponder(private val eurekaResolver: EurekaResolver,
					  private val config: Config) {
	private val log = LogFactory.getLog(UdpDnsResponder::class.java)

	private val server = config.run {
		DatagramSocket(udpPort)
	}

	private val eurekaTld by lazy {
		Name.fromString(config.eurekaTld)
	}

	private val threadPool by lazy {
		ThreadPoolForever(poolSize = config.udpThreadCount, prefix = "UdpDns Thread") {
			val buffer = ByteArray(size = 512)
			val packetIn = DatagramPacket(buffer, buffer.size).apply {
				this.length = buffer.size
			}

			server.receive(packetIn)
			val query = Message(buffer)
			val response = eurekaResolver.process(query).toWire()
			val packetOut = DatagramPacket(response, response.size, packetIn.address, packetIn.port)
			server.send(packetOut)
		}
	}

	@PostConstruct
	fun start() {
		log.info("Starting UdpDns Threads")
		threadPool
	}
}