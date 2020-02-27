package nbcp.db.mongo

import nbcp.base.extend.ForEachExt
import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.db.mongo.component.MongoBaseUpdateClip
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class MongoEntityEvent : BeanPostProcessor {
    companion object {
        //所有的组。
        val groups = mutableSetOf<IDataGroup>()
        //需要删 除后放入垃圾箱的实体
        val dustbinEntitys = mutableSetOf<Class<*>>()  //mongo entity class
        val logHistoryMap = linkedMapOf<Class<*>, Array<String>>()
        // 冗余字段的引用。如 user.corp.name 引用的是  corp.name
        val refsMap = mutableListOf<DbEntityFieldRefData>()
        //注册的 Update Bean
        val updateEvent = mutableListOf<IMongoEntityUpdate>()
        //注册的 Delete Bean
        val deleteEvent = mutableListOf<IMongoEntityDelete>()

        /**
         * 根据名称查找定义的集合。
         */
        fun getCollection(collectionName: String): MongoBaseEntity<IMongoDocument>? {
            var ret: BaseDbEntity? = null
            groups.any { group ->
                ret = group.getEntities().firstOrNull() { it.tableName == collectionName }

                return@any ret != null
            }

            return ret as MongoBaseEntity<IMongoDocument>?
        }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {

        if (bean is IDataGroup) {
            groups.add(bean)

            bean.getEntities().forEach { moer ->
                if (moer is MongoBaseEntity<*>) {
                    var entityClass = moer.entityClass

                    addDustbin(entityClass)
                    addRef(entityClass)
                    addLogHistory(entityClass);
                }
            }
        }
        if (bean is IMongoEntityUpdate) {
            var ann = bean::class.java.getAnnotation(DbEntityUpdate::class.java)
            if (ann != null) {
                updateEvent.add(bean)
            }
        }


        if (bean is IMongoEntityDelete) {
            var ann = bean::class.java.getAnnotation(DbEntityDelete::class.java)
            if (ann != null) {
                deleteEvent.add(bean)
            }
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }

    private fun addLogHistory(entityClass: Class<out IMongoDocument>) {
        var logHistory = entityClass.getAnnotation(DbEntityLogHistory::class.java)
        if (logHistory != null) {
            logHistoryMap.put(entityClass, logHistory.fields.map { it }.toTypedArray());
        }
    }


    private fun addRef(entityClass: Class<out IMongoDocument>) {
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
    }

    private fun addDustbin(entityClass: Class<out IMongoDocument>) {
        var dustbin = entityClass.getAnnotation(MongoEntitySysDustbin::class.java)
        if (dustbin != null) {
            dustbinEntitys.add(entityClass)
        }
    }


    fun onUpdating(update: MongoBaseUpdateClip): Array<Pair<IMongoEntityUpdate, DbEntityEventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IMongoEntityUpdate, DbEntityEventResult>>()
        updateEvent.ForEachExt { it, index ->
            var ret = it.beforeUpdate(update);
            if (ret.result == false) {
                return@ForEachExt false;
            }
            list.add(it to ret)
            return@ForEachExt true
        }
        return list.toTypedArray()
    }

    fun onDeleting(delete: MongoDeleteClip<*>): Array<Pair<IMongoEntityDelete, DbEntityEventResult>> {

        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IMongoEntityDelete, DbEntityEventResult>>()
        deleteEvent.ForEachExt { it, index ->
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