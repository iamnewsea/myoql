package nbcp

import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.comm.DefaultMyJsonMapper
import nbcp.comm.JsonStyleEnumScope
import nbcp.comm.utf8
import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import nbcp.web.*
import org.springframework.boot.jackson.JsonComponent
import org.springframework.boot.web.servlet.MultipartConfigFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.util.unit.DataSize
import org.springframework.util.unit.DataUnit
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import javax.servlet.MultipartConfigElement


@Configuration
@DependsOn("springUtil")
open class MyMvcOrmInit : ApplicationListener<ContextRefreshedEvent> {

    val handerAdapter: RequestMappingHandlerAdapter by lazy {
        return@lazy SpringUtil.getBean<RequestMappingHandlerAdapter>();
    }


    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        initMvcRequest()
    }

    private fun initMvcRequest() {

        var listResolvers = mutableListOf<HandlerMethodArgumentResolver>()
        listResolvers.add(RequestParameterConverter());
        listResolvers.addAll(handerAdapter.argumentResolvers ?: listOf())

        handerAdapter.argumentResolvers = listResolvers;


        val initializer = handerAdapter.webBindingInitializer as ConfigurableWebBindingInitializer
        if (initializer.conversionService != null) {
            val genericConversionService = initializer.conversionService as GenericConversionService
            genericConversionService.addConverter(StringToDateConverter())
            genericConversionService.addConverter(StringToLocalDateConverter())
            genericConversionService.addConverter(StringToLocalTimeConverter())
            genericConversionService.addConverter(StringToLocalDateTimeConverter())
        }

        //处理请求的消息体。
        handerAdapter.messageConverters.forEach { converter ->
            if (converter is MappingJackson2HttpMessageConverter) {
                converter.defaultCharset = utf8
                converter.objectMapper = DefaultMyJsonMapper.get(JsonStyleEnumScope.FieldStyle, JsonStyleEnumScope.IgnoreNull, JsonStyleEnumScope.Compress)
                return@forEach
            }

            if (converter is StringHttpMessageConverter) {
                converter.setWriteAcceptCharset(false);
                converter.defaultCharset = utf8;
            }

            if (converter is AllEncompassingFormHttpMessageConverter) {
                converter.setCharset(utf8)

                (MyUtil.getPrivatePropertyValue(converter, "partConverters") as Collection<*>).forEach foreach2@{ sub_conveter ->

                    if (sub_conveter is MappingJackson2HttpMessageConverter) {
                        sub_conveter.defaultCharset = utf8
                        sub_conveter.objectMapper = DefaultMyJsonMapper.get(JsonStyleEnumScope.FieldStyle, JsonStyleEnumScope.IgnoreNull, JsonStyleEnumScope.Compress)
                    }
                    return@foreach2
                }
            }
            return@forEach
        }
    }
}


@JsonComponent
class MyMvcJsonSerializerManage {
    @Bean
    fun jacksonObjectMapper(): ObjectMapper {
        return DefaultMyJsonMapper.get(JsonStyleEnumScope.GetSetStyle, JsonStyleEnumScope.IgnoreNull, JsonStyleEnumScope.Compress)
    }
}


