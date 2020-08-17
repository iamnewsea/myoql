package nbcp.db


/**
 * 保存收集 DbEntityFieldRef 的 Bean。
 * 冗余字段的引用。如 user.corp.name 引用的是  corp.name
 * 更新规则：
 * 如更新了引用实体，corp.id = 1 ,corp.name = 'a'
 * 则：
 * mor.定义的实体
 *  .where { it.corp.id match 1 }
 *  .set { it.corp.name to 'a' }
 *  .exec()
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
            "",
            "",
            annRef.masterEntityClass.java,
            "",
            "") {

        var idFields = annRef.idFieldMap.split(":").toMutableList();
        if (idFields.size == 1) {
            idFields.add(idFields.first().split(".").last())
        }

        this.idField = idFields.first();
        this.masterIdField = idFields.last();

        var nameFields = annRef.nameFieldMap.split(":").toMutableList();
        if (nameFields.size == 1) {
            nameFields.add(nameFields.first().split(".").last())
        }

        this.nameField = nameFields.first();
        this.masterNameField = nameFields.last();
    }
}