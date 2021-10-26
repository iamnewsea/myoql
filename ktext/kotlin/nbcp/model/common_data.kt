package nbcp.db

import nbcp.comm.*
import nbcp.utils.*
import java.io.Serializable

/**
 * Created by yuxh on 2018/11/13
 */

open class IdValue @JvmOverloads constructor(var id: String = "", var value: String = "") : Serializable {}

open class IdName @JvmOverloads constructor(var id: String = "", @Cn("名称") var name: String = "") :
    Serializable {}

open class KeyValueString @JvmOverloads constructor(@Cn("键") var key: String = "", @Cn("值") var value: String = "") :
    Serializable {}

/**
 * 表示Mongo数据库里 Id，Url 的附件实体引用。
 * 保存到Mongo数据库的时候，使用 field Json，无Host。
 * 返回到Mvc前端的时候，使用 get method Json，带Host。
 */
abstract class BaseUrlModel() : Serializable {
    @Cn("网络资源地址")
    var url: String = ""
}


open class IdUrl() : BaseUrlModel() {
    var id: String = ""

    constructor(id: String, url: String) : this() {
        this.id = id;
        this.url = url;
    }
}

open class NameUrl() : BaseUrlModel() {
    @Cn("名称")
    var name: String = ""

    constructor(name: String, url: String) : this() {
        this.name = name;
        this.url = url;
    }
}

open class IdNameUrl @JvmOverloads constructor(var id: String = "", name: String = "", url: String = "") :
    NameUrl(name, url)

open class IdNamePath @JvmOverloads constructor(id: String = "", name: String = "", @Cn("路径") var path: String = "") :
    IdName(id, name) {}

/**
 * 只能用于 code 不变的情况。
 */
open class CodeName @JvmOverloads constructor(@Cn("编码") var code: String = "", @Cn("名称") var name: String = "") :
    Serializable {}

/**
 * 只能用于 code 不变的情况。
 */
open class CodeValue @JvmOverloads constructor(@Cn("编码") var code: String = "", @Cn("值") var value: String = "") :
    Serializable {}

open class IdCodeName @JvmOverloads constructor(var id: String = "", code: String = "", name: String = "") :
    CodeName(code, name) {}


open class LoginNamePasswordData(var loginName: String = "", var password: String = "")

/**
 * 登录用户数据
 */
open class LoginUserModel @JvmOverloads constructor(
    var token: String = "",
    var system: String = "",
    var isAdmin: Boolean = false,
    id: String = "",
    var loginName: String = "",
    name: String = "",
    var organization: IdName = IdName(),
    var roles: List<String> = listOf() //角色
) : IdName(id, name) {


//    fun isAdmin(isAdmin: Boolean): LoginUserModel {
//        this.isAdmin = isAdmin;
//        return this;
//    }

    //登录时间
//    var loginAt: LocalDateTime = LocalDateTime.now()

    fun AsIdName(): IdName {
        return IdName(this.id, this.name)
    }
}


/**
 * 树型数据
 */
interface ITreeData<T> {
    var id: String;
    fun children(): MutableList<T>;
}
