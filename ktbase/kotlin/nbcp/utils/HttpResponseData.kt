package nbcp.utils

import nbcp.comm.AsString
import nbcp.comm.StringMap
import nbcp.comm.basicSame
import java.io.DataInputStream

class HttpResponseData {

    /**
     * 回发的原始内容。处理回发文本
     */
    var resultBody: String = ""

    /**
     * 回发回调，处理下载大文件。
     */
    var resultAction: ((DataInputStream) -> Unit)? = null


    /**
     * 回发内容是否是文字
     */
    val resultIsText: Boolean
        get() {
            return HttpUtil.getTextTypeFromContentType(this.contentType)
        }

    var contentType: String = ""
        internal set;

    /**
     * 该次回发Header，只读 ,全小写
     */
    var headers: StringMap = StringMap()
        internal set;

    /**
     * 回发的编码，只读
     */
    val charset: String
        get() {
            var char_parts = this.contentType.AsString().split(";").last().split("=");
            if (char_parts.size == 2) {
                if (char_parts[0].trim() basicSame "charset") {
                    return char_parts[1];
                }
            }
            return "UTF-8"
        }
}