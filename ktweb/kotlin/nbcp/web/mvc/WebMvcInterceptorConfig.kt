package nbcp.web.mvc

//import org.springframework.session.web.http.DefaultCookieSerializer
//import org.springframework.messaging.converter.StringMessageConverter
import nbcp.web.mvc.interceptor.DefaultWebInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 * Created by udi on 2017.3.11.
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class WebMvcInterceptorConfig : WebMvcConfigurer {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Autowired
    lateinit var defaultWebInterceptor: DefaultWebInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(defaultWebInterceptor)
            .order(-5)
            .addPathPatterns("/*")
            .addPathPatterns("/**")
    }
}