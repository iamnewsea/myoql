package nbcp.base.event

import nbcp.base.db.LoginUserModel
import org.springframework.context.ApplicationEvent

/**
 * 获取用户信息事件
 *
 */
class GetLoginUserInfoEvent(var token: String) : ApplicationEvent(token) {
    /**
     * 结果
     */
    var result: LoginUserModel? = null
        get() {
            return field;
        }
        set(value) {
            field = value
            this.procDone = true
        }

    /**
     * 是否处理完成标志
     */
    var procDone: Boolean = false
}