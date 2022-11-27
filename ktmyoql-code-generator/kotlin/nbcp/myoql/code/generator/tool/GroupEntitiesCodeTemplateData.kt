package nbcp.myoql.code.generator.tool

import nbcp.myoql.code.generator.db.mysql.*


class GroupEntitiesCodeTemplateData(
    var group: String,
    var entities: List<EntityDbItemData>,
) : BaseFreemarkerModel() {



}