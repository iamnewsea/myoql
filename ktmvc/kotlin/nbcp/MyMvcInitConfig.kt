package nbcp

import nbcp.base.*
import nbcp.base.mvc.ServletBeanProcessor
import nbcp.comm.const
import nbcp.component.WebJsonMapper
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties
import org.springframework.context.annotation.Import
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.http.converter.AbstractHttpMessageConverter
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.util.unit.DataSize
import org.springframework.util.unit.DataUnit

//@Component
@Import(ServletBeanProcessor::class)
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
                bean.maxFileSize = DataSize.of(256, DataUnit.MEGABYTES)
            }

            if (bean.maxRequestSize.toMegabytes() <= 10) {
                bean.maxRequestSize = DataSize.of(260, DataUnit.MEGABYTES)
            }
        }
//        else {
//            var beanType = bean::class.java
//            if (beanType.name == "org.springframework.cloud.gateway.config.HttpClientProperties") {
//                var connectTimeout = beanType.getDeclaredField("connectTimeout")
//
//                if (MyUtil.getPrivatePropertyValue(bean, connectTimeout).AsInt() < 1000) {
//                    MyUtil.setPrivatePropertyValue(bean, connectTimeout, 3000)
//                }
//
//                var responseTimeout = beanType.getDeclaredField("responseTimeout")
//                var responseTimeout_value = MyUtil.getPrivatePropertyValue(bean, responseTimeout) as Duration
//                if (responseTimeout_value.toMinutes() < 1) {
//                    MyUtil.setPrivatePropertyValue(bean, responseTimeout, Duration.ofMinutes(3))
//                }
//            }
//        }

        return super.postProcessBeforeInitialization(bean, beanName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is HttpMessageConverters) {
            var webJsonMapper = SpringUtil.getBean<WebJsonMapper>();
            bean.converters.forEach { converter ->
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
                        }
                        return@foreach2
                    }
                }
                return@forEach
            }
        } else if (bean is GenericConversionService) {
            bean.addConverter(StringToDateConverter())
            bean.addConverter(StringToLocalDateConverter())
            bean.addConverter(StringToLocalTimeConverter())
            bean.addConverter(StringToLocalDateTimeConverter())
        }

        return super.postProcessAfterInitialization(bean, beanName);
    }


    /**
     * 在所有Bean初始化之前执行
     */
    private fun init_app() {

    }

    /**
     * 系统预热之后，最后执行事件。
     */
//    @EventListener
//    fun app_started(event: ApplicationStartedEvent) {
//    }

}