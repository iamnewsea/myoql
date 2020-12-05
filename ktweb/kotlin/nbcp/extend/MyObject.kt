@file:JvmName("MyWebHelper")
@file:JvmMultifileClass

package nbcp.web

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.LoginUserModel
import nbcp.db.db
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest


/**
 * 高并发系统不应该有Session。使用token即可。
 * 另外，由于跨域 SameSite 的限制，需要避免使用 Cookie 的方式。
 * 设置 getLoginUserFunc 在需要用户信息的时候获取。示例代码：
 * 获取用户 token 使用 db.rer_base.getLoginInfoFromToken
 */
var HttpServletRequest.LoginUser: LoginUserModel
    get() {
        var ret = this.getAttribute("[LoginUser]") as LoginUserModel?;
        if (ret != null) {
            return ret;
        }

        if (WebUserTokenBeanInstance.instance == null) {
            ret = this.session.getAttribute("[LoginUser]") as LoginUserModel?;

            if (ret != null) {
                return ret;
            }

            return LoginUserModel();
        }


        var token = this.tokenValue;

        ret = WebUserTokenBeanInstance.instance!!.getUserInfo(token);
        if (ret == null) {
            ret = LoginUserModel.ofToken(token);
        }

        this.LoginUser = ret!!;
        return ret;
    }
    set(value) {
        if (WebUserTokenBeanInstance.instance == null) {
            this.session.setAttribute("[LoginUser]", value);
            return;
        }

        this.setAttribute("[LoginUser]", value)
        db.rer_base.userSystem.saveLoginUserInfo(value);
    }


val HttpServletRequest.UserId: String
    get() {
        return this.LoginUser.id;
    }

val HttpServletRequest.LoginName: String
    get() {
        return this.LoginUser.loginName;
    }

val HttpServletRequest.UserName: String
    get() {
        return this.LoginUser.name;
    }


/**
 * 由于 SameSite 限制，避免使用 Cookie，定义一个额外值来保持会话。使用 app.token-key 定义。
 */
val HttpServletRequest.tokenValue: String
    get() {
        if (WebUserTokenBeanInstance.instance == null) {
            return this.requestedSessionId;
        }
        var cacheKey = "_Token_Value_";
        var token = this.getAttribute(cacheKey).AsStringWithNull();
        if (token.HasValue) {
            return token.AsString();
        }

        token = this.findParameterValue(config.tokenKey).AsStringWithNull();

        if (token.isNullOrEmpty()) {
            token = generateToken();
        } else {
            if (token.startsWith(tokenPrefix)) {
                var tokenTime: LocalDateTime? = null;
                try {
                    tokenTime = CodeUtil.getDateTimeFromCode(token.substring(tokenPrefix.length));
                } catch (e: Exception) {
                    logger.error("token格式非法:" + e.message);
                }

                if (tokenTime != null) {
                    var now = LocalDateTime.now();

                    var diffSeconds = (now - tokenTime).totalSeconds
                    if (diffSeconds > config.tokenKeyExpireSeconds) {
                        db.rer_base.userSystem.deleteToken(token);
                        token = generateToken();
                    } else if (diffSeconds > config.tokenKeyRenewalSeconds) {
                        var newToken = generateToken();
                        WebUserTokenBeanInstance.instance!!.changeToken(token, newToken);
                        token = newToken;
                    }
                } else {
                    token = generateToken()
                }
            }
        }

        this.setAttribute(cacheKey, token)
        HttpContext.response.setHeader(config.tokenKey, token)
        return token;
    }


