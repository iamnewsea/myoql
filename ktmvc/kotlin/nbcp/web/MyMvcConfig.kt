//package nbcp.web
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import nbcp.comm.DefaultMyJsonMapper
//import nbcp.comm.JsonStyleEnumScope
//import org.springframework.context.annotation.Configuration
//import org.springframework.http.converter.HttpMessageConverter
//import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
//import org.springframework.web.method.support.HandlerMethodArgumentResolver
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
//
//
//@Configuration
//class MyMvcConfig : WebMvcConfigurer {
//    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
//        resolvers.add(0,RequestParameterConverter())
//        super.addArgumentResolvers(resolvers)
//    }
//
//    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
//        converters.add(0,MyMappingMessageConverter(DefaultMyJsonMapper.get(JsonStyleEnumScope.GetSetStyle, JsonStyleEnumScope.IgnoreNull, JsonStyleEnumScope.Compress)))
//        super.configureMessageConverters(converters)
//    }
//}
//
//
//class MyMappingMessageConverter(mapper:ObjectMapper): MappingJackson2HttpMessageConverter(mapper) {
//}