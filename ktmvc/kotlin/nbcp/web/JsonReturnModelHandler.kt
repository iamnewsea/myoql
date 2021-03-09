//package nbcp.web
//
//
//import org.springframework.core.MethodParameter
//import org.springframework.web.bind.annotation.*
//import org.springframework.web.bind.support.WebDataBinderFactory
//import org.springframework.web.context.request.NativeWebRequest
//import org.springframework.web.context.request.ServletWebRequest
//import org.springframework.web.method.support.HandlerMethodArgumentResolver
//import org.springframework.web.method.support.ModelAndViewContainer
//import nbcp.comm.*
//import nbcp.utils.*
//import org.slf4j.LoggerFactory
//import org.springframework.web.method.support.HandlerMethodReturnValueHandler
//import org.springframework.web.servlet.HandlerMapping
//import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
//import java.lang.RuntimeException
//import java.lang.reflect.Method
//import javax.servlet.ServletRequest
//import javax.servlet.ServletRequestWrapper
//import javax.servlet.ServletResponse
//import javax.servlet.http.HttpServletRequest
//import javax.servlet.http.HttpServletResponse
//import javax.servlet.http.HttpSession
//
//
///**
// * 自定义Mvc参数解析,有如下规则:
// * 1. 不支持默认值
// * 2. 基本类型, string , 不传递也会有默认值.
// * 3. 如果定义了可空参数,需要默认值, 重写参数 var  productIds = productIds ?: mutableListOf<String>()
// * 4. 只解析没有注解的参数,有任何注解,都不使用该方式.
// */
//class JsonReturnModelHandler() : HandlerMethodReturnValueHandler {
//    companion object {
//        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
//    }
//
//    override fun supportsReturnType(methodParameter: MethodParameter): Boolean {
//        var jsonModel = methodParameter.method.getAnnotation(JsonReturnModel::class.java)
//        if (jsonModel == null) return false
//        if (jsonModel.value.java.isInterface) return false
//        return true
//    }
//
//    override fun handleReturnValue(
//        value: Any?,
//        methodParameter: MethodParameter,
//        mvc: ModelAndViewContainer,
//        nativeRequest: NativeWebRequest
//    ) {
//        mvc.isRequestHandled = true
//        var jsonModel = methodParameter.method.getAnnotation(JsonReturnModel::class.java)
//
//        val request: HttpServletRequest = nativeRequest.getNativeRequest(HttpServletRequest::class.java)
//
//        var value2 = jsonModel.value.java.newInstance().apply(request,value)
//        if (value2 == null) return;
//
//        var value2_type = value2::class.java
//        var value2_str = ""
//
//        if (value2_type.IsSimpleType()) {
//            value2_str = value2.AsString()
//        } else {
//            value2_str = value2.ToJson()
//        }
//
//        if (value2_str.isNotEmpty()) {
//            val response: HttpServletResponse = nativeRequest.getNativeResponse(HttpServletResponse::class.java)
//            response
//                .writer
//                .write(value2_str)
//        }
//    }
//
//
//}