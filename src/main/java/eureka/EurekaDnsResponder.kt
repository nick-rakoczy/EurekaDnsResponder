package eureka

import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan(basePackageClasses = arrayOf(EurekaDnsResponder::class))
@PropertySource("/application.properties")
class EurekaDnsResponder {
	companion object {
		@JvmStatic
		fun main(args: Array<String>) {
			SpringApplication.run(EurekaDnsResponder::class.java, *args)
		}
	}
}