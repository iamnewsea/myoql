package nbcp.myoql.db.sql.base

import java.lang.reflect.Field

interface IFieldValueConverter {
    /**
     * @param field 实体字段
     */
    fun convert(field: Field, value: Any?): Any?
}