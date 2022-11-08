package nbcp.myoql.db.sql.component

import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import nbcp.base.comm.*
import nbcp.myoql.db.mysql.ExistsSqlSourceConfigCondition
import org.mariadb.jdbc.MariaDbDataSource
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Conditional
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.ResultSet

/**
 * Created by yuxh on 2018/7/2
 */
@Component
@Conditional(ExistsSqlSourceConfigCondition::class)
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