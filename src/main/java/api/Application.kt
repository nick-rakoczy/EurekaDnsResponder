package api

import com.netflix.eureka.api.Instance
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "application")
class Application {
	@XmlElement(required = true)
	lateinit var name: String

	@XmlElement(required = true)
	lateinit var instance: MutableList<Instance>
}