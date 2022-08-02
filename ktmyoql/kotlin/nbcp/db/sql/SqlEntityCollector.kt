package nbcp.db.sql

import nbcp.db.sql.*;
import nbcp.comm.ForEachExt
import nbcp.db.*
import nbcp.db.cache.RedisCacheColumns
import nbcp.db.cache.RedisCacheDefine
import nbcp.db.mysql.ExistsSqlSourceConfigCondition
import nbcp.db.sql.event.*
import nbcp.utils.SpringUtil
import org.mariadb.jdbc.MariaDbDataSource
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Conditional
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import javax.sql.DataSource
import java.io.Serializable

/**
 * 事件处理中心
 */
@Component
@Conditional(ExistsSqlSourceConfigCondition::class)
@ConditionalOnProperty("spring.datasource.url")
class SqlEntityCollector : BeanPostProcessor {
    companion object {
        //需要删 除后放入垃圾箱的实体
        @JvmStatic
        val dustbinEntities = mutableSetOf<SqlBaseMetaTable<*>>()  //mongo meta class

        // 冗余字段的引用。如 user.corp.name 引用的是  corp.name
        @JvmStatic
        val refsMap = mutableListOf<DbEntityFieldRefData>()

        //注册的 select Bean
        @JvmStatic
        val selectEvents = mutableListOf<ISqlEntitySelect>()

        //注册的 Insert Bean
        @JvmStatic
        val insertEvents = mutableListOf<ISqlEntityInsert>()

        //注册的 Update Bean
        @JvmStatic
        val updateEvents = mutableListOf<ISqlEntityUpdate>()

        //注册的 Delete Bean
        @JvmStatic
        val deleteEvents = mutableListOf<ISqlEntityDelete>()

        @JvmStatic
        val dataSources = mutableListOf<ISqlDataSource>()

        @JvmStatic
        val sysRedisCacheDefines = mutableMapOf<String, Array<out RedisCacheColumns>>()
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {

        if (bean is IDataGroup) {
            var group = bean::class.java.getAnnotation(MetaDataGroup::class.java)
            if (group.dbType.isSqlType()) {
                db.sql.groups.add(bean)

                bean.getEntities().forEach { moer ->
                    if (moer is SqlBaseMetaTable<*>) {
                        var entityClass = moer.tableClass

                        addDustbin(moer)
                        addRedisCache(moer);

                        addRef(entityClass)
                    }
                }
            }
        }

//        if (SpringUtil.runningInTest) {
//            return super.postProcessAfterInitialization(bean, beanName)
//        }


        if (bean is ISqlEntitySelect) {
            selectEvents.add(bean)
        }

        if (bean is ISqlEntityInsert) {
            insertEvents.add(bean)
        }

        if (bean is ISqlEntityUpdate) {
            updateEvents.add(bean)
        }

        if (bean is ISqlEntityDelete) {
            deleteEvents.add(bean)
        }

        if (bean is ISqlDataSource) {
            dataSources.add(bean)
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }


    private val dataSourceMap = mutableMapOf<String, DataSource>();

    /**
     * 在拦截器中获取数据源。
     */
    fun getDataSource(tableName: String, isRead: Boolean): DataSource? {
        var key = "${tableName}-${isRead}"
        var ret = dataSourceMap.get(key);
        if (ret != null) return ret;

        dataSources.ForEachExt { iSqlDataSource, _ ->
            var v = iSqlDataSource.run(tableName, isRead)
            if (v == null) {
                return@ForEachExt false;
            }

            return@ForEachExt true;
        }

        if (ret != null) {
            dataSourceMap.put(key, ret);
        }
        return ret;
    }


    private fun addRef(entityClass: Class<*>) {
        var refs = entityClass.getAnnotationsByType(DbEntityFieldRef::class.java)

        refs.forEach {
            refsMap.add(DbEntityFieldRefData(entityClass, it))
        }

        if (entityClass.superclass != null) {
            addRef(entityClass.superclass);
        }
    }

    private fun addDustbin(moer: SqlBaseMetaTable<*>) {
        var moerClass = moer::class.java;
        var logicalDelete = moerClass.getAnnotation(LogicalDelete::class.java)
        if (logicalDelete != null) {
            return;
        }

        var dustbin = moerClass.getAnnotation(RemoveToSysDustbin::class.java)
        if (dustbin != null) {
            dustbinEntities.add(moer)
        }
    }

    private fun addRedisCache(moer: SqlBaseMetaTable<*>) {
        var list = mutableListOf<RedisCacheColumns>()

        var moerClass = moer::class.java
        moerClass.getAnnotationsByType(DbEntityIndex::class.java)
            .filter { it.cacheable }
            .forEach {
                list.add(RedisCacheColumns(it.value))
            }

        var redisCacheDefine = moerClass.getAnnotation(RedisCacheDefine::class.java);
        if (redisCacheDefine != null) {
            list.add(RedisCacheColumns(redisCacheDefine.value))
        }

        if (list.isEmpty()) return;

        sysRedisCacheDefines.put(moer.tableName, list.toTypedArray())
    }

    fun onSelecting(select: SqlBaseQueryClip): Array<Pair<ISqlEntitySelect, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<ISqlEntitySelect, EventResult>>()
        selectEvents.ForEachExt { it, _ ->
            var ret = it.beforeSelect(select);
            if (ret.result == false) {
                return@ForEachExt false;
            }
            list.add(it to ret)
            return@ForEachExt true
        }
        return list.toTypedArray()
    }


    fun onInserting(insert: SqlInsertClip<*, *>): Array<Pair<ISqlEntityInsert, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<ISqlEntityInsert, EventResult>>()
        insertEvents.ForEachExt { it, _ ->
            var ret = it.beforeInsert(insert);
            if (ret.result == false) {
                return@ForEachExt false;
            }
            list.add(it to ret)
            return@ForEachExt true
        }
        return list.toTypedArray()
    }


    fun onUpdating(update: SqlUpdateClip<*>): Array<Pair<ISqlEntityUpdate, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<ISqlEntityUpdate, EventResult>>()
        updateEvents.ForEachExt { it, _ ->
            var ret = it.beforeUpdate(update);
            if (ret.result == false) {
                return@ForEachExt false;
            }
            list.add(it to ret)
            return@ForEachExt true
        }
        return list.toTypedArray()
    }

    fun onDeleting(delete: SqlDeleteClip<*>): Array<Pair<ISqlEntityDelete, EventResult>> {

        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<ISqlEntityDelete, EventResult>>()
        deleteEvents.ForEachExt { it, _ ->
            var ret = it.beforeDelete(delete);
            if (ret.result == false) {
                return@ForEachExt false;
            }
            list.add(it to ret)
            return@ForEachExt true
        }
        return list.toTypedArray()
    }
}