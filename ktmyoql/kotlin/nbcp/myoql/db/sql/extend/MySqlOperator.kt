@file:JvmName("MyOqlSql")
@file:JvmMultifileClass

package nbcp.myoql.db.sql.extend

import nbcp.base.comm.*
import nbcp.base.extend.AsBoolean
import java.util.*

/**
 * Created by udi on 17-7-10.
 */
fun proc_value(value: Any): Any {
    var type = value::class.java
    if (type.isEnum) {
        return value.toString();
    } else if (type == UUID::class.java) {
        return value.toString()
    } else if (type == Boolean::class.java || type == java.lang.Boolean::class.java) {
        if (value.AsBoolean()) return 1;
        else return 0;
    }

    return value
}
