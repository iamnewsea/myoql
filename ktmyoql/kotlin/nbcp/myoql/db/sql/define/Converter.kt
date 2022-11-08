package nbcp.myoql.db.sql.define

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.sql.base.IFieldValueConverter
import java.lang.RuntimeException
import java.lang.reflect.Field

/**
 * Created by yuxh on 2019/1/31
 */

/**
 * 自定义转换： 把所有值转为大写。
 */
class TrimUppercaseConverter : IFieldValueConverter {
    override fun convert(field: Field, value: Any?): Any? {
        if (value == null) return null;
        if (value is String) {
            return value.trim().uppercase()
        }
        return value;
    }
}


class TrimLowercaseConverter : IFieldValueConverter {
    override fun convert(field: Field, value: Any?): Any? {
        if (value == null) return null;
        if (value is String) {
            return value.trim().lowercase()
        }
        return value;
    }
}

class AutoIdConverter : IFieldValueConverter {
    override fun convert(field: Field, value: Any?): Any? {
        if (field.type.IsNumberType) {
            if (value == null || value.AsLong(0) == 0L) {
                //检查类型
                val clazz = field.type;
                if (clazz == java.lang.Integer::class.java || clazz == Int::class.java) {
                    throw RuntimeException("AutoId不能是32位整型")
                }
                return CodeUtil.getNumberValue();
            }
        } else if (field.type.IsStringType) {
            if (value == null || value.toString().isNullOrEmpty()) {
                return CodeUtil.getCode();
            }
        } else {
            throw RuntimeException("AutoId必须是字符串或长整型")
        }

        return value;
    }
}