package nbcp.comm

import nbcp.scope.*


fun Array<out JsonStyleEnumScope>.getDateFormat(): String = this.toList().getDateFormat()
/**
 */
fun Collection<JsonStyleEnumScope>.getDateFormat(): String {
    if (this.contains(JsonStyleEnumScope.DateUtcStyle)) {
        return "yyyy-MM-dd'T'HH:mm:ss'Z'"
    } else if (this.contains(JsonStyleEnumScope.DateLocalStyle)) {
        return "yyyy/MM/dd HH:mm:ss"
    } else {
        return "yyyy-MM-dd HH:mm:ss";
    }
}


