package nbcp.myoql.code.generator.db.mysql

import nbcp.base.comm.config
import nbcp.base.extend.basicSame
import nbcp.base.utils.MyUtil
import nbcp.myoql.code.generator.db.mysql.model.TableColumnMetaData
import nbcp.myoql.code.generator.removeQuoteContent
import nbcp.myoql.db.sql.enums.DbType
import org.springframework.beans.BeanUtils

/**
 * 按 Freemarker 形式定义数据库表模型 。
 */
class TableColumnDetail : TableColumnMetaData() {
    companion object {
        fun loadFrom(column: TableColumnMetaData): TableColumnDetail {
            var ret = TableColumnDetail();
            BeanUtils.copyProperties(column, ret);
            return ret;
        }
    }


    /**
     * 去除（）标志的注释，用于生成 CRUD 前端页面表单的标题
     */
    val labelComment: String
        get() {
            return this.columnComment.removeQuoteContent()
        }

    val fieldName: String
        get() {
            var name2 = this.columnName;
            if (name2[0].isDigit()) {
                name2 = "field_" + name2;
            }

            if (config.myoqlKeepDbName) {
                return MyUtil.splitWordParts(name2).joinToString("_");
            }
            return MyUtil.getSmallCamelCase(name2);
        }

    var dbType = DbType.OTHER


    val kotlinType: String
        get() {
            return this.dbType.toKotlinType()
        }

    val javaType: String
        get() {
            return this.dbType.javaType.typeName
        }

    val kotlinDefaultValue: String
        get() {
            return this.dbType.toKotlinDefaultValue()
        }

    val javaDefaultValue: String
        get() {
            if (this.dbType == DbType.BYTE) {
                return "new byte[0]"
            }
            return this.dbType.toKotlinDefaultValue()
        }


    val autoId: Boolean
        get() {
            return this.columnComment.contains(Regex("\bauto_id\b", RegexOption.IGNORE_CASE))
        }

    val autoNumber: Boolean
        get() {
            return this.columnComment.contains(Regex("\bauto_number\b", RegexOption.IGNORE_CASE))
        }

    /*下面四个属性表示该表的单键主键 或 唯一键*/
    val autoInc: Boolean
        get() {
            return this.extra basicSame "auto_increment"
        }

    val isEnum: Boolean
        get() {
            return this.dataType basicSame "enum";
        }

    val isEnumSet: Boolean
        get() {
            return this.dataType basicSame "set"
        }
}