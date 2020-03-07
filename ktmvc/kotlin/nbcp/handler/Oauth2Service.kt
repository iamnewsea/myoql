//package nbcp.handler
//
//import nbcp.base.extend.ConvertJson
//import nbcp.base.extend.ToLong
//import nbcp.base.utils.CodeUtil
//import nbcp.base.utils.Md5Util
//import nbcp.comm.ApiResult
//import nbcp.comm.OpenAction
//import nbcp.comm.Require
//import nbcp.db.db
//import nbcp.db.mongo.*
//import nbcp.db.mongo.entity.SysApplication
//import nbcp.db.mongo.entity.SysUser
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RequestMethod
//import org.springframework.web.bind.annotation.RestController
//import java.io.Serializable
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
//    fun getAppInfo(@Require key: String): ApiResult<SysApplication> {
//        var app = db.mor_base.sysApplication.query()
//                .where { it.key match key }
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
//     * 第二步，用户点击登录授权。
//     */
//    @RequestMapping("/oauth2/login", method = arrayOf(RequestMethod.POST))
//    fun login(
//            @Require key: String,
//            @Require loginName: String,
//            @Require password: String): ApiResult<String> {
//
//        var app = db.mor_base.sysApplication.query()
//                .where { it.key match key }
//                .unSelect { it.secretInfo }
//                .toEntity()
//
//        if (app == null) {
//            return ApiResult("找不到应用")
//        }
//
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
//        var loginUser = users.firstOrNull { user -> user.password == db_pwd };
//
//        if (loginUser == null) {
//            return ApiResult("登录失败")
//        }
//
//        var authorizeCode = CodeUtil.getCode()
//        var token = CodeUtil.getCode()
//        loginUser.authorizeCode = authorizeCode;
//
//        db.mor_base.sysLoginUser.updateById(loginUser.id)
//                .set { it.authorizeCode to authorizeCode }
//                .set { it.token to token }
//                .set { it.freshToken to CodeUtil.getCode() }
//                .set { it.codeCreateAt to LocalDateTime.now() }
//                .exec();
//
//        db.mor_base.sysUser.update()
//                .where { it.loginName match loginUser.loginName }
//                .set { it.token to token }
//                .exec();
//
//        db.rer_base.oauth2.authorizeCode.set(key + "-" + loginUser.loginName, authorizeCode);
//
//        //返回 token 是为了自动登录。此时还没有设置授权范围。
//        return ApiResult.of(token)
//    }
//
//
//    data class TokenResultData(
//            var userId: String = "",
//            var token: String = "",
//            var freshToken: String = "",
//            var expireTime: Int = 0 //秒
//    )
//
//    /**
//     * 第四步，App-Server拿 code 换 token
//     * @param sign: 规则：md5(key=${应用Key}&time=${1970年毫秒数}&secret=${密钥}) ， 时间误差3分钟
//     */
//    @RequestMapping("/oauth2/token", method = arrayOf(RequestMethod.POST))
//    fun token(
//            @Require key: String,
//            @Require time: Long,  //1970年到现在的毫秒数
//            @Require sign: String, //为了安全，不传递secret。
//            @Require authorizeCode: String): ApiResult<TokenResultData> {
//
//        var now = LocalDateTime.now().ToLong();
//        if (Math.abs(now - time) > 180000) {
//            return ApiResult("时间差异太大")
//        }
//
//        var app = db.mor_base.sysApplication.query()
//                .where { it.key match key }
//                .toEntity()
//
//        if (app == null) {
//            return ApiResult("找不到应用")
//        }
//
//        var loginUser = db.mor_base.sysLoginUser.query()
//                .where { it.authorizeCode match authorizeCode }
//                .toEntity()
//
//        if (loginUser == null) {
//            return ApiResult("找不到用户信息")
//        }
//        if (loginUser.authorizeCode != authorizeCode) {
//            return ApiResult("授权码不正确")
//        }
//
//        var user = db.mor_base.sysUser.query()
//                .where { it.loginName match loginUser.loginName }
//                .toEntity()
//
//        if (user == null) {
//            return ApiResult("找不到用户")
//        }
//
//
//        var ret = TokenResultData();
//        ret.userId = user.id;
//        ret.token = loginUser.token;
//        ret.freshToken = loginUser.freshToken;
//        ret.expireTime = 7200;
//
//        db.rer_base.oauth2.authorizeCode.deleteWithKey(key + "-" + loginUser.loginName)
////        db.rer_base.oauth2.token.setMap(key + "-" + loginUser.token, ret.ConvertJson(linkedMapOf<String, Serializable>()::class.java))
//
//        db.mor_base.sysLoginUser.updateById(loginUser.id)
//                .unset { it.authorizeCode }
//                .exec();
//
//        return ApiResult.of(ret)
//    }
//
//    /**
//     * 第五步，刷新token
//     */
//    @RequestMapping("/oauth2/fresh", method = arrayOf(RequestMethod.POST))
//    fun fresh(@Require key: String, @Require freshToken: String): ApiResult<TokenResultData> {
//        var app = db.mor_base.sysApplication.query()
//                .where { it.key match key }
//                .toEntity()
//
//        if (app == null) {
//            return ApiResult("找不到应用")
//        }
//
//        var loginUser = db.mor_base.sysLoginUser.query()
//                .where { it.freshToken match freshToken }
//                .toEntity();
//
//        if (loginUser == null) {
//            return ApiResult("找不到用户信息")
//        }
//
//        if (loginUser.freshToken != freshToken) {
//            return ApiResult("刷新码不正确")
//        }
//
//        loginUser.freshToken = CodeUtil.getCode();
//        loginUser.token = CodeUtil.getCode();
//
//        db.mor_base.sysLoginUser.updateById(loginUser.id)
//                .set { it.token to loginUser.token }
//                .set { it.freshToken to loginUser.freshToken }
//                .set { it.codeCreateAt to LocalDateTime.now() }
//                .exec();
//
//        db.mor_base.sysUser.update()
//                .where { it.loginName match loginUser.loginName }
//                .set { it.token to loginUser.token }
//                .exec()
//
//
//        var ret = TokenResultData();
//        ret.userId = loginUser.userId;
//        ret.token = loginUser.token;
//        ret.freshToken = loginUser.freshToken;
//        ret.expireTime = 7200;
//
////        db.rer_base.oauth2.token.setMap(key + "-" + loginUser.token, ret.ConvertJson(linkedMapOf<String, Serializable>()::class.java))
//        return ApiResult.of(ret)
//    }
//
//
//    /**
//     * 第六步，获取用户信息
//     */
//    fun getUserInfo(@Require key: String, @Require userId: String, @Require token: String): ApiResult<SysUser> {
//        var loginUser = db.mor_base.sysLoginUser.query()
//                .where { it.userId match userId }
//                .where { it.token match token }
//                .toEntity();
//
//        if (loginUser == null) {
//            return ApiResult("找不到用户信息")
//        }
//
//        var user = db.mor_base.sysUser.queryById(loginUser.userId).toEntity()
//
//        if (user == null) {
//            return ApiResult("找不到用户信息")
//        }
//
//        return ApiResult.of(user);
//    }
//}