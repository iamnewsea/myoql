package nbcp.config

import nbcp.base.mvc.MyHttpRequestWrapper
import nbcp.base.mvc.MyHttpResponseWrapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiKey
import springfox.documentation.service.AuthorizationScope
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket

/**
 * 很奇怪，这个配置放到单独的Jar包里，swagger-ui.html 就不能访问，
 * 把下面的配置放到启动项目里，就起作用。
 * Spring Boot中只能有一个WebMvcConfigurationSupport配置类，所以使用 WebMvcConfigurer
 */
@Configuration
open class MySwaggerConfig : WebMvcConfigurer {
    @Value("\${app.swagger.basePackage:}")
    var basePackage: String = ""

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        if (basePackage.isEmpty()) return;
        registry.addResourceHandler("/statics/**")
            .addResourceLocations("classpath:/statics/");
        // 解决 SWAGGER 404报错
        registry.addResourceHandler("/swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/springfox-swagger-ui/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/");
    }

    @Bean
    @ConditionalOnProperty("app.swagger.basePackage")
    open fun petApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
            .useDefaultResponseMessages(false)
            .ignoredParameterTypes(*arrayOf(MyHttpRequestWrapper::class.java, MyHttpResponseWrapper::class.java))
            .select()
            .apis(RequestHandlerSelectors.basePackage(basePackage))
            .paths(PathSelectors.any())
            .build()
            .pathMapping("/")
            .securitySchemes(mutableListOf(ApiKey("token", "token", "header")))
            .securityContexts(mutableListOf(securityContext()))
    }

    private fun securityContext(): SecurityContext {
        return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .build();
    }

    private fun defaultAuth(): List<SecurityReference> {
        var authorizationScope = AuthorizationScope("global", "accessEverything");
        var authorizationScopes = mutableListOf<AuthorizationScope>();
        authorizationScopes.add(authorizationScope);
        return mutableListOf(SecurityReference("token", authorizationScopes.toTypedArray()));
    }
}