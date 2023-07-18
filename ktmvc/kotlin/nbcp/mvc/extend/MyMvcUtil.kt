package nbcp.mvc.extend

import nbcp.mvc.mvc.MvcContext

/**
 * 在中英环境下，返回多语言信息， 使用字典是比较麻烦的， 使用如下方式
 *
 * "服务器错误" lang "server error"
 */
infix fun String.lang(englishMessage: String): String {
    var lang = MvcContext.request.getAttribute("[Lang]")?.toString() ?: "cn"
    if (lang == "en") return englishMessage;
    return this;
}

