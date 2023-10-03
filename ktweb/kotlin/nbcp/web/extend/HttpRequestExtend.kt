package nbcp.web.extend

import io.jsonwebtoken.Jwts
import nbcp.base.comm.config
import nbcp.base.db.LoginNamePasswordData
import nbcp.base.db.LoginUserModel
import nbcp.base.event.GetLoginUserInfoEvent
import nbcp.base.extend.AsString
import nbcp.base.extend.AsStringWithNull
import nbcp.base.extend.HasValue
import nbcp.base.extend.findParameterKey
import nbcp.base.utils.*
import nbcp.mvc.sys.MvcContext
import nbcp.mvc.sys.findParameterStringValue
import org.springframework.http.HttpHeaders
import javax.servlet.http.HttpServletRequest


/**
 * 高并发系统不应该有Session。使用token即可。
 * 另外，由于跨域 SameSite 的限制，需要避免使用 Cookie 的方式。
 * 设置 getLoginUserFunc 在需要用户信息的时候获取。示例代码：
 * 获取用户 token 使用 db.rer_base.getLoginInfoFromToken
 * @see userAuthenticationService.saveLoginUserInfo
 */
val HttpServletRequest.LoginUser: LoginUserModel
    get() {
        var ret = this.getAttribute("[LoginUser]") as LoginUserModel?;
        if (ret != null) {
            return ret;
        }

        var token = this.tokenValue;

        var ev = GetLoginUserInfoEvent(token);
        SpringUtil.context.publishEvent(ev);
        ret = ev.result;

        if (ret == null) {
            ret = LoginUserModel().apply { this.token = token }
        }

        if (ret.id.HasValue) {
            this.setAttribute("[LoginUser]", ret)
        }
        return ret;
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
 * 从Jwt中获取用户Id
 */
fun getJwtUserId(value: String): String {
    if (config.jwtSecretKey.isEmpty()) return "";

    var claims = Jwts.parser()
            .setSigningKey(config.jwtSecretKey)
            .parseClaimsJws(value)
            .getBody();

    return claims.findParameterKey("userId").AsString()
}

/**
 * 获取基本认证的用户名密码
 */
val HttpServletRequest.basicLoginNamePassword: LoginNamePasswordData
    get() {
        var key = org.apache.http.HttpHeaders.AUTHORIZATION
        var value = this.getHeader(key);
        if (value == null) {
            return LoginNamePasswordData();
        }
        var value_decript = Base64Util.decodeBase64Utf8(value);
        return HttpUtil.getLoginNamePassword(value_decript)
    }

/**
 * 由于 SameSite 限制，避免使用 Cookie，定义一个额外值来保持会话。使用 app.token-key 定义。
 */
val HttpServletRequest.tokenValue: String
    get() {
        var cacheKey = "[Token_Value]";
        var token = this.getAttribute(cacheKey).AsStringWithNull();
        if (token.HasValue) {
            return token.AsString();
        }


        token = this.findParameterStringValue(config.tokenKey).AsString {
            //获取自定义 Authorization 中的 token,值必须以 st!开头。和 token 保持一致，redis key 不能有空格。
            var authorization = this.findParameterStringValue(HttpHeaders.AUTHORIZATION);

            if (authorization.startsWith("Bearer ")) {
                var value = authorization.substring("Bearer ".length)

                if (value.split(".").size == 3) {
                    var userId = getJwtUserId(value)
                    if (userId.HasValue) {
                        return@AsString userId;
                    }
                }
            }

            return@AsString "";
        }

        if (token.isEmpty()) {
            token = TokenUtil.generateToken();
            MvcContext.response.setHeader(config.tokenKey, token)
        }

        var event = RequestGetTokenEvent(this, token);
        SpringUtil.context.publishEvent(event);
        if (event.token.HasValue) {
            token = event.token
        }

        this.setAttribute(cacheKey, token)

        return token;
    }
