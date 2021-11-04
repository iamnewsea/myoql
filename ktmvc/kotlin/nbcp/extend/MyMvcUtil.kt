@file:JvmName("MyMvcHelper")
@file:JvmMultifileClass

package nbcp.web

import nbcp.base.mvc.HttpContext

/**
 * 在中英环境下，返回多语言信息， 使用字典是比较麻烦的， 使用如下方式
 *
 * "服务器错误" lang "server error"
 */
infix fun String.lang(englishMessage: String): String {
    var lang = HttpContext.request.getAttribute("lang")?.toString() ?: "cn"
    if (lang == "en") return englishMessage;
    return this;
}

