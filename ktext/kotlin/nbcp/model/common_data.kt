package nbcp.db

import nbcp.comm.*
import nbcp.utils.*

/**
 * Created by yuxh on 2018/11/13
 */

open class IdValue(var id: String = "", var value: String = "") : java.io.Serializable {}

open class IdName(var id: String = "", var name: String = "") : java.io.Serializable {}

open class CodeName(var code: String = "", var name: String = "") : java.io.Serializable {}

open class CodeValue(var code: String = "", var value: String = "") : java.io.Serializable {}

open class IdCodeName(var id: String = "", code: String = "", name: String = "") : CodeName(code, name) {}

open class KeyValueString(var key: String = "", var value: String = "") : java.io.Serializable {}


/**
 * 表示Mongo数据库里 Id，Url 的附件实体引用。
 * fullUrl 需要配置 app.upload.host
 */
open class IdUrl(var id: String = "", var url: String = "") : java.io.Serializable {
    //全路径
    val fullUrl: String
        get() {
            var host = SpringUtil.context.environment.getProperty("app.upload.host") ?: ""
            return url.PatchHostUrl(host)
        }

//Logo图大小
//    val logoSize: Int
//        get() {
//            var extInfo = FileExtentionInfo(url);
//            if (extInfo.extType != FileExtentionTypeEnum.Image) {
//                return 0;
//            }
//
//
//            var size = SpringUtil.context.environment.getProperty("server.upload.logoSize") ?: "0"
//            return size.AsInt()
//        }
}

open class IdNameUrl(id: String = "", var name: String = "", url: String = "") : IdUrl(id, url)


/**
 * 登录用户数据
 */
open class LoginUserModel(
        id: String = "",
        var loginName: String = "",
        name: String = "",
        var token: String = "",
        var organization: IdName = IdName(),
        var roles: List<String> = listOf() //角色
) : IdName(id, name) {
    companion object{

    }

    fun AsIdName(): IdName {
        return IdName(this.id, this.name)
    }
}

