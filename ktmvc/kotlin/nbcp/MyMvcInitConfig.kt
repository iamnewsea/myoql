package nbcp

import nbcp.base.mvc.*
import nbcp.comm.Important
import nbcp.comm.clazzesIsSimpleDefine
import nbcp.comm.const
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.component.WebJsonMapper
import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import nbcp.web.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.event.ApplicationStartingEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.http.converter.AbstractHttpMessageConverter
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.support.AllEncompassingFormHttpMessageConverter
import org.springframework.scheduling.annotation.SchedulingConfiguration
import org.springframework.stereotype.Component
import org.springframework.util.unit.DataSize
import org.springframework.util.unit.DataUnit
import org.springframework.web.bind.support.ConfigurableWebBindingInitializer
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import java.util.function.Consumer

@Component
@Import(value = [SpringUtil::class])
class MyMvcInitConfig : BeanPostProcessor {
    companion object {
        private var inited = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (inited == false) {
            inited = true;

            init_app();
        }

        /**
         * 默认情况下， maxFileSize = 1Mb ,maxRequestSize = 10Mb
         *
         */
        if (bean is MultipartProperties) {
            if (bean.maxFileSize.toMegabytes() <= 1) {
                bean.maxFileSize = DataSize.of(100, DataUnit.MEGABYTES)
            }

            if (bean.maxRequestSize.toMegabytes() <= 10) {
                bean.maxRequestSize = DataSize.of(120, DataUnit.MEGABYTES)
            }
        }

        return super.postProcessBeforeInitialization(bean, beanName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)


        return ret;
    }


    /**
     * 在所有Bean初始化之前执行
     */
    private fun init_app() {

    }

    /**
     * 系统预热之后，最后执行事件。
     */
    @EventListener
    fun onApplicationReady(event: ApplicationReadyEvent) {
        initMvcRequest()
    }


    val handlerAdapter: RequestMappingHandlerAdapter by lazy {
        return@lazy SpringUtil.getBean<RequestMappingHandlerAdapter>();
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

        var webJsonMapper = SpringUtil.getBean<WebJsonMapper>();
        //处理请求的消息体。
        handlerAdapter.messageConverters.forEach { converter ->
            if (converter is AbstractHttpMessageConverter) {
                converter.defaultCharset = const.utf8
            }

            //解决绝大多数Json转换问题
            if (converter is MappingJackson2HttpMessageConverter) {
                converter.defaultCharset = const.utf8
                converter.objectMapper = webJsonMapper
                return@forEach
            }

            if (converter is StringHttpMessageConverter) {
                converter.setWriteAcceptCharset(false);
                converter.defaultCharset = const.utf8;
            }

            if (converter is FormHttpMessageConverter) {
                converter.setCharset(const.utf8)

                converter.partConverters.forEach foreach2@{ sub_conveter ->
                    if (sub_conveter is AbstractJackson2HttpMessageConverter) {
                        sub_conveter.defaultCharset = const.utf8

                        //不能设置，因为不同的子类，需要不同的 Factory
//                        sub_conveter.objectMapper = webJsonMapper
                    }
                    return@foreach2
                }
            }
            return@forEach
        }
    }

}