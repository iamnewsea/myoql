package nbcp.db.sql.component

import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import nbcp.comm.*
import org.mariadb.jdbc.MariaDbDataSource
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import java.sql.ResultSet

/**
 * Created by yuxh on 2018/7/2
 */
@Component
@ConditionalOnProperty("spring.datasource.url")
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