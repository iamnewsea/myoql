//package nbcp.web
//
//import org.springframework.context.annotation.Configuration
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
//
//
/**
 * 很奇怪，这个配置放到单独的Jar包里，swagger-ui.html 就不能访问，
 * 把下面的配置放到启动项目里，就起作用。
 */
//@Configuration
//open class MySwaggerConfig : WebMvcConfigurationSupport() {
//    @Override
//    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
//        registry.addResourceHandler("/statics/**").addResourceLocations("classpath:/statics/");
//        // 解决 SWAGGER 404报错
//        registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//}

