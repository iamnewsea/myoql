package nbcp

import com.fasterxml.jackson.databind.ObjectMapper
import nbcp.comm.JsonStyleEnumScope
import nbcp.comm.utf8
import nbcp.component.WebJsonMapper
import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import nbcp.web.*
import org.springframework.boot.jackson.JsonComponent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter


@Configuration
@DependsOn("springUtil")
open class MyMvcOrmInit : ApplicationListener<ContextRefreshedEvent> {
    companion object {
        private var _inited = false;

        fun isInited(): Boolean {
            var tmp = _inited;
            _inited = true;
            return tmp
        }
    }

    val handlerAdapter: RequestMappingHandlerAdapter by lazy {
        return@lazy SpringUtil.getBean<RequestMappingHandlerAdapter>();
    }


    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        if (isInited()) return;
        initMvcRequest()
    }

    private fun initMvcRequest() {
        var listResolvers = mutableListOf<HandlerMethodArgumentResolver>()
        listResolvers.add(JsonModelParameterConverter());
        listResolvers.addAll(handlerAdapter.argumentResolvers ?: listOf())

        handlerAdapter.argumentResolvers = listResolvers;

//        var listReturnHandlers = mutableListOf<HandlerMethodReturnValueHandler>()
//        listReturnHandlers.add(JsonReturnModelHandler())
//        listReturnHandlers.addAll(handlerAdapter.returnValueHandlers ?: listOf())
//
//        handlerAdapter.returnValueHandlers = listReturnHandlers

        val initializer = handlerAdapter.webBindingInitializer as ConfigurableWebBindingInitializer
        if (initializer.conversionService != null) {
            val genericConversionService = initializer.conversionService as GenericConversionService
            genericConversionService.addConverter(StringToDateConverter())
            genericConversionService.addConverter(StringToLocalDateConverter())
            genericConversionService.addConverter(StringToLocalTimeConverter())
            genericConversionService.addConverter(StringToLocalDateTimeConverter())
        }

        //处理请求的消息体。
        handlerAdapter.messageConverters.forEach { converter ->
            if (converter is MappingJackson2HttpMessageConverter) {
                converter.defaultCharset = utf8
                converter.objectMapper = SpringUtil.getBean<WebJsonMapper>()
                return@forEach
            }

            if (converter is StringHttpMessageConverter) {
                converter.setWriteAcceptCharset(false);
                converter.defaultCharset = utf8;
            }

            if (converter is AllEncompassingFormHttpMessageConverter) {
                converter.setCharset(utf8)

                (MyUtil.getPrivatePropertyValue(
                    converter,
                    "partConverters"
                ) as Collection<*>).forEach foreach2@{ sub_conveter ->

                    if (sub_conveter is MappingJackson2HttpMessageConverter) {
                        sub_conveter.defaultCharset = utf8
                        sub_conveter.objectMapper = SpringUtil.getBean<WebJsonMapper>()
                    }
                    return@foreach2
                }
            }
            return@forEach
        }
    }
}


//@JsonComponent
//class MyMvcJsonSerializerManage {
//    @Bean
//    fun jacksonObjectMapper(): ObjectMapper {
//        return DefaultMyJsonMapper.get(
//            JsonStyleEnumScope.GetSetStyle,
//            JsonStyleEnumScope.IgnoreNull,
//            JsonStyleEnumScope.Compress
//        )
//    }
//}


