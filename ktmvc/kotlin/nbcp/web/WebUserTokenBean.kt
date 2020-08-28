package nbcp.web

import nbcp.db.LoginUserModel

/**
 * 用户TokenBean，必须有一个
 */
interface WebUserTokenBean {
    /**
     * token发生改变，但是依然有效。
     */
    fun changeToken(token: String, newToken: String);

    fun getUserInfo(token: String): LoginUserModel?

}