package nbcp.db


/**
 * 保存收集 DbEntityFieldRef 的Bean。
 * 冗余字段的引用。如 user.corp.name 引用的是  corp.name
 * 更新规则：
 * 如更新了引用实体，corp.id = 1 ,corp.name = 'a'
 * 则：
 * mor.定义的实体
 *  .where { it.corp.id match 1 }
 *  .set { it.corp.name to 'a' }
 *  .exec()
 *
 */
data class DbEntityFieldRefData(
        //实体，entity 如 SysUser
        var entityClass: Class<*>,
        //实体的引用Id， 如 "corp._id"
        var idField: String,
        //实体的冗余字段, 如： "corp.name"
        var nameField: String,
        // 引用的实体
        var masterEntityClass: Class<*>,
        //引用实体的Id字段， corp 表的 , "id"
        var masterIdField: String,
        //冗余字段对应的引用实体字段， corp表的 , "name"
        var masterNameField: String
) {
    constructor(entityClass: Class<*>, annRef: DbEntityFieldRef) : this(
            entityClass, //moer class
            annRef.idField,
            annRef.nameField,
            annRef.masterEntityClass.java,
            annRef.masterIdField,
            annRef.masterNameField) {

    }
}

/**
 * 更新或删除事件执行的结果
 */
data class DbEntityEventResult(
        // 执行结果
        var result: Boolean = true,
        // 执行前，操作的额外数据。
        var extData: Any? = null
)