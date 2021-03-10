package nbcp.web


import org.springframework.core.MethodParameter
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import nbcp.comm.*
import nbcp.utils.*
import org.slf4j.LoggerFactory
import org.springframework.web.servlet.HandlerMapping
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.RuntimeException
import java.lang.reflect.Method
import javax.servlet.ServletRequest
import javax.servlet.ServletRequestWrapper
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession


/**
 * 自定义Mvc参数解析,有如下规则:
 * 1. 不支持默认值
 * 2. 基本类型, string , 不传递也会有默认值.
 * 3. 如果定义了可空参数,需要默认值, 重写参数 var  productIds = productIds ?: mutableListOf<String>()
 * 4. 只解析没有注解的参数,有任何注解,都不使用该方式.
 */
class JsonModelParameterConverter() : HandlerMethodArgumentResolver {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        if (ServletRequest::class.java.isAssignableFrom(parameter.parameterType)) return false
        if (ServletResponse::class.java.isAssignableFrom(parameter.parameterType)) return false
        if (HttpSession::class.java.isAssignableFrom(parameter.parameterType)) return false

        if (parameter.hasParameterAnnotation(PathVariable::class.java)) return false;
        if (parameter.hasParameterAnnotation(CookieValue::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestHeader::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestParam::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestBody::class.java)) return false;
        if (parameter.hasParameterAnnotation(SessionAttribute::class.java)) return false;
        if (parameter.hasParameterAnnotation(ModelAttribute::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestPart::class.java)) return false;


        if (parameter.hasParameterAnnotation(JsonModel::class.java)) return true;

//        var className = parameter.containingClass.name
//        if (packages.any()) {
//            return packages.any { return@any className.startsWith(it) }
//        }
        return true
    }

    private fun getMyRequest(request: HttpServletRequest): MyHttpRequestWrapper? {
        if (request is MyHttpRequestWrapper) {
            return request;
        }
        if ((request is ServletRequestWrapper) == false) {
            return null
        }

        var requestWrapper = request as ServletRequestWrapper
        if (requestWrapper.request == null) {
            return null;
        }

        return getMyRequest(requestWrapper.request as HttpServletRequest)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        nativeRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        if (mavContainer == null || binderFactory == null) return null
        var webRequest = (nativeRequest as ServletWebRequest).request
        var myRequest = getMyRequest(webRequest);
        var value: Any? = null
        var key = parameter.parameterName


        //获取 PathVariable 的值
        value =
            (webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, Any?>?)?.get(key);

        if (value == null && webRequest.queryString != null) {
            value = getFromQuery(webRequest, parameter);
        }

        if (value == null && myRequest != null) {
            var jsonModelValue = parameter.getParameterAnnotation(JsonModel::class.java)
            if (jsonModelValue != null) {
//                if (jsonModelValue.value.java.isInterface) {
//                    return (myRequest.body ?: byteArrayOf()).toString(utf8).FromJson(parameter.parameterType);
//                } else {
//                    return jsonModelValue.value.java.newInstance().apply(webRequest)
//                }

                return (myRequest.body ?: byteArrayOf()).toString(utf8).FromJson(parameter.parameterType);
            }

            value = myRequest.json.get(key)
        }

        if (value == null) {
            value = webRequest.getHeader(key)
        }

        if (parameter.parameterType.IsStringType) {
            var strValue = value.AsString().trim()

            if (strValue.isEmpty()) {
                checkRequire(parameter, webRequest)
            }

            return strValue;
        }

        if (value == null) {
            if (parameter.parameterType.isArray) {
                value = java.lang.reflect.Array.newInstance(parameter.parameterType.componentType, 0);
            } else if (parameter.parameterType.IsCollectionType) {
                value = listOf<Any>()
            } else if (parameter.parameterType.IsNumberType) {
                value = MyUtil.getSimpleClassDefaultValue(parameter.parameterType);
            }
        }

        if (value == null) {
            checkRequire(parameter, webRequest);
        }

        //如果是列表。
        if (parameter.parameterType.IsCollectionType) {
            var genType = (parameter.genericParameterType as ParameterizedTypeImpl).GetActualClass(0);
            value = value?.ConvertType(parameter.parameterType, genType)
            if (value is Collection<*> && value.size == 0) {
                checkRequire(parameter, webRequest);
            }
            return value;
        } else if (parameter.parameterType.isArray) {
            value = value?.ConvertType(parameter.parameterType)

            if (value is Array<*> && value.size == 0) {
                checkRequire(parameter, webRequest);
            }
            return value;
        }

        //转换枚举、Map之类的。
        return value?.ConvertType(parameter.parameterType);
    }

    private fun checkRequire(parameter: MethodParameter, webRequest: HttpServletRequest) {
        var require = parameter.getParameterAnnotation(Require::class.java)
        if (require == null) {
            return;
        }

        var caller = ""
        if (parameter.executable is Method) {
            var method = parameter.executable as Method
            caller = "${method.name}(${
                method.parameters.map { it.toString() }.joinToString()
            }):${method.returnType.name}"
        } else {
            var method = parameter.executable
            caller = "${method.name}(${method.parameters.map { it.toString() }.joinToString()})"
        }

        logger.Error { require.value.AsString("请求:${webRequest.fullUrl} --> 方法:${caller} 中，找不到参数${parameter.parameterName}") }
        throw RuntimeException("找不到参数${parameter.parameterName}")

    }

    private fun getFromQuery(webRequest: HttpServletRequest, parameter: MethodParameter): Any? {
        var key = parameter.parameterName
        var value: Any? = null
        var queryMap = webRequest.queryJson
        if (queryMap.containsKey(key) == false) {
            return null;
        }

        value = queryMap.get(key)

        if (value == null) {
            if (parameter.parameterType.IsBooleanType) {
                return true;
            }
            return null;
        }

        //如果得到了多个值。进行转换。
        if (value::class.java.IsCollectionType) {
            //如果参数是 List.
            if (parameter.parameterType.isArray) {
                if (parameter.parameterType.componentType.IsStringType) {
                    value = (value as Collection<String>).toTypedArray()
                } else {
                    value = (value as Collection<String>).map { it.ConvertType(parameter.parameterType.componentType) }
                        .toTypedArray()
                }
            } else if (parameter.parameterType.IsCollectionType) {
                var genType = (parameter.genericParameterType as ParameterizedTypeImpl).GetActualClass(0);
                if (!genType.IsStringType) {
                    value = (value as Collection<String>).map { it.ConvertType(genType) }
                }
            } else if (parameter.parameterType.IsStringType) {
                value = (value as Collection<String>).joinToString(",")
            }

            return value;
        }


        //如果得到了一个值，但是参数是 List.
        if (parameter.parameterType.isArray) {
            value = arrayOf(value.ConvertType(parameter.parameterType.componentType))
        } else if (parameter.parameterType.IsCollectionType) {
            var genType = (parameter.genericParameterType as ParameterizedTypeImpl).GetActualClass(0);
            value = listOf(value.ConvertType(genType))
        } else if (parameter.parameterType.IsStringType == false) {
            value = value.ConvertType(parameter.parameterType)
        }


        return value;
    }

}