package nbcp

import nbcp.base.utils.SpringUtil
import nbcp.comm.GetSetTypeJsonMapper
import nbcp.comm.JavascriptDateModule
import nbcp.comm.utf8
import nbcp.web.RequestParameterConverter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter

@Configuration
@DependsOn("springUtil")
open class PzxMvcOrmInit : ApplicationListener<ContextRefreshedEvent> {

    val handerAdapter: RequestMappingHandlerAdapter by lazy {
        return@lazy SpringUtil.getBean<RequestMappingHandlerAdapter>();
    }


//    @Autowired
//    lateinit var converter: MappingJackson2HttpMessageConverter


    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        initMvcRequest()

        initMvcResponse()
    }


    private fun initMvcRequest() {
//        genericConversionService.addConverter(Date2LocalDateTimeConverter())

        var listResolvers = mutableListOf<HandlerMethodArgumentResolver>()
        listResolvers.add(RequestParameterConverter());

        listResolvers.addAll(handerAdapter.argumentResolvers ?: listOf())

        handerAdapter.argumentResolvers = listResolvers;
    }


    private fun initMvcResponse() {
        //注册返回的消息体。
        handerAdapter.messageConverters.filter { it is MappingJackson2HttpMessageConverter }.map { it as MappingJackson2HttpMessageConverter }.forEach {
            it.objectMapper = GetSetTypeJsonMapper.instance
        }

        //设置 StringHttpMessageConverter 的字符集
        handerAdapter.messageConverters.filter { it is StringHttpMessageConverter }.map { it as StringHttpMessageConverter }.forEach {
            it.setWriteAcceptCharset(false)
            it.defaultCharset = utf8
        }
    }
}
