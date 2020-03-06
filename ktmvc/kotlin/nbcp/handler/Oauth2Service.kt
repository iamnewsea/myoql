//package nbcp.handler
//
//import nbcp.base.utils.CodeUtil
//import nbcp.base.utils.Md5Util
//import nbcp.comm.ApiResult
//import nbcp.comm.OpenAction
//import nbcp.comm.Require
//import nbcp.db.db
//import nbcp.db.mongo.entity.SysApplication
//import nbcp.db.mongo.entity.SysLoginUser
//import nbcp.db.mongo.match
//import nbcp.db.mongo.query
//import nbcp.db.mongo.table.MongoBaseGroup
//import nbcp.db.mongo.updateById
//import nbcp.db.redis.RedisBaseGroup
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RequestMethod
//import org.springframework.web.bind.annotation.RestController
//import java.lang.RuntimeException
//import java.time.LocalDateTime
//
///**
// * https://www.zhihu.com/question/27446826?rf=275041157
// * 为什么OAuth2里面在获取access token之前一定要先获取code，然后再用code去获取access token？
//
//oauth2 code 认证的角色
//1，client——通常是我们开发的 app
//2，owner——使用我们 app 的用户
//3，auth server——在 owner 授权后，为 client 提供接口来访问资源
//
//认证过程
//1，client 获取code时，auth server是不能确认client的身份的，因为这时auth server只有一个app id，但没有任何手段来确认 client 使用的是自己的 app id
//2，owner 在 auth server 上认证身份，并同意授权给 client
//3，auth server 向 client 发送一个 code，按 oauth2 的协议约定，该 code 通过浏览器的 302 重定向发送给 client
//4，client 拿 code 换取 token，首先，这个过程是 client 后台对 auth server 后台的，其次，client 需要提供自己的 app secret，这样就为 auth server 提供了一种验证 client 的机制
//
//那么为什么要这个code？
//关键还是第三步：auth server 把 code 发送给 client 这一步不安全，因为 client 可能会用一个 http 协议的接口来接收 code，那么 code 就会被截取
//
//如果在这一步把 token 返回去，有 2 个问题
//1）必须在第一步就提供 app secret，使 auth server 能够验证 client  的身份，这对于 client 的 app secret 来说是不安全的
//2）如果 client 指定的 redirect_url 是 http 协议，token 可以在传输过程中被截取导致泄漏
//
//再延伸一下：
//拿到 token 后，client 在后续的请求里，token 还是直接发送给 auth server （这个时候其实已经是 resource server 了）的，这个时候就不能截取了吗？
//答案是 https——虽然 oauth2.0 协议没有明文要求 auth server 使用 https，但实际上 auth server 提供的接口都是 https 的，作为 app 开发者可以留意一下，看看有哪个提供 oauth 认证服务的厂家不用 https 的。
//
// **/
//@OpenAction
//@RestController
//@ConditionalOnProperty(name = ["server.oauth2"], havingValue = "true", matchIfMissing = true)
//class Oauth2Service {
//    companion object {
//    }
//
//    /**
//     * 第一步，前端加载时，先请求接口，用于显示登录到应用的信息。
//     */
//    @RequestMapping("/oauth2/app-info", method = arrayOf(RequestMethod.POST))
//    fun getAppInfo(@Require appKey: String ): ApiResult<SysApplication> {
//        var app = db.mor_base.sysApplication.query()
//                .where { it.key match appKey }
//                .unSelect { it.secretInfo }
//                .toEntity()
//
//        if (app == null) {
//            return ApiResult("找不到应用")
//        }
//
//        return ApiResult.of(app)
//    }
//
//    /**
//     * 第二步，登录成功，获取Code
//     */
//    @RequestMapping("/oauth2/authorizeCode", method = arrayOf(RequestMethod.POST))
//    fun authorizeCode(@Require appKey: String, @Require callback: String,authorizes:List<String>): ApiResult<SysApplication> {
//        var app = db.mor_base.sysApplication.query()
//                .where { it.key match appKey }
//                .unSelect { it.secretInfo }
//                .toEntity()
//
//        if (app == null) {
//            return ApiResult("找不到应用")
//        }
//
//        if (callback.startsWith(app.hostDomainName) == false) {
//            return ApiResult("回调地址和安全域名不一致")
//        }
//
//        var authorizeCode = CodeUtil.getCode()
//        app.secretInfo.authorizeCode = authorizeCode;
//
//        db.mor_base.sysApplication.updateById(app.id)
//                .set { it.secretInfo.authorizeCode to authorizeCode }
//                .set { it.secretInfo.token to CodeUtil.getCode() }
//                .set { it.secretInfo.freshToken to CodeUtil.getCode() }
//                .set { it.secretInfo.codeCreateAt to LocalDateTime.now() }
//                .exec();
//
//        db.rer_base.oauth2.authorizeCode.set(appKey, authorizeCode);
//
//        return ApiResult.of(app)
//    }
//
//    /**
//     * 第二步，用户点击登录授权。
//     */
//    @RequestMapping("/oauth2/login", method = arrayOf(RequestMethod.POST))
//    fun login(@Require loginName: String, @Require password: String, @Require authorizeCode: String): ApiResult<String> {
//        var users = db.mor_base.sysLoginUser.query()
//                .whereOr({ it.loginName match loginName },
//                        { it.mobile match loginName },
//                        { it.email match loginName })
//                .limit(0, 3)
//                .toList()
//
//        //应该只有一个。
//        if (users.any() == false) {
//            return ApiResult("用户不存在")
//        }
//        //用Redis配合，防暴力破解
//        if (db.rer_base.oauth2.addLoginName(loginName) == false) {
//            return ApiResult("登录太频繁,稍后再试")
//        }
//
//        var db_pwd = Md5Util.getBase64Md5(password);
//
//        users.firstOrNull{ user-> user.password == db_pwd };
//
//    }
//
//
//    data class TokenResultData(
//            var token: String = "",
//            var freshToken: String = "",
//            var expireTime: Int = 0 //秒
//    )
//
//    @RequestMapping("/oauth2/token", method = arrayOf(RequestMethod.POST))
//    fun token(@Require appKey: String, @Require authorizeCode: String): ApiResult<TokenResultData> {
//
//
//        var app = db.mor_base.sysApplication.query()
//                .where { it.key match appKey }
//                .toEntity()
//
//        if (app == null) {
//            return ApiResult("找不到应用")
//        }
//
//        if (app.authorizeCode != authorizeCode) {
//            return ApiResult("授权码不正确")
//        }
//
//        var ret = TokenResultData();
//        ret.token = app.token;
//        ret.freshToken = app.freshToken;
//        ret.expireTime = 7200;
//
//        db.rer_base.oauth2.authorize.deleteWithKey(appKey)
//        db.mor_base.sysApplication.updateById(app.id)
//                .unset { it.authorizeCode }
//                .exec();
//
//        return ApiResult.of(ret)
//    }
//
//    @RequestMapping("/oauth2/fresh", method = arrayOf(RequestMethod.POST))
//    fun fresh(@Require appKey: String, @Require freshToken: String): ApiResult<TokenResultData> {
//        var app = db.mor_base.sysApplication.query()
//                .where { it.key match appKey }
//                .toEntity()
//
//        if (app == null) {
//            return ApiResult("找不到应用")
//        }
//
//        if (app.freshToken != freshToken) {
//            return ApiResult("刷新码不正确")
//        }
//
//        app.freshToken = CodeUtil.getCode();
//        app.token = CodeUtil.getCode();
//
//        db.mor_base.sysApplication.updateById(app.id)
//                .set { it.token to app.token }
//                .set { it.freshToken to app.freshToken }
//                .set { it.codeCreateAt to LocalDateTime.now() }
//                .exec();
//
//        var ret = TokenResultData();
//        ret.token = app.token;
//        ret.freshToken = app.freshToken;
//        ret.expireTime = 7200;
//        return ApiResult.of(ret)
//    }
//}