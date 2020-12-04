@file:JvmName("MyWebHelper")
@file:JvmMultifileClass

package nbcp.web

import nbcp.comm.*
import org.springframework.http.MediaType
import nbcp.utils.*
import nbcp.db.LoginUserModel
import nbcp.db.db
import org.springframework.web.servlet.HandlerMapping
import java.time.LocalDateTime
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


private val webUserToken: WebUserTokenBean by lazy {
    return@lazy SpringUtil.getBean<WebUserTokenBean>()
}

/**
 * 高并发系统不应该有Session。使用token即可。
 * 另外，由于跨域 SameSite 的限制，需要避免使用 Cookie 的方式。
 * 设置 getLoginUserFunc 在需要用户信息的时候获取。示例代码：
 * 获取用户 token 使用 db.rer_base.getLoginInfoFromToken
 */
var HttpServletRequest.LoginUser: LoginUserModel
    get() {
        /**
        getLoginUserFunc 示例代码：
        nbcp.web.getLoginUserFunc = af@{
        var token = it;
        if (token.startsWith("sf")) {
        var time = CodeUtil.getDateTimeFromCode(token.substring(3));
        if ((LocalDateTime.now() - time).totalHours > 7) {
        return@af null;
        }
        }
        return@af LoginUserModel.loadFromToken(token)
        }
         */

        var ret = this.getAttribute("[LoginUser]") as LoginUserModel?;
        if (ret != null) {
            return ret;
        }

        var token = this.tokenValue;

        ret = webUserToken.getUserInfo(token);
        if (ret == null) {
            ret = LoginUserModel.ofToken(token);
        }

        this.LoginUser = ret;
        return ret;
    }
    set(value) {
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
                        webUserToken.changeToken(token, newToken);
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


