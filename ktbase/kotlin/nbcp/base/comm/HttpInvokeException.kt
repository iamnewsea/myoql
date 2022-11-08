package nbcp.base.comm

class HttpInvokeException(var status: Int, msg: String) : Exception(msg) {

}