package nbcp.myoql.db.mybatis

import nbcp.base.extend.HasValue
import nbcp.base.extend.IsIn
import nbcp.base.extend.ToEnum
import nbcp.base.utils.MyUtil
import nbcp.base.utils.ReflectUtil
import nbcp.base.utils.ResourceUtil
import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.db
import org.apache.ibatis.cache.CacheKey
import org.apache.ibatis.executor.Executor
import org.apache.ibatis.executor.keygen.SelectKeyGenerator
import org.apache.ibatis.mapping.BoundSql
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlCommandType
import org.apache.ibatis.plugin.*
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import org.mybatis.spring.transaction.SpringManagedTransaction
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.*
import javax.sql.DataSource


@Component
@Intercepts(
        Signature(type = Executor::class, method = "update", args = arrayOf(MappedStatement::class, Any::class)),
        Signature(
                type = Executor::class, method = "query", args = arrayOf(
                MappedStatement::class,
                Object::class,
                RowBounds::class,
                ResultHandler::class,
                CacheKey::class,
                BoundSql::class
        )
        )
)
class MyBatisInterceptor : Interceptor {
    @Throws(Throwable::class)
    override fun intercept(invocation: Invocation): Any {

        val objects = invocation.getArgs()
        val ms = objects[0] as MappedStatement

        //!selectKey 为自增id查询主键(SELECT LAST_INSERT_ID() )方法，使用主库
        if (ms.id.contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)) {
            setWriteMode(invocation.target as Executor);
            return invocation.proceed()
        }


        val paramMap = objects[1]
        val boundSql = ms.sqlSource.getBoundSql(paramMap)
        val sql = boundSql.sql.lowercase(Locale.CHINA)
        //写方法
//        if (ms.sqlCommandType.IsIn(SqlCommandType.UPDATE, SqlCommandType.DELETE, SqlCommandType.INSERT)) {
//            setWriteMode(invocation.target as Executor);
//        }

        var matches = CHANG_OP_REGEX.findAll(sql).toList();
        if (matches.any()) {
            for (index in matches.indices) {
                var match = matches.get(index);

                var group = match.groups.get(1)!!;
                var op = group.value.ToEnum<SqlCommandType>()!!;
                var text = sql.substring(group.range.last)

                addChangeTable(op, text);
            }

            setWriteMode(invocation.target as Executor);
            return invocation.proceed()
        }


        //如果之前对该表执行了 insert,update
        var selectTables = SELECT_TABLE_REGEX.findAll(sql)
                .toList()
                .map { it.groupValues.last().lowercase() }
                .toSet();
        if (selectTables.any() && db.currentRequestChangeDbTable.intersect(selectTables).any()) {
            setWriteMode(invocation.target as Executor);
            return invocation.proceed()
        }


        setReadMode(invocation.target as Executor);
        return invocation.proceed()
    }

    private fun addChangeTable(op: SqlCommandType, text: String) {
        var matched = CHANG_TABLE_REGEX.find(text);
        if (matched == null) {
            return;
        }


        var tableName = matched.groupValues.filter { it.HasValue }.last();

        db.currentRequestChangeDbTable.add(tableName.lowercase())
    }


    private fun setReadMode(executor: Executor) {
        val slave = SpringUtil.getBeanWithNull<DataSource>("slaveDataSource");
        if (slave != null) {
            ReflectUtil.setPrivatePropertyValue(executor.transaction as SpringManagedTransaction, "dataSource", slave)
            return;
        }

        this.setWriteMode(executor);
    }

    //默认
    private fun setWriteMode(executor: Executor) {
        ReflectUtil.setPrivatePropertyValue(
                executor.transaction as SpringManagedTransaction,
                "dataSource",
                db.sql.getScopeDataSource()
        )
    }

    override fun plugin(target: Any): Any {
        return if (target is Executor) {
            Plugin.wrap(target, this)
        } else {
            target
        }
    }

    override fun setProperties(properties: Properties) {
        //
    }

    companion object {
        private val CHANG_OP_REGEX = ".*?\\b(insert|update|delete)\\b".toRegex(RegexOption.IGNORE_CASE)
        private val CHANG_TABLE_REGEX = ".*?\\b(from|into)?\\b\\s+([`\"\\[])?([\\w_]+)".toRegex(RegexOption.IGNORE_CASE)
        private val SELECT_TABLE_REGEX = ".*?\\b(from|join)\\b\\s+([`\"\\[])?([\\w_]+)".toRegex(RegexOption.IGNORE_CASE)
    }
}

