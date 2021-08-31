@file:JvmName("MyWebHelper")
@file:JvmMultifileClass

package nbcp.web

import nbcp.comm.*
import nbcp.data.TokenStorageTypeEnum
import nbcp.utils.*
import nbcp.db.LoginUserModel
import nbcp.extend.RequestGetLoginUserModelEvent
import nbcp.extend.RequestSetLoginUserModelEvent
import nbcp.extend.RequestTokenEvent
import nbcp.service.UserAuthenticationService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest

private val logger = LoggerFactory.getLogger("ktweb.MyWebHelper")

val HttpServletRequest.userAuthenticationService by lazy {
    return@lazy SpringUtil.getBean<UserAuthenticationService>();
}


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

        var event = RequestGetLoginUserModelEvent(this);
        SpringUtil.context.publishEvent(event);
        if (event.loginUser != null) return event.loginUser!!;

        var token = this.tokenValue;
        if (config.tokenStorage == TokenStorageTypeEnum.Memory) {
            return this.session.getAttribute("[LoginUser]") as LoginUserModel? ?: LoginUserModel(token);
        }

        ret = userAuthenticationService.getLoginInfoFromToken(token)
        if (ret == null) {
            ret = LoginUserModel(token);
        }

        this.setAttribute("[LoginUser]", ret)
        return ret;
    }
    set(value) {
        this.setAttribute("[LoginUser]", value)

        var event = RequestSetLoginUserModelEvent(this);
        SpringUtil.context.publishEvent(event);
        if (event.proced) return;

        if (config.tokenStorage == TokenStorageTypeEnum.Memory) {
            this.session.setAttribute("[LoginUser]", value);
            return;
        }

        this.userAuthenticationService.saveLoginUserInfo(value);
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

        var event = RequestTokenEvent(this);
        SpringUtil.context.publishEvent(event);
        if (event.tokenValue.HasValue) return event.tokenValue;

        token = this.findParameterStringValue(config.tokenKey).AsString {
            //获取自定义 Authorization 中的 token,值必须以 st!开头。和 token 保持一致，redis key 不能有空格。
            var authorization = this.findParameterStringValue("Authorization");
            if (authorization.startsWith("st!")) {
                return@AsString authorization.substring(3);
            }
            return@AsString "";
        }


//        else {
//            var tokenTime: LocalDateTime? = TokenUtil.getTokenCreateTime(token);
//
//            if (tokenTime == null) {
//                newToken = TokenUtil.generateToken()
//            } else {
//                var now = LocalDateTime.now();
//
//                var diffSeconds = (now - tokenTime).seconds
//                if (diffSeconds > config.tokenKeyExpireSeconds) {
//                    this.userAuthenticationService.deleteToken(token);
//                    newToken = TokenUtil.generateToken();
//                } else {
//                    newToken = token
//                }
//            }
//        }

        if (token.isEmpty()) {
            token = TokenUtil.generateToken();
            HttpContext.response.setHeader(config.tokenKey, token)
        }

        this.setAttribute(cacheKey, token)

        return token;
    }



