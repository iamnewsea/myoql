package nbcp.myoql.tool

import nbcp.myoql.db.mysql.tool.EntityDbItemData

class GroupEntitiesCodeTemplateData(
    var group: String,
    var entities: List<EntityDbItemData>,
) : BaseFreemarkerModel() {



}