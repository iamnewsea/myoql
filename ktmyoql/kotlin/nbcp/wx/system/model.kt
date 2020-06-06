package nbcp.wx.system


data class wx_msg_return_data(
        var errcode: Int = 0,
        var errmsg: String = ""
)

data class wx_msg_data_value(
        var value: String = "",
        var color: String = "#000000"
)

data class wx_msg_data(
        var touser: String = "",// 	是 	接收者（用户）的 openid
        var template_id: String = "",// 	是 	所需下发的模板消息的id
        var page: String = "",// 	否 	点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转。
        //var form_id: String = "",// 	是 	表单提交场景下，为 submit 事件带上的 formId；支付场景下，为本次支付的 prepay_id
        var data: LinkedHashMap<String, wx_msg_data_value> = LinkedHashMap<String, wx_msg_data_value>()// 	是 	模板内容，不填则下发空模板
)