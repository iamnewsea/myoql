package nbcp.base.utils

import nbcp.base.comm.config
import nbcp.base.comm.const
import nbcp.base.extend.*
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class KotlinCoderUtil {
    companion object {
        /**
         * 如果存在， 就有隔行。 如果不存在就返回空。
         */
        @JvmStatic
        fun getAnnotationCodes(annotations: Array<out Annotation>): List<String> {
            return annotations
                .sortedBy { it::annotationClass.name }
                .map { an ->
                    return@map getAnnotationCode(an)
                }
                .filter { it.HasValue }

        }

        val memberComparator: Comparator<String> =
            compareBy(
                { 0 - it.filter { it.isUpperCase() }.length },
                { it.length },
                { it })


        @JvmStatic
        @JvmOverloads
        fun getAnnotationCode(an: Annotation, isRoot: Boolean = true): String {
            if (an is Metadata) return "";
            if (an is Proxy == false) {
                throw RuntimeException("非 Proxy!")
            }

            var members = an.getMemberValues()
//            var h = Proxy.getInvocationHandler(an);
//            var members = ReflectUtil.getValueByWbsPath(h, "memberValues") as Map<String, Any?>?;
//            if (members == null) return "";


            var ret = "";

            if (isRoot) {
                ret += "@"
            }

            ret += an.annotationClass.qualifiedName


            if (members.any() == false) {
                return ret;
            }

            var list = members
                .keys
                .sortedWith(memberComparator)
                .map { key ->
                    var v = members.get(key)!!;

                    return@map """${key} = ${getValueString(v)}"""
                }

            return ret + "(" + list.joinToString(", ") + ")"
        }

        private fun getValueString(value: Any): String {
            if (value is Class<*>) {
                return """${value.name}::class"""
            } else if (value is Annotation) {
                return getAnnotationCode(value, false)
            }

            var v_type = value::class.java;
            if (v_type.IsStringType) {
                return """${"\""}""${value.AsString()}""${"\""}"""
            } else if (v_type.IsNumberType) {
                return value.AsString()
            } else if (v_type.IsBooleanType) {
                return value.AsString().lowercase()
            } else if (v_type.isArray) {
                return "arrayOf(" + (value as Array<Any>).map { getValueString(it) }
                    .joinToString(", ") + ")"
            } else if (v_type.IsCollectionType) {
                return "listOf(" + (value as List<Any>).map { getValueString(it) }
                    .joinToString(", ") + ")"
            } else if (v_type.isAssignableFrom(Map::class.java)) {
                throw RuntimeException("不识别Map")
            }

            var args =
                v_type.AllFields.map { return@map it.name + " = " + getValueString(it.get(value)) }
                    .joinToString(", ")
            //对象
            return v_type.name + "(" + args + ")"
        }
    }


    var models: MutableSet<ClassCodeData> = mutableSetOf<ClassCodeData>()

    fun addModelCode(type: Class<*>) {
        if (type.IsSimpleType()) return;
        if (type.IsMapType) return; //应该递归找值类
        if (type.IsCollectionType) return;

        var ret = ClassCodeData();
        ret.packageName = type.`package`.name;
        ret.className = type.simpleName;
        ret.content = """
class ${ret.className}{
${type.AllFields.map { "lateinit var " + it.name + ":" + it.type }.joinToString("\n").ToTab(1)}
}
        """;


        models.add(ret);
        type.AllFields.forEach { addModelCode(it.type) }
    }


    fun Method.getInterfaceMethodCode(): MethodCodeData {
        var ret = MethodCodeData();
        ret.content = """
${getAnnotationCodes(this.annotations).map { const.line_break + it }.joinToString("")}
fun ${this.name}(${
            this.parameters.map {
                return@map getAnnotationCodes(it.annotations).joinToString(" ") + " " + it.name + ":" + it.type.typeName
            }.joinToString(",")
        }):${this.returnType.name}
    """


        this.parameters.forEach { addModelCode(it.type) }

        return ret;
    }


    data class ClassCodeData(
        var className: String = "",
        var packageName: String = "",
        var content: String = "",
    ) {
        override fun toString(): String {
            return content;
        }
    }

    data class MethodCodeData(
        var methodName: String = "",
        var content: String = ""
    ) {
        override fun toString(): String {
            return content;
        }
    }

    /**
     * 根据Mvc的类，自动生成 Feign风格的代码
     */
    @JvmOverloads
    fun getFeignClientCode(type: Class<*>, name: String = ""): ClassCodeData {
        var beanName = name;
        if (beanName.isEmpty()) {
            beanName = type.simpleName;
        }

        var ret = ClassCodeData();
        ret.className = "";
        if (beanName.endsWith("Controller")) {
            ret.className = beanName.Slice(0, 0 - "Controller".length) + "FeignClient";
        } else {
            ret.className = beanName + "FeignClient";
        }


        ret.packageName = type.`package`.name.replace(".mvc.", ".client.")
            .replace(".web.", ".client.")

        if (ret.packageName.contains(".client.") == false) {
            ret.packageName += ".client"
        }

        var mvcs = type.methods
            .filter {
                it.annotations.any {
                    it.annotationClass.qualifiedName!!.IsIn(
                        "org.springframework.web.bind.annotation.RequestMapping",
                        "org.springframework.web.bind.annotation.PostMapping",
                        "org.springframework.web.bind.annotation.GetMapping",
                        "org.springframework.web.bind.annotation.PutMapping",
                        "org.springframework.web.bind.annotation.DeleteMapping"
                    )
                }
            }

        ret.content = """

@FeignClient("${config.applicationName}")
interface ${ret.className} {
${
            mvcs.map {
                return@map it.getInterfaceMethodCode()
            }.joinToString("\n")
        }
}
"""

        return ret;
    }
}