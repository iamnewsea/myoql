package nbcp.myoql.code.generator.db.mysql

import nbcp.base.extend.basicSame
import nbcp.base.utils.MyUtil
import nbcp.myoql.code.generator.db.mysql.model.TableColumnMetaData
import nbcp.myoql.code.generator.db.mysql.model.TableIndexMetaData
import nbcp.myoql.code.generator.tool.BaseFreemarkerModel
import org.springframework.beans.BeanUtils

/**
 * 按 Freemarker 形式定义数据库表模型 。
 */
class MySqlTableCodeTemplateData(
    var group: String,
    var tableName: String
) : BaseFreemarkerModel() {

    /**
     * 原始注释，可以 (Enum:StatusEnum)(group:admin)
     */
    var tableComment: String = ""

    /**
     * 去除（）标志的注释，用于生成 CRUD 前端页面的标题
     */
    var titleComment: String = ""
    var columns = listOf<TableColumnDetail>()
    var indexes = listOf<TableIndexMetaData>()


}