package eureka

import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.beans.Introspector
import javax.annotation.PostConstruct
import kotlin.reflect.memberProperties
import kotlin.reflect.primaryConstructor

@Component
class Config(
		@Value("\${tcp.port:53}")
		val tcpPort: Int,

		@Value("\${tcp.backlog:1024}")
		val tcpBacklog: Int,

		@Value("\${tcp.threads:4}")
		val tcpThreadCount: Int,

		@Value("\${udp.port:53}")
		val udpPort: Int,

		@Value("\${udp.threads:4}")
		val udpThreadCount: Int,

		@Value("\${eureka.tld:eureka}")
		val eurekaTld: String,

		@Value("\${eureka.baseUrl}")
		val eurekaBaseUrl: String,

		@Value("\${eureka.ttl:60}")
		val eurekaTtl: Long
) {
	@PostConstruct
	fun report() {
		val log = LogFactory.getLog(Config::class.java)
		Config::class.primaryConstructor?.parameters?.map {
			val memberName = it.name
			val key = it.annotations
					.mapNotNull { it as Value }
					.firstOrNull()
					?.value
					?.let {
						it.removeSurrounding(prefix = "\${", suffix = "}").substringBeforeLast(":")
					}
			val value = Config::class.memberProperties
					.filter { it.name == memberName }
					.singleOrNull()
					?.get(this)
			log.info("$key: $value")
		}
	}
}