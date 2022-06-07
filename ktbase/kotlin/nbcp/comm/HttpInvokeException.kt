package nbcp.comm

class HttpInvokeException(var status: Int, msg: String) : Exception(msg) {

}