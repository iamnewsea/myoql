package nbcp.base

import java.nio.charset.Charset

/**
 * Created by yuxh on 2018/11/13
 */

val utf8: Charset
    get() {
        return Charset.forName("utf-8")
    }

//换行符符
val line_break: String
    get() = "\r\n";

