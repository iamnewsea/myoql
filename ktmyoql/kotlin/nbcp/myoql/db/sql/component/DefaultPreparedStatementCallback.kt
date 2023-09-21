package nbcp.myoql.db.sql.component

import org.springframework.jdbc.core.PreparedStatementCallback
import java.sql.PreparedStatement

class DefaultPreparedStatementCallback(): PreparedStatementCallback<Boolean> {
    override fun doInPreparedStatement(ps: PreparedStatement): Boolean  {
        return ps.execute()
    }

}