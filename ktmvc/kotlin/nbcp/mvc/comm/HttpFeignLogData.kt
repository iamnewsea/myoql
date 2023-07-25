package nbcp.mvc.comm;

import nbcp.base.comm.StringKeyMap
import nbcp.base.comm.StringMap


class HttpFeignLogData {
    var requestUrl = ""
    var status = 0;
    var responseBody = "";
    var requestHeaders = mapOf<String,String>();
    var responseHeaders = mapOf<String,String>();
}
