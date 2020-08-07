package nbcp.db.sql

import nbcp.db.sql.IConverter

/**
 * Created by yuxh on 2019/1/31
 */

/**
 * 自定义转换： 把所有值转为大写。
 */
class TrimUppercaseConverter:IConverter{
    override fun convert(value: Any?): Any? {
        if( value == null) return null;
        if( value is String) {
            return value.trim().toUpperCase()
        }
        return value;
    }
}


class TrimLowercaseConverter:IConverter{
    override fun convert(value: Any?): Any? {
        if( value == null) return null;
        if( value is String) {
            return value.trim().toLowerCase()
        }
        return value;
    }
}