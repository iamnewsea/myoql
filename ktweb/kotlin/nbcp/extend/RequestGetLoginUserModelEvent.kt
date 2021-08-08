package nbcp.extend

import nbcp.db.LoginUserModel
import org.springframework.context.ApplicationEvent
import javax.servlet.http.HttpServletRequest

/**
 * @param source: request
 */
class RequestGetLoginUserModelEvent(var request: HttpServletRequest) : ApplicationEvent(request) {

    public var loginUser: LoginUserModel? = null
}


class RequestSetLoginUserModelEvent(var request: HttpServletRequest) : ApplicationEvent(request) {

    //返回 true 表示已处理过，系统直接返回。
    public var proced = false;
}