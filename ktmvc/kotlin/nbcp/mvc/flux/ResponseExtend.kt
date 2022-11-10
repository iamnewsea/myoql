//package nbcp.base.flux
//
//
//import nbcp.base.comm.*
//import org.springframework.http.MediaType
//import nbcp.base.utils.*
//import org.springframework.http.server.reactive.ServerHttpResponse
//import reactor.core.publisher.Mono
//
//
//fun ServerHttpResponse.WriteXmlRawValue(xml: String) {
//    this.headers.contentType = MediaType.TEXT_XML;
//    this.writeWith { Mono.just(xml.toByteArray(const.utf8)) }
//}
//
//fun ServerHttpResponse.WriteJsonRawValue(json: String) {
//    this.headers.contentType = MediaType.APPLICATION_JSON;
//    this.writeWith { Mono.just(json.toByteArray(const.utf8)) };
//}
//
//fun ServerHttpResponse.WriteTextValue(text: String) {
//    this.headers.contentType = MediaType.TEXT_PLAIN;
//    this.writeWith { Mono.just(text.toByteArray(const.utf8)) };
//}
//
//fun ServerHttpResponse.WriteHtmlValue(text: String) {
//    this.headers.contentType = MediaType.TEXT_HTML
//    this.writeWith { Mono.just(text.toByteArray(const.utf8)) };
//}
//
//fun ServerHttpResponse.WriteHtmlBodyValue(text: String) {
//    this.headers.contentType = MediaType.TEXT_HTML
//    this.writeWith {
//        Mono.just(
//                """<!DOCTYPE html>
//<html lang="en">
//<head><meta charset="utf-8"></head>
//<body>${text}</body>
//</html>""".toByteArray(const.utf8)
//        )
//    }
//}
//
//fun ServerHttpResponse.setDownloadFileName(fileName: String) {
//    this.headers.set("Content-Disposition", "attachment; filename=" + JsUtil.encodeURIComponent(fileName));
//    this.headers.contentType = MediaType.APPLICATION_OCTET_STREAM
//}
//
//
//val ServerHttpResponse.IsOctetContent: Boolean
//    get() {
//        return WebUtil.contentTypeIsOctetContent(this.headers.contentType.AsString())
//    }
//
//
///**
// * 输出 javascript ，通过 window.parent.postMessage 函数呼叫父窗口，弹出消息 用于前端下载时处理消息。
// * 前端环境：
// *  1. 调用 jv.download() 函数，原理是通过页面的iframe,打开下载页面。
// *  2. 在主页面添加 window.addEventListener('message',e=>{}) 处理函数。
// * @param msg: 错误消息
// * @param title: 消息标题
// */
//@JvmOverloads
//fun ServerHttpResponse.parentAlert(msg: String, title: String = "", targetOrigin: String = "*") {
//    /**
//     * <pre>{@code
//     * window.addEventListener('message',e=>{
//     *      if( e.data.event == 'error') {
//     *          jv.error.apply(jv, e.data.arguments)
//     *      }
//     *  });
//     *  }
//     *  </pre>
//     */
//    this.WriteHtmlValue("<script>window.parent.postMessage({event:'error',arguments:['${msg}','${title}']},'${targetOrigin}')</script>")
//}
