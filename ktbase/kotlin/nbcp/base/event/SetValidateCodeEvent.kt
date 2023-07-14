package nbcp.base.event

import org.springframework.context.ApplicationEvent

/**
 * 获取用户信息事件
 *
 */
class SetValidateCodeEvent(var token: String) : ApplicationEvent(token) {

    /**
     * 结果
     */
    var result: String = ""
        get() {
            return field;
        }
        set(value) {
            field = value
        }
}