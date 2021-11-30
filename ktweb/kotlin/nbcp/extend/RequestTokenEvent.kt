package nbcp.extend

import org.springframework.context.ApplicationEvent
import javax.servlet.http.HttpServletRequest


//source = request
class RequestGetTokenEvent(var request: HttpServletRequest,var token:String) : ApplicationEvent(request) {
}