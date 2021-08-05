package nbcp.db.mongo

import nbcp.comm.ForEachExt
import nbcp.comm.OrmLogScope
import nbcp.comm.usingScope
import nbcp.db.*
import nbcp.db.mongo.event.IMongoEntityDelete
import nbcp.db.mongo.event.IMongoEntityInsert
import nbcp.db.mongo.event.IMongoEntityQuery
import nbcp.db.mongo.event.IMongoEntityUpdate
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.PriorityOrdered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
class MongoEntityCollector : BeanPostProcessor {
    companion object {
        //需要删 除后放入垃圾箱的实体
        @JvmStatic
        val dustbinEntitys = mutableSetOf<Class<*>>()  //mongo entity class

        @JvmStatic
        val logHistoryMap = linkedMapOf<Class<*>, Array<String>>()

        // 冗余字段的引用。如 user.corp.name 引用的是  corp.name
        @JvmStatic
        val refsMap = mutableListOf<DbEntityFieldRefData>()

        //注册的 Update Bean
        @JvmStatic
        val queryEvent = mutableListOf<IMongoEntityQuery>()

        @JvmStatic
        val insertEvent = mutableListOf<IMongoEntityInsert>()

        //注册的 Update Bean
        @JvmStatic
        val updateEvent = mutableListOf<IMongoEntityUpdate>()

        //注册的 Delete Bean
        @JvmStatic
        val deleteEvent = mutableListOf<IMongoEntityDelete>()

        /**
         * 根据名称查找定义的集合。
         */
        @JvmStatic
        fun getCollection(collectionName: String): MongoBaseMetaCollection<IMongoDocument>? {
            var ret: BaseMetaData? = null
            db_mongo.groups.any { group ->
                ret = group.getEntities().firstOrNull() { it.tableName == collectionName }

                return@any ret != null
            }

            return ret as MongoBaseMetaCollection<IMongoDocument>?
        }
    }

    //    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
//        return super.postProcessBeforeInitialization(bean, beanName)
//    }
//
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is IDataGroup) {
            db_mongo.groups.add(bean)

            bean.getEntities().forEach { moer ->
                if (moer is MongoBaseMetaCollection<*>) {
                    var entityClass = moer.entityClass

                    addDustbin(entityClass)
                    addRef(entityClass)
                    addLogHistory(entityClass);
                }
            }
        }

        if (bean is IMongoEntityQuery) {
            queryEvent.add(bean)
        }

        if (bean is IMongoEntityInsert) {
            insertEvent.add(bean)
        }

        if (bean is IMongoEntityUpdate) {
            updateEvent.add(bean)
        }

        if (bean is IMongoEntityDelete) {
            deleteEvent.add(bean)
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }

    private fun addLogHistory(entityClass: Class<out IMongoDocument>) {
        var logHistory = entityClass.getAnnotation(DbEntityLogHistory::class.java)
        if (logHistory != null) {
            logHistoryMap.put(entityClass, logHistory.fields.map { it }.toTypedArray());
        }
    }


    private fun addRef(entityClass: Class<*>) {
        var refs = entityClass.getAnnotation(DbEntityFieldRefs::class.java)
        if (refs != null && refs.values.any()) {
            refs.values.forEach {
                refsMap.add(DbEntityFieldRefData(entityClass, it))
            }
        }

        var ref = entityClass.getAnnotation(DbEntityFieldRef::class.java)
        if (ref != null) {
            refsMap.add(DbEntityFieldRefData(entityClass, ref))
        }

        if (entityClass.superclass != null) {
            addRef(entityClass.superclass);
        }
    }

    private fun addDustbin(entityClass: Class<out IMongoDocument>) {
        var dustbin = entityClass.getAnnotation(RemoveToSysDustbin::class.java)
        if (dustbin != null) {
            dustbinEntitys.add(entityClass)
        }
    }

    fun onQuering(query: MongoBaseQueryClip): Array<Pair<IMongoEntityQuery, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IMongoEntityQuery, EventResult>>()
        usingScope(arrayOf(OrmLogScope.IgnoreAffectRow, OrmLogScope.IgnoreExecuteTime)) {
            queryEvent.ForEachExt { it, index ->
                var ret = it.beforeQuery(query);
                if (ret.result == false) {
                    return@ForEachExt false;
                }
                list.add(it to ret)
                return@ForEachExt true
            }
        }
        return list.toTypedArray()
    }

    fun onInserting(insert: MongoBaseInsertClip): Array<Pair<IMongoEntityInsert, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IMongoEntityInsert, EventResult>>()
        usingScope(arrayOf(OrmLogScope.IgnoreAffectRow, OrmLogScope.IgnoreExecuteTime)) {
            insertEvent.ForEachExt { it, index ->
                var ret = it.beforeInsert(insert);
                if (ret.result == false) {
                    return@ForEachExt false;
                }
                list.add(it to ret)
                return@ForEachExt true
            }
        }
        return list.toTypedArray()
    }

    fun onUpdating(update: MongoBaseUpdateClip): Array<Pair<IMongoEntityUpdate, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IMongoEntityUpdate, EventResult>>()
        usingScope(arrayOf(OrmLogScope.IgnoreAffectRow, OrmLogScope.IgnoreExecuteTime)) {
            updateEvent.ForEachExt { it, index ->
                var ret = it.beforeUpdate(update);
                if (ret.result == false) {
                    return@ForEachExt false;
                }
                list.add(it to ret)
                return@ForEachExt true
            }
        }
        return list.toTypedArray()
    }

    fun onDeleting(delete: MongoDeleteClip<*>): Array<Pair<IMongoEntityDelete, EventResult>> {

        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IMongoEntityDelete, EventResult>>()
        usingScope(arrayOf(OrmLogScope.IgnoreAffectRow, OrmLogScope.IgnoreExecuteTime)) {
            deleteEvent.ForEachExt { it, index ->
                var ret = it.beforeDelete(delete);
                if (ret.result == false) {
                    return@ForEachExt false;
                }
                list.add(it to ret)
                return@ForEachExt true
            }
        }
        return list.toTypedArray()
    }
}