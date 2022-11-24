package nbcp.mvc.mvc


import nbcp.base.annotation.*
import nbcp.base.comm.const
import nbcp.base.exception.ParameterInvalidException
import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import nbcp.mvc.annotation.*
import org.slf4j.LoggerFactory
import org.springframework.core.MethodParameter
import org.springframework.core.Ordered
import org.springframework.core.io.InputStreamSource
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.HandlerMapping
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession


/**
 * 自定义Mvc参数解析,有如下规则:
 * 1. 不支持默认值
 * 2. 数值类型, string , 不传递也会有默认值.
 * 3. 由于cao蛋的擦除机制，不支持List泛型参数，使用 Array泛型。
 * 4. 只解析没有注解的参数, 或 JsonModel 注解的参数。
 */
class JsonModelParameterConverter() : HandlerMethodArgumentResolver, Ordered {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val parameterType = parameter.parameterType;
        if (ServletRequest::class.java.isAssignableFrom(parameterType)) return false
        if (ServletResponse::class.java.isAssignableFrom(parameterType)) return false
        if (HttpSession::class.java.isAssignableFrom(parameterType)) return false
        if (InputStreamSource::class.java.isAssignableFrom(parameterType)) return false

        //系统注解
        if (parameter.hasParameterAnnotation(PathVariable::class.java)) return false;
        if (parameter.hasParameterAnnotation(CookieValue::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestHeader::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestParam::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestBody::class.java)) return false;
        if (parameter.hasParameterAnnotation(SessionAttribute::class.java)) return false;
        if (parameter.hasParameterAnnotation(ModelAttribute::class.java)) return false;
        if (parameter.hasParameterAnnotation(RequestPart::class.java)) return false;


//        if (parameter.hasParameterAnnotation(JsonModel::class.java)) return true;

//        if (parameterType.IsCollectionType) return true;
//        if (parameterType.IsMapType) return true;
//        if (parameterType.IsSimpleType()) return true;
        return true
    }

//    private fun getMyRequest(request: HttpServletRequest): HttpServletRequest? {
//        if (request is HttpServletRequest) {
//            return request;
//        }
//        if ((request is ServletRequestWrapper) == false) {
//            return null
//        }
//
//        var requestWrapper = request as ServletRequestWrapper
//        if (requestWrapper.request == null) {
//            return null;
//        }
//
//        return getMyRequest(requestWrapper.request as HttpServletRequest)
//    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        nativeRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        if (mavContainer == null || binderFactory == null) return null
        //现在只支持ServletWebRequest
        val webRequest = (nativeRequest as ServletWebRequest).request
        var value: Any? = getValueFromRequest(webRequest, parameter)


        //查值最后一步。 转值
        if (value == null) {
            translateAliasParam(webRequest, parameter).apply {
                if (this != null) {
                    return this;
                }
            }
        }

        if (parameter.parameterType.IsStringType) {
            var strValue = "";
            if (value != null) {
                var value_type = value::class.java;
                if (value_type.IsSimpleType()) {
                    strValue = value.AsString().trim()
                } else {
                    throw RuntimeException("参数 ${parameter.parameterName} 数据异常! 要求字符串!")
                }
            }

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
                var defNumberValue = parameter.getParameterAnnotation(DefaultNumberValue::class.java)
                if (defNumberValue != null) {
                    value = defNumberValue.value
                } else {
                    value = MyUtil.getTypeDefaultValue(parameter.parameterType);
                }
            }
        }

        if (value == null) {
            checkRequire(parameter, webRequest);
        }

        //如果是列表。
        if (parameter.parameterType.IsCollectionType) {
            var genType = (parameter.genericParameterType as ParameterizedType).GetActualClass(0);

            if (value == null) {
                return null;
            }

            if (value is String) {
                var strValue = value.AsString();

                if (genType.isEnum) {
                    value = strValue.split(",").filter { it.HasValue }.map { it.ToEnum(genType) }
                } else if (genType.IsStringType) {
                    value = strValue.split(",")
                } else if (genType.IsNumberType) {
                    value = strValue.split(",").map { it.ConvertType(genType) }
                }
            }

            try {
                value = value.ConvertType(parameter.parameterType, genType)
            } catch (e: Exception) {
                throw RuntimeException("参数 ${parameter.parameterName} 数据异常! 要求集合类型!")
            }

            if (value is Collection<*> && value.size == 0) {
                checkRequire(parameter, webRequest);
            }
            return value;
        } else if (parameter.parameterType.isArray) {
            if (value == null) {
                return null;
            }

            var genType = parameter.parameterType.componentType;
            if (value is String) {
                var strValue = value.AsString();

                if (genType.isEnum) {
                    value = strValue.split(",").filter { it.HasValue }.map { it.ToEnum(genType) }
                } else if (genType.IsStringType) {
                    value = strValue.split(",")
                } else if (genType.IsNumberType) {
                    value = strValue.split(",").map { it.ConvertType(genType) }
                }
            }

            try {
                value = value.ConvertType(parameter.parameterType)
            } catch (e: Exception) {
                throw RuntimeException("参数 ${parameter.parameterName} 数据异常! 要求数组类型!")
            }


            if (value is Array<*> && value.size == 0) {
                checkRequire(parameter, webRequest);
            }
            return value;
        }

        //转换枚举Map之类的。
        if (value == null) {
            return null;
        }

        try {
            return value.ConvertType(parameter.parameterType);
        } catch (e: Exception) {
            throw RuntimeException("参数 ${parameter.parameterName} 数据异常! 要求 ${parameter.parameterType.simpleName} 类型!")
        }
    }

    /**
     * 处理参数别名
     */
    private fun translateAliasParam(webRequest: HttpServletRequest, parameter: MethodParameter): Any? {

        if (parameter.parameterName == "skip") {
            val pageNumber = (getValueFromRequest(webRequest, parameter, "pageNumber") ?: getValueFromRequest(
                webRequest,
                parameter,
                "pageNo"
            )).AsInt(-1)
            val pageSize = getValueFromRequest(webRequest, parameter, "pageSize").AsInt(-1)

            if (pageNumber > 0 && pageSize > 0) {
                return (pageNumber - 1) * pageSize
            }

            return null;
        }

        if (parameter.parameterName == "take") {
            val pageNumber = getValueFromRequest(webRequest, parameter, "pageSize").AsInt(-1)
            if (pageNumber > 0) {
                return pageNumber
            }
            return null;
        }

        var kName = MyUtil.getKebabCase(parameter.parameterName);
        if (kName != parameter.parameterName) {
            return getValueFromRequest(webRequest, parameter, kName)
        }


        return null;
    }

    private fun getValueFromRequest(
        webRequest: HttpServletRequest,
        parameter: MethodParameter,
        parameterName: String = ""
    ): Any? {

        var parameterNameLocal = parameterName;
        if (parameterNameLocal.isEmpty()) {
            parameterNameLocal = parameter.parameterName
        }

//        val myRequest = getMyRequest(webRequest);
        //获取 PathVariable 的值
        var value: Any? =
            (webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as Map<String, Any?>?)?.get(
                parameterNameLocal
            );

        if (value == null && webRequest.queryString != null) {
            value = getFromQuery(webRequest, parameter, parameterNameLocal);
        }

        if (value == null) {
            val jsonModelValue = parameter.getParameterAnnotation(JsonModel::class.java)
            if (jsonModelValue != null) {
                //如果用 JsonModel 接收 String 等简单参数？
                val postBody = webRequest.postBody?.toString(const.utf8) ?: ""
                if (postBody.HasValue) {
                    if (parameter.parameterType.isArray) {
                        return postBody.ConvertType(parameter.parameterType);
                    }
                    if (parameter.parameterType.IsCollectionType) {
                        var p1Type = (parameter.genericParameterType as ParameterizedType).GetActualClass(0)
                        return postBody.FromListJson(p1Type);
                    }
                    return postBody.ConvertType(parameter.parameterType);
                } else {
                    return MyUtil.getTypeDefaultValue(parameter.parameterType);
                }
            }

            value = webRequest.getPostJson().get(parameterNameLocal)
        }

        if (value == null) {
            value = webRequest.getHeader(parameterNameLocal)
        }

        return value;
    }

    private fun checkRequire(parameter: MethodParameter, webRequest: HttpServletRequest) {
        var require = parameter.getParameterAnnotation(nbcp.base.annotation.Require::class.java)
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
        throw ParameterInvalidException(parameter.parameterName!!)
    }

    private fun getFromQuery(webRequest: HttpServletRequest, parameter: MethodParameter, parameterName: String): Any? {
        val queryMap = webRequest.queryJson

        var parameterNameLocal = parameterName;
        if (parameterNameLocal.isEmpty()) {
            parameterNameLocal = parameter.parameterName;
        }


        var value = queryMap.get(parameterNameLocal)

        if (value == null) {
            //判断是否是 solo
            if (parameter.parameterType.IsBooleanType
                && webRequest.soloQueryKeys.contains(parameterNameLocal)
            ) {
                return true;
            }
            return null;
        }
        //如果得到了多个值。进行转换。
        if (value is Collection<*>) {
            //如果参数是 List.
            if (parameter.parameterType.isArray) {
                if (parameter.parameterType.componentType.IsStringType) {
                    value = value.toTypedArray()
                } else {
                    value = value.map { it!!.ConvertType(parameter.parameterType.componentType) }
                        .toTypedArray()
                }
            } else if (parameter.parameterType.IsCollectionType) {
                var genType = (parameter.genericParameterType as ParameterizedType).GetActualClass(0);
                if (!genType.IsStringType) {

                    value = value.map { it!!.ConvertType(genType) }
                }
            } else if (parameter.parameterType.IsStringType) {
                value = value.joinToString(",")
            }

            return value;
        }


        //如果得到了一个值，但是参数是 List.
        if (parameter.parameterType.isArray) {
            val genType = parameter.parameterType.componentType
            if (genType.IsSimpleType() && value::class.java.IsStringType) {
                return value.AsString()
                    .split(",")
                    .filter { it.HasValue }
                    .map { it.ConvertType(genType) }
            }

            value = arrayOf(value.ConvertType(genType))
        } else if (parameter.parameterType.IsCollectionType) {
            val genType = (parameter.genericParameterType as ParameterizedType).GetActualClass(0);
            if (genType.IsSimpleType() && value::class.java.IsStringType) {
                return value.AsString()
                    .split(",")
                    .filter { it.HasValue }
                    .map { it.ConvertType(genType) }
            }

            value = mutableListOf(value.ConvertType(genType))
        } else if (!parameter.parameterType.IsStringType) {
            value = value.ConvertType(parameter.parameterType)
        }

        return value;
    }

    override fun getOrder(): Int {
        return Ordered.HIGHEST_PRECEDENCE + 10000;
    }

}