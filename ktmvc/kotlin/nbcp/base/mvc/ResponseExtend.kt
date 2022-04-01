@file:JvmName("MyMvcHelper")
@file:JvmMultifileClass

package nbcp.base.mvc

import nbcp.comm.*
import org.springframework.http.MediaType
import nbcp.utils.*
import org.springframework.web.servlet.HandlerMapping
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


fun ServletResponse.WriteXmlRawValue(xml: String) {
    this.contentType = MediaType.TEXT_XML_VALUE;
    this.outputStream.write(xml.toByteArray(const.utf8));
}

fun ServletResponse.WriteJsonRawValue(json: String) {
    this.contentType = MediaType.APPLICATION_JSON_VALUE;
    this.outputStream.write(json.toByteArray(const.utf8));
}

fun ServletResponse.WriteTextValue(text: String) {
    this.contentType = "text/plain;charset=UTF-8";
    this.outputStream.write(text.toByteArray(const.utf8));
}

fun ServletResponse.WriteHtmlValue(text: String) {
    this.contentType = "text/html;charset=UTF-8";
    this.outputStream.write(text.toByteArray(const.utf8));
}

fun ServletResponse.WriteHtmlBodyValue(text: String) {
    this.contentType = "text/html;charset=UTF-8";
    this.outputStream.write(
        """<!DOCTYPE html>
<html lang="en">
<head><meta charset="utf-8"></head>
<body>${text}</body>
</html>""".toByteArray(const.utf8)
    );
}

fun HttpServletResponse.setDownloadFileName(fileName: String) {
    this.setHeader("Content-Disposition", "attachment; filename=" + JsUtil.encodeURIComponent(fileName));
    this.contentType = "application/octet-stream"
}


val HttpServletResponse.IsOctetContent: Boolean
    get() {
        return WebUtil.contentTypeIsOctetContent(this.contentType.AsString())
    }


/**
 * 输出 javascript ，通过 window.parent.postMessage 函数呼叫父窗口，弹出消息 用于前端下载时处理消息。
 * 前端环境：
 *  1. 调用 jv.download() 函数，原理是通过页面的iframe,打开下载页面。
 *  2. 在主页面添加 window.addEventListener('message',e=>{}) 处理函数。
 * @param msg: 错误消息
 * @param title: 消息标题
 */
@JvmOverloads
fun HttpServletResponse.parentAlert(msg: String, title: String = "", targetOrigin: String = "*") {
    /**
     * <pre>{@code
     * window.addEventListener('message',e=>{
     *      if( e.data.event == 'error') {
     *          jv.error.apply(jv, e.data.arguments)
     *      }
     *  });
     *  }
     *  </pre>
     */
    this.WriteHtmlValue("<script>window.parent.postMessage({event:'error',arguments:['${msg}','${title}']},'${targetOrigin}')</script>")
}
