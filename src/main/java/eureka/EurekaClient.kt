package eureka

import api.Application
import com.netflix.eureka.api.Instance
import okhttp3.OkHttpClient
import okhttp3.Request
import org.springframework.stereotype.Component
import java.io.StringReader
import java.net.InetAddress
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller

@Component
class EurekaClient(private val config: Config) {
	fun getClientInstances(appId: String): List<Instance> {
		val client = OkHttpClient()
		val request = Request.Builder()
				.header("Accept", "application/xml")
				.url("${config.eurekaBaseUrl}/apps/$appId")
				.get()
				.build()

		val call = client.newCall(request)
		val responseXml = call.execute().body().string()
		val unmarshaller = getJaxbDeserializer()
		val bundle = unmarshaller.unmarshallString<Application>(responseXml)
		return bundle.instance
	}

	private inline fun <reified T : Any> Unmarshaller.unmarshallString(s: String): T {
		return StringReader(s).let {
			this.unmarshal(it)
		}.let {
			it as T
		}
	}

	private fun getJaxbDeserializer() = JAXBContext.newInstance(Application::class.java).let {
		it.createUnmarshaller()
	}
}