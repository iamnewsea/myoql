package nbcp.myoql.code.generator.tool

import nbcp.myoql.code.generator.db.mysql.*
import nbcp.myoql.code.generator.tool.freemarker.FreemarkerColumnStyle


class GroupEntitiesCodeTemplateData(
    var group: String,
    var entities: List<EntityDbItemData>,
) : BaseFreemarkerModel() {


    val style = FreemarkerColumnStyle()

}