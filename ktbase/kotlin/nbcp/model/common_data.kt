package nbcp.db

import nbcp.comm.*
import nbcp.utils.*
import java.io.Serializable

/**
 * Created by yuxh on 2018/11/13
 */

open class IdValue @JvmOverloads constructor(var id: String = "", var value: String = "") : Serializable {
    override fun toString(): String {
        if (this::class.java == IdValue::class.java) {
            return "${id}:${value}"
        }
        return super.toString()
    }
}

open class IdName @JvmOverloads constructor(var id: String = "", @Cn("名称") var name: String = "") :
    Serializable {
    override fun toString(): String {
        if (this::class.java == IdName::class.java) {
            return "${id}:${name}"
        }
        return super.toString()
    }
}

open class KeyValueString @JvmOverloads constructor(@Cn("键") var key: String = "", @Cn("值") var value: String = "") :
    Serializable {
    override fun toString(): String {
        if (this::class.java == KeyValueString::class.java) {
            return "${key}:${value}"
        }
        return super.toString()
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
}


open class IdUrl() : BaseUrlModel() {
    var id: String = ""

    constructor(id: String, url: String) : this() {
        this.id = id;
        this.url = url;
    }

    override fun toString(): String {
        if (this::class.java == IdUrl::class.java) {
            return "${id}:${url}"
        }
        return super.toString()
    }
}

open class NameUrl() : BaseUrlModel() {
    @Cn("名称")
    var name: String = ""

    constructor(name: String, url: String) : this() {
        this.name = name;
        this.url = url;
    }

    override fun toString(): String {
        if (this::class.java == NameUrl::class.java) {
            return "${name}:${url}"
        }
        return super.toString()
    }
}

open class IdNameUrl @JvmOverloads constructor(var id: String = "", name: String = "", url: String = "") :
    NameUrl(name, url) {
    override fun toString(): String {
        if (this::class.java == IdNameUrl::class.java) {
            return "${id}:${name}:${url}"
        }
        return super.toString()
    }
}

open class IdNamePath @JvmOverloads constructor(id: String = "", name: String = "", @Cn("路径") var path: String = "") :
    IdName(id, name) {
    override fun toString(): String {
        if (this::class.java == IdNamePath::class.java) {
            return "${id}:${name}:${path}"
        }
        return super.toString()
    }
}

/**
 * 只能用于 code 不变的情况。
 */
open class CodeName @JvmOverloads constructor(@Cn("编码") var code: String = "", @Cn("名称") var name: String = "") :
    Serializable {
    override fun toString(): String {
        if (this::class.java == CodeName::class.java) {
            return "${code}:${name}"
        }
        return super.toString()
    }
}

/**
 * 只能用于 code 不变的情况。
 */
open class CodeValue @JvmOverloads constructor(@Cn("编码") var code: String = "", @Cn("值") var value: String = "") :
    Serializable {
    override fun toString(): String {
        if (this::class.java == CodeValue::class.java) {
            return "${code}:${value}"
        }
        return super.toString()
    }
}

open class IdCodeName @JvmOverloads constructor(var id: String = "", code: String = "", name: String = "") :
    CodeName(code, name) {
    override fun toString(): String {
        if (this::class.java == IdCodeName::class.java) {
            return "${id}:${code}:${name}"
        }
        return super.toString()
    }
}


open class LoginNamePasswordData(var loginName: String = "", var password: String = "") : Serializable {
    override fun toString(): String {
        if (this::class.java == LoginNamePasswordData::class.java) {
            return "${loginName}:${password}"
        }
        return super.toString()
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
