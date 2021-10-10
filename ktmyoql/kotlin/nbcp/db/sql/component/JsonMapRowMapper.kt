package nbcp.db.sql.component

import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import nbcp.comm.*
import java.sql.ResultSet

/**
 * Created by yuxh on 2018/7/2
 */

/**
 * 暂时无用
 */
@Component
class JsonMapRowMapper : RowMapper<JsonMap> {
    override fun mapRow(rs: ResultSet, rowNum: Int): JsonMap {
        val ret = JsonMap()
        val meta = rs.metaData;
        for (i in 1..meta.columnCount) {
            ret.set(meta.getColumnLabel(i), rs.getObject(i))
        }
        return ret
    }
}