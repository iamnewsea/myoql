package nbcp.db

import nbcp.comm.*


/**
 * 保存收集 DbEntityFieldRef 的 Bean。
 * 冗余字段的引用。如 user.corp.name 引用的是  corp.name
 * entityClass,refEntityClass 是实体类，不是元数据类！
 * 更新规则：
 * 如更新了引用实体，corp.id = 1 ,corp.name = 'a'
 * 则：
 * mor.定义的实体
 *  .where { it.corp.id match 1 }
 *  .set { it.corp.name to 'a' }
 *  .exec()
 */
data class DbEntityFieldRefData(
    /**
     * 实体类，主类
     */
    var entityClass: Class<*>,
    //实体的引用Id， 如 "corp._id"
    var idField: String,
    //实体的冗余字段, 如： "corp"
    var field: String,
    /**
     * 实体类，引用类
     */
    var refEntityClass: Class<*>,
    //引用实体的Id字段， corp 表的 , "id"
    var refIdField: String
) {
    constructor(entityClass: Class<*>, annRef: DbEntityFieldRef) : this(
        entityClass, //moer class
        "",
        "",
        annRef.refEntityClass.java,
        ""
    ) {

//        var idFields = annRef.idFieldMap.split(":").toMutableList();
//        if (idFields.size == 1) {
//            idFields.add(idFields.first().split(".").last())
//        }

        this.idField = annRef.idField;
        this.refIdField = annRef.refIdField.AsString(this.idField.split(".").last());

//        var nameFields = annRef.nameFieldMap.split(":").toMutableList();
//        if (nameFields.size == 1) {
//            nameFields.add(nameFields.first().split(".").last())
//        }

        this.field = annRef.field
//        this.refNameField = annRef.refNameField.AsString(this.nameField.split(".").last())
    }

    val refNameFields: List<String>
        get() {
            var f = entityClass.GetFieldPath(this.field)
            if (f == null) {
                return listOf()
            }

            var com_type = f.type
            if (f.type.isArray) {
                com_type = f.type.componentType
            } else if (f.type.IsCollectionType) {
                com_type = f.type.GetFirstTypeArguments().first() as Class<*>
            }

            return com_type.AllFields.map { it.name }.filter { it != this.idField }
        }
}