package nbcp.base.db

import nbcp.base.db.Cn
import nbcp.base.comm.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import java.io.InputStream
import java.io.Serializable

/**
 * Created by yuxh on 2018/11/13
 */

open class IdValue @JvmOverloads constructor(var id: String = "", var value: String = "") : Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}

open class IdName @JvmOverloads constructor(var id: String = "", @Cn("名称") var name: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}

open class KeyValueString @JvmOverloads constructor(@Cn("键") var key: String = "", @Cn("值") var value: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}

open class JsonKeyValuePair<T> @JvmOverloads constructor(@Cn("键") var key: String = "", @Cn("值") var value: T? = null) :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}

/**
 * 表示Mongo数据库里 Id，Url 的附件实体引用。
 * 保存到Mongo数据库的时候，使用 field Json，无Host。
 * 返回到Mvc前端的时候，使用 get method Json，带Host。
 */
abstract class BaseUrlModel() : Serializable {
    @Cn("网络资源地址")
    var url: String = ""
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
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
    NameUrl(name, url) {
}

open class IdNamePath @JvmOverloads constructor(id: String = "", name: String = "", @Cn("路径") var path: String = "") :
    IdName(id, name) {
}


open class IntCodeName @JvmOverloads constructor(@Cn("编码") var code: Int = 0, @Cn("名称") var name: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}


/**
 * 只能用于 code 不变的情况。
 */
open class CodeName @JvmOverloads constructor(@Cn("编码") var code: String = "", @Cn("名称") var name: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}

/**
 * 只能用于 code 不变的情况。
 */
open class CodeValue @JvmOverloads constructor(@Cn("编码") var code: String = "", @Cn("值") var value: String = "") :
    Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}

open class IdCodeName @JvmOverloads constructor(var id: String = "", code: String = "", name: String = "") :
    CodeName(code, name) {
}


open class LoginNamePasswordData(var loginName: String = "", var password: String = "") : Serializable {
    override fun toString(): String {
        return this.ToJson().Slice(1, -1)
    }
}

/**
 * 登录用户数据
 */
open class LoginUserModel @JvmOverloads constructor(
    var token: String = "",
    var system: String = "",
    id: String = "",
    var loginField: String = "",
    var loginName: String = "",
    /**
     * 是否是超级管理员
     */
    var isAdmin: Boolean = false,
    name: String = "",
    /**
     * 所属组织
     */
    var organization: IdName = IdName(),
    var freshToken: String = ""//角色
) : IdName(id, name) {

    var depts: List<String> = listOf()
    var groups: List<String> = listOf()
    var apps: List<String> = listOf()
    var roles: List<String> = listOf()

    var ext: String = ""

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
//interface ITreeData<T> {
//    var id: String;
//    fun children(): MutableList<T>;
//}


data class UploadFileResource(var fileName: String, val stream: InputStream)
