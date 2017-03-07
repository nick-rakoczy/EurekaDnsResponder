package eureka

import com.netflix.eureka.api.Instance
import org.springframework.stereotype.Component
import org.xbill.DNS.ARecord
import org.xbill.DNS.Message
import org.xbill.DNS.Name
import org.xbill.DNS.PTRRecord
import org.xbill.DNS.Record
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.Section
import util.mapReduce
import java.net.InetAddress

@Component
class EurekaResolver(private val config: Config, private val eurekaClient: EurekaClient) {
	private val eurekaTld by lazy { config.eurekaTld }

	fun process(message: Message): Message {
		val response = message.clone() as Message
		return response.apply {
			val isEurekaService = question.name.toString(true).endsWith(eurekaTld)
			if (isEurekaService) {
				val clientInstances = eurekaClient.getClientInstances(question.name.toString(true).removeSuffix(eurekaTld).removeSuffix("."))
				clientInstances.mapReduce<Instance, Record> { it, emit ->
					ARecord(question.name, question.dClass, config.eurekaTtl, InetAddress.getByName(it.ipAddr)).let(emit)
					SRVRecord(question.name, question.dClass, config.eurekaTtl, 1, 1, it.port.toInt(), Name.fromString(it.hostName + ".")).let(emit)
				}.forEach {
					response.addRecord(it, Section.ANSWER)
				}
				response.header.setFlag(org.xbill.DNS.Flags.QR.toInt())
			} else {
				response.header.apply {
					this.rcode = 3
					this.setFlag(org.xbill.DNS.Flags.QR.toInt())
				}
			}
		}
	}
}