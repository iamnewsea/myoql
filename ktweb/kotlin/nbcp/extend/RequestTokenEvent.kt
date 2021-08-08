package nbcp.extend

import org.springframework.context.ApplicationEvent
import javax.servlet.http.HttpServletRequest


//source = request
class RequestTokenEvent(var request: HttpServletRequest) : ApplicationEvent(request) {

    public var tokenValue: String = "";
}