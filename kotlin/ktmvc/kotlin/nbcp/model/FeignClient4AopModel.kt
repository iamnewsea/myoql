//package nbcp.model
//
//import feign.Target
//
//import org.aspectj.lang.ProceedingJoinPoint
//import org.aspectj.lang.reflect.MethodSignature
//import org.springframework.cloud.openfeign.FeignClient
//import org.springframework.web.bind.annotation.PathVariable
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RequestParam
//import nbcp.base.comm.StringMap
//import java.lang.reflect.Parameter
//import java.lang.reflect.Proxy
//
///**
// * Created by yuxh on 2018/8/23
// */
//https://blog.csdn.net/Jeson0725/article/details/70226461
//class FeignClient4AopModel {
//    var url: String = ""
//        private set
//
//    var postBody: String = ""
//        private set
//
//    var stackClassName: String = ""
//        private set
//
//    var stackMethodName: String = ""
//        private set
//
//    var stackInfo: Array<StackTraceElement> = arrayOf()
//        private set
//
//    companion object {
//        fun load(aopData: ProceedingJoinPoint): FeignClient4AopModel {
//            var ret = FeignClient4AopModel()
//
//            var feignServerUrl = getFeignUrl(aopData);
//            if (feignServerUrl.isEmpty()) {
//                return ret;
//            }
//
//            val method = (aopData.signature as MethodSignature).method
//            val requestMapping = method.getAnnotation(RequestMapping::class.java)
//            if (requestMapping == null) {
//                return ret;
//            }
//
//            ret.stackMethodName = method.name;
//            ret.stackClassName = method.declaringClass.name
//            ret.stackInfo = Thread.currentThread().stackTrace
//            ret.url = feignServerUrl + requestMapping.value[0]
//
//            val paramData = getFeignMethodParameters(aopData, method.parameters)
//
//            var requestParameters = paramData.first
//            var pathVariable = paramData.second
//            ret.postBody = paramData.third;
//
//            if (requestParameters.any()) {
//                ret.url += "?" + requestParameters.keys.map { it -> it + "=" + requestParameters[it] }.joinToString("&")
//            }
//
//            pathVariable.keys.forEach { pathVariableItemKey ->
//                ret.url = ret.url.replace("{$${pathVariableItemKey}}", pathVariable.get(pathVariableItemKey)!!)
//            }
//
//            return ret
//        }
//
//
//        private fun getFeignMethodParameters(aopData: ProceedingJoinPoint, parameters: Array<Parameter>): Triple<StringMap, StringMap, String> {
//            var requestParameters = StringMap();
//            var pathVariable = StringMap();
//            var body = ""
//
//            parameters.forEachIndexed { index, parameter ->
//                val value = aopData.args[index]
//                var value_String = ""
//                if (value != null) {
//                    value_String = value.toString()
//                }
//
//
//                val param = parameter.getAnnotation(RequestParam::class.java)
//                if (param != null) {
//                    if (param.value == null) {
//                        body = value_String
//                        return@forEachIndexed
//                    }
//
//                    requestParameters.set(param.value, value_String);
//                    return@forEachIndexed
//                }
//
//
//                val variable = parameter.getAnnotation(PathVariable::class.java)
//                if (variable != null) {
//                    pathVariable.set(variable.value, value_String);
//                    return@forEachIndexed
//                }
//            }
//
//            return Triple(requestParameters, pathVariable, body);
//        }
//
//        private fun getFeignUrl(aopData: ProceedingJoinPoint): String {
//            val handler = Proxy.getInvocationHandler(aopData.target)
//
//            val handler_target = handler.javaClass.getDeclaredField("target")
//            handler_target.isAccessible = true
//            val v = handler_target.get(handler) as Target.HardCodedTarget<*>
//
//            val feignClient = v.type().getAnnotation(FeignClient::class.java)
//            if (feignClient == null) {
//                return "";
//            }
//            return v.url()
//        }
//    }
//}