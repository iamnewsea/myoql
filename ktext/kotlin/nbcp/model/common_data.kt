package nbcp.db

import nbcp.comm.*
import nbcp.utils.*

/**
 * Created by yuxh on 2018/11/13
 */

open class IdValue(var id: String = "", var value: String = "") : java.io.Serializable {}

open class IdName(var id: String = "", @Cn("名称") var name: String = "") : java.io.Serializable {}

open class CodeName(@Cn("编码") var code: String = "", @Cn("名称") var name: String = "") : java.io.Serializable {}

open class CodeValue(@Cn("编码") var code: String = "", @Cn("值") var value: String = "") : java.io.Serializable {}

open class IdCodeName(var id: String = "", code: String = "", name: String = "") : CodeName(code, name) {}

open class KeyValueString(@Cn("键") var key: String = "", @Cn("值") var value: String = "") : java.io.Serializable {}

/**
 * 表示Mongo数据库里 Id，Url 的附件实体引用。
 * 保存到Mongo数据库的时候，使用 field Json，无Host。
 * 返回到Mvc前端的时候，使用 get method Json，带Host。
 */
open class IdUrl() : java.io.Serializable {
    var id: String = ""

    @Cn("网络资源地址")
    var url: String = ""
        get() {
            var style = scopes.GetLatest(JsonStyleEnumScope.FieldStyle, JsonStyleEnumScope.GetSetStyle)
                    ?: JsonStyleEnumScope.FieldStyle;


            if (style == JsonStyleEnumScope.GetSetStyle) {
                return field.PatchHostUrl(config.uploadHost)
            }

            return field;
        }
        set(value) {
            field = value;
        }

    constructor(id: String, url: String) : this() {
        this.id = id;
        this.url = url;
    }
}

open class IdNameUrl(id: String = "", @Cn("名称") var name: String = "", url: String = "") : IdUrl(id, url)

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
    companion object {
        fun ofToken(token: String): LoginUserModel {
            return LoginUserModel("", "", "", token);
        }
    }

    fun AsIdName(): IdName {
        return IdName(this.id, this.name)
    }
}

