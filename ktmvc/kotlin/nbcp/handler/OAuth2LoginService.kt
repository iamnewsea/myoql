//package nbcp.handler
//
//import nbcp.base.utils.CodeUtil
//import nbcp.comm.ApiResult
//import nbcp.comm.Require
//import nbcp.db.db
//import nbcp.db.mongo.*
//import nbcp.db.mongo.entity.SysApplication
//import nbcp.db.mongo.entity.SysApplicationAuthorizeTypeEnum
//import nbcp.web.LoginUser
//import org.springframework.web.bind.annotation.RequestMapping
//import org.springframework.web.bind.annotation.RequestMethod
//import org.springframework.web.bind.annotation.RestController
//import java.time.LocalDateTime
//import javax.servlet.http.HttpServletRequest
//
//@RestController
//class OAuth2LoginService {
//    /**
//     * 第三步，设置授权，返回授权码。
//     */
//    @RequestMapping("/oauth2/authorizeCode", method = arrayOf(RequestMethod.POST))
//    fun authorizeCode(
//            @Require appKey: String,
//            authorizes: List<SysApplicationAuthorizeTypeEnum>,
//            request: HttpServletRequest): ApiResult<String> {
//        var app = db.mor_base.sysApplication.query()
//                .where { it.key match appKey }
//                .unSelect { it.secretInfo }
//                .toEntity()
//
//        if (app == null) {
//            return ApiResult("找不到应用")
//        }
//
//        var user = db.mor_base.sysUser.queryById(request.LoginUser.id).toEntity();
//        if (user == null) {
//            return ApiResult("找不到用户")
//        }
//
//        var loginUser = db.mor_base.sysLoginUser.query()
//                .where { it.loginName match user.loginName }
//                .toEntity()
//
//        if (loginUser == null) {
//            return ApiResult("找不到用户信息")
//        }
//
//        var map = app.authorizeRange.map {
//            it to authorizes.contains(it)
//        }.toMap()
//
//        db.mor_base.sysUserAuthorizeApplication.update()
//                .where { it.loginName match user.loginName }
//                .where { it.appKey match app.key }
//                .set { it.authorizeRange to map }
//                .exec()
//
//        return ApiResult.of(loginUser.authorizeCode)
//    }
//
//
//}