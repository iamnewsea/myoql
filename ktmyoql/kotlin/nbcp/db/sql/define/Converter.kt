package nbcp.db.sql

import nbcp.comm.AsLong
import nbcp.db.sql.IConverter
import nbcp.utils.CodeUtil

/**
 * Created by yuxh on 2019/1/31
 */

/**
 * 自定义转换： 把所有值转为大写。
 */
class TrimUppercaseConverter : IConverter {
    override fun convert(value: Any?): Any? {
        if (value == null) return null;
        if (value is String) {
            return value.trim().toUpperCase()
        }
        return value;
    }
}


class TrimLowercaseConverter : IConverter {
    override fun convert(value: Any?): Any? {
        if (value == null) return null;
        if (value is String) {
            return value.trim().toLowerCase()
        }
        return value;
    }
}

class AutoIdConverter : IConverter {
    override fun convert(value: Any?): Any? {
        if (value == null || value.toString().isNullOrEmpty()) {
            return CodeUtil.getCode();
        }
        return value;
    }
}

class AutoNumberConverter : IConverter {
    override fun convert(value: Any?): Any? {
        if (value.AsLong(0) == 0L) {
            return CodeUtil.getNumberValue()
        }
        return value;
    }
}
