package nbcp.db.mybatis

import nbcp.base.utils.MyUtil
import nbcp.base.utils.SpringUtil
import org.apache.ibatis.executor.Executor
import org.apache.ibatis.executor.keygen.SelectKeyGenerator
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.mapping.SqlCommandType
import org.apache.ibatis.plugin.*
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import org.mybatis.spring.transaction.SpringManagedTransaction
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.*
import javax.sql.DataSource


@Intercepts(
        Signature(type = Executor::class, method = "update", args = arrayOf(MappedStatement::class, Any::class)),
        Signature(type = Executor::class, method = "query", args = arrayOf(MappedStatement::class, Object::class, RowBounds::class, ResultHandler::class))
)
class MyBatisInterceptor : Interceptor {
    @Throws(Throwable::class)
    override fun intercept(invocation: Invocation): Any {
        val synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive()
        if (!synchronizationActive) {

            val objects = invocation.getArgs()
            val ms = objects[0] as MappedStatement

            //读方法
            if (ms.sqlCommandType == SqlCommandType.SELECT) {
                //!selectKey 为自增id查询主键(SELECT LAST_INSERT_ID() )方法，使用主库
                if (ms.id.contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)) {
                    setWriteMode(invocation.target as Executor);
                } else {
                    val boundSql = ms.sqlSource.getBoundSql(objects[1])
                    val sql = boundSql.sql.toLowerCase(Locale.CHINA)
                    if (sql.matches(REGEX.toRegex())) {
                        setWriteMode(invocation.target as Executor);
                    } else {
                        setReadMode(invocation.target as Executor);
                    }
                }
            } else {
                setWriteMode(invocation.target as Executor);
            }
        }
        return invocation.proceed()
    }


    private fun setReadMode(executor: Executor) {
        MyUtil.setPrivatePropertyValue(executor.transaction as SpringManagedTransaction, "dataSource", SpringUtil.getBeanByName<DataSource>("read"))
    }

    //默认
    private fun setWriteMode(executor: Executor) {
        MyUtil.setPrivatePropertyValue(executor.transaction as SpringManagedTransaction, "dataSource", SpringUtil.getBean<DataSource>())
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
        private val REGEX = ".*insert\\s.*|.*delete\\s.*|.*update\\s.*"
    }
}

