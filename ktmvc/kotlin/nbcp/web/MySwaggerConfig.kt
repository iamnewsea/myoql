package nbcp.web

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 * 很奇怪，这个配置放到单独的Jar包里，swagger-ui.html 就不能访问，
 * 把下面的配置放到启动项目里，就起作用。
 * Spring Boot中只能有一个WebMvcConfigurationSupport配置类，所以使用 WebMvcConfigurer
 */
@Configuration
open class MySwaggerConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/statics/**")
            .addResourceLocations("classpath:/statics/");
        // 解决 SWAGGER 404报错
        registry.addResourceHandler("/swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/springfox-swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
    }
}

