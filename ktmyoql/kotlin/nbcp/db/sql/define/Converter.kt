package nbcp.db.sql

import nbcp.comm.AsLong
import nbcp.comm.IsNumberType
import nbcp.comm.IsStringType
import nbcp.db.sql.IConverter
import nbcp.utils.CodeUtil
import java.lang.RuntimeException
import java.lang.reflect.Field

/**
 * Created by yuxh on 2019/1/31
 */

/**
 * 自定义转换： 把所有值转为大写。
 */
class TrimUppercaseConverter : IConverter {
    override fun convert(field: Field, value: Any?): Any? {
        if (value == null) return null;
        if (value is String) {
            return value.trim().uppercase()
        }
        return value;
    }
}


class TrimLowercaseConverter : IConverter {
    override fun convert(field: Field, value: Any?): Any? {
        if (value == null) return null;
        if (value is String) {
            return value.trim().lowercase()
        }
        return value;
    }
}

class AutoIdConverter : IConverter {
    override fun convert(field: Field, value: Any?): Any? {
        if (field.type.IsNumberType) {
            if (value == null || value.AsLong(0) == 0L) {
                //检查类型
                var clazz = field.type;
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