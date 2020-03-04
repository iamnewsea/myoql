package nbcp.handler

import nbcp.base.utils.CodeUtil
import nbcp.comm.ApiResult
import nbcp.comm.OpenAction
import nbcp.comm.Require
import nbcp.db.db
import nbcp.db.mongo.match
import nbcp.db.mongo.query
import nbcp.db.mongo.table.MongoBaseGroup
import nbcp.db.mongo.updateById
import nbcp.db.redis.RedisBaseGroup
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@OpenAction
@RestController
@ConditionalOnProperty(name = ["server.oauth2"], havingValue = "true", matchIfMissing = true)
class Oauth2Service {
    companion object {
    }

    @RequestMapping("/oauth2/authorizeCode", method = arrayOf(RequestMethod.POST))
    fun authorizeCode(@Require appKey: String, @Require appSecret: String): ApiResult<String> {
        //先查 redis
        var map = db.rer_base.oauth2.authorize.getMap(appKey);
        if (map != null) {
            if (map.getOrDefault("secret", "") != appSecret) {
                return ApiResult("密钥不正确")
            }
            return ApiResult.of(map.get("authorizeCode"))
        }

        var app = db.mor_base.sysApplication.query()
                .where { it.key match appKey }
                .toEntity()

        if (app == null) {
            return ApiResult("找不到应用")
        }

        if (app.secret != appSecret) {
            return ApiResult("密钥不正确")
        }

        app.authorizeCode = CodeUtil.getCode()
        app.token = CodeUtil.getCode()
        app.freshToken = CodeUtil.getCode()

        db.mor_base.sysApplication.updateById(app.id)
                .set { it.authorizeCode to app.authorizeCode }
                .set { it.token to app.token }
                .set { it.freshToken to app.freshToken }
                .set { it.codeCreateAt to LocalDateTime.now() }
                .exec();

        db.rer_base.oauth2.authorize.setMap(appKey, mapOf("secret" to app.secret, "authorizeCode" to app.authorizeCode))

        return ApiResult.of(app.authorizeCode)
    }


    data class TokenResultData(
            var token: String = "",
            var freshToken: String = "",
            var expireTime: Int = 0 //秒
    )

    @RequestMapping("/oauth2/token", method = arrayOf(RequestMethod.POST))
    fun token(@Require appKey: String, @Require authorizeCode: String): ApiResult<TokenResultData> {


        var app = db.mor_base.sysApplication.query()
                .where { it.key match appKey }
                .toEntity()

        if (app == null) {
            return ApiResult("找不到应用")
        }

        if (app.authorizeCode != authorizeCode) {
            return ApiResult("授权码不正确")
        }

        var ret = TokenResultData();
        ret.token = app.token;
        ret.freshToken = app.freshToken;
        ret.expireTime = 7200;

        db.rer_base.oauth2.authorize.deleteWithKey(appKey)
        db.mor_base.sysApplication.updateById(app.id)
                .unset { it.authorizeCode }
                .exec();

        return ApiResult.of(ret)
    }

    @RequestMapping("/oauth2/fresh", method = arrayOf(RequestMethod.POST))
    fun fresh(@Require appKey: String, @Require freshToken: String): ApiResult<TokenResultData> {
        var app = db.mor_base.sysApplication.query()
                .where { it.key match appKey }
                .toEntity()

        if (app == null) {
            return ApiResult("找不到应用")
        }

        if (app.freshToken != freshToken) {
            return ApiResult("刷新码不正确")
        }

        app.freshToken = CodeUtil.getCode();
        app.token = CodeUtil.getCode();

        db.mor_base.sysApplication.updateById(app.id)
                .set { it.token to app.token }
                .set { it.freshToken to app.freshToken }
                .set { it.codeCreateAt to LocalDateTime.now() }
                .exec();

        var ret = TokenResultData();
        ret.token = app.token;
        ret.freshToken = app.freshToken;
        ret.expireTime = 7200;
        return ApiResult.of(ret)
    }
}