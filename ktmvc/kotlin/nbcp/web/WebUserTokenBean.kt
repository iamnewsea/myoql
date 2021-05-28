package nbcp.web

import nbcp.db.LoginUserModel
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

/**
 * 用户TokenBean，必须有一个
 */
interface WebUserTokenBean {
    /**
     * token发生改变，但是依然有效。
     */
    fun changeToken(token: String, newToken: String);

    fun getUserInfo(token: String): LoginUserModel?

//    fun tokenLoginUser(userInfo:LoginUserModel)
//
//    fun lostToken(token:String)
}


@Component
class WebUserTokenBeanInstance : BeanPostProcessor {
    companion object {
        //如果没有配置WebUserTokenBean,那么使用session
        var instances: MutableList<WebUserTokenBean> = mutableListOf()
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is WebUserTokenBean) {
            instances.add(bean)
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }
}