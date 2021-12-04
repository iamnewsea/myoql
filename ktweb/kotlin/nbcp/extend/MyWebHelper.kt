package nbcp.web

import io.jsonwebtoken.Jwts
import nbcp.base.mvc.HttpContext
import nbcp.comm.*
import nbcp.data.TokenStorageTypeEnum
import nbcp.utils.*
import nbcp.db.LoginUserModel
import nbcp.db.mongo.MongoBaseMetaCollection
import nbcp.db.mongo.MongoColumnName
import nbcp.db.mongo.MongoSetEntityUpdateClip
import nbcp.db.sql.SqlBaseMetaTable
import nbcp.db.sql.SqlSetEntityUpdateClip
import nbcp.extend.RequestGetLoginUserModelEvent
import nbcp.extend.RequestSetLoginUserModelEvent
import nbcp.extend.RequestGetTokenEvent
import nbcp.base.service.UserAuthenticationService
import nbcp.db.LoginNamePasswordData
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import java.io.Serializable
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object MyWebHelper {
    private val logger = LoggerFactory.getLogger(this::class.java)

    /**
     * 把当前请求转向另一个目标，并把目标结果输出。
     */
    @JvmStatic
    fun transform(request: HttpServletRequest, response: HttpServletResponse, targetUrl: String): HttpUtil {
        var http = HttpUtil(targetUrl)
        http.request.requestMethod = request.method
        if (request.method == "POST" || request.method == "PUT") {
            http.setPostBody(request.inputStream.ReadContentStringFromStream())
        }

        run {
            var headerArray = arrayOf("accept", "content-type")
            var requestHeaderNames = request.headerNames.toList()
            headerArray.forEach { headerName ->
                var requestHeaderName = requestHeaderNames.firstOrNull { headerName VbSame it }
                if (requestHeaderName != null) {
                    request.getHeader(requestHeaderName).apply {
                        http.request.headers.put(requestHeaderName, this)
                    }
                }
            }
        }


        var res = http.doNet()
        response.status = http.status;

        run {
            var headerArray = arrayOf("content-type")
            var responseHeaderNames = http.response.headers.keys.toList()
            headerArray.forEach { headerName ->
                var responseHeaderName = responseHeaderNames.firstOrNull { headerName VbSame it }
                if (responseHeaderName != null) {
                    response.setHeader(responseHeaderName, http.response.headers.get(responseHeaderName))
                }
            }
        }

        response.writer.write(res)
        return http
    }
}

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
        if (event.loginUser != null) {
            this.LoginUser = event.loginUser!!
            return event.loginUser!!;
        }

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
 * 从Jwt中获取用户Id
 */
fun getJwtUserId(value: String): String {
    if (config.jwtSecretKey.isEmpty()) return "";

    var claims = Jwts.parser()
        .setSigningKey(config.jwtSecretKey)
        .parseClaimsJws(value)
        .getBody();


    var userIdKey = claims.keys.firstOrNull { it VbSame "userId" } ?: claims.keys.first { it VbSame "user-id" }

    if (userIdKey.HasValue) {
        return claims.getStringValue(userIdKey!!) ?: "";
    }

    return "";
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
        var value_decript = MyUtil.getStringContentFromBase64(value);
        return HttpUtil.getLoginNamePassword(value_decript)
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
            HttpContext.response.setHeader(config.tokenKey, token)
        }

        var event = RequestGetTokenEvent(this, token);
        SpringUtil.context.publishEvent(event);
        if (event.token.HasValue) {
            token = event.token
        }

        this.setAttribute(cacheKey, token)

        return token;
    }


fun <M : MongoBaseMetaCollection<out Serializable>> MongoSetEntityUpdateClip<M>.withRequestParams(): MongoSetEntityUpdateClip<M> {
    var keys = HttpContext.request.getPostJson().keys;
    keys.forEach { key ->
        this.withColumn { MongoColumnName(key) }
    }
    return this;
}

fun <M : SqlBaseMetaTable<out Serializable>> SqlSetEntityUpdateClip<M>.withRequestParams(): SqlSetEntityUpdateClip<M> {
    var keys = HttpContext.request.getPostJson().keys;
    var columns = this.mainEntity.getColumns();
    keys.forEach { key ->
        var column = columns.firstOrNull { it.name == key }
        if (column != null) {
            this.withColumn { column }
        }
    }
    return this
}
