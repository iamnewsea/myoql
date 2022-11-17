package nbcp.base.db

/**
 * 登录用户数据
 */
open class LoginUserModel () : IdName("", "") {
    var token: String = ""
    var system: String = ""

    /**
     * 是否是超级管理员
     */
    var isAdmin: Boolean = false
    var loginField: String = ""
    var loginName: String = ""

    /**
     * 所属组织
     */
    var organization: IdName = IdName()
    var freshToken: String = ""//角色


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