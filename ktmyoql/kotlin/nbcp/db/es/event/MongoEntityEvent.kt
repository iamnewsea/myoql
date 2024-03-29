package nbcp.db.es

import nbcp.comm.*
import nbcp.db.*
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class EsEntityEvent : BeanPostProcessor {
    companion object {
        @JvmStatic
        //需要删 除后放入垃圾箱的实体
        val dustbinEntitys = mutableSetOf<Class<*>>()  //es entity class
        @JvmStatic
        val logHistoryMap = linkedMapOf<Class<*>, Array<String>>()
        // 冗余字段的引用。如 user.corp.name 引用的是  corp.name
        @JvmStatic
        val refsMap = mutableListOf<DbEntityFieldRefData>()
        //注册的 Update Bean
        @JvmStatic
        val insertEvent = mutableListOf<IEsEntityInsert>()
        //注册的 Update Bean
        @JvmStatic
        val updateEvent = mutableListOf<IEsEntityUpdate>()
        //注册的 Delete Bean
        @JvmStatic
        val deleteEvent = mutableListOf<IEsEntityDelete>()

        /**
         * 根据名称查找定义的集合。
         */
        @JvmStatic
        fun getCollection(collectionName: String): EsBaseEntity<IEsDocument>? {
            var ret: BaseMetaData? = null
            db.es.groups.any { group ->
                ret = group.getEntities().firstOrNull() { it.tableName == collectionName }

                return@any ret != null
            }

            return ret as EsBaseEntity<IEsDocument>?
        }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {

        if (bean is IDataGroup) {
            db.es.groups.add(bean)

            bean.getEntities().forEach { moer ->
                if (moer is EsBaseEntity<*>) {
                    var entityClass = moer.entityClass

                    addDustbin(entityClass)
                    addRef(entityClass)
                    addLogHistory(entityClass);
                }
            }
        }

        if (bean is IEsEntityInsert) {
            insertEvent.add(bean)
        }

        if (bean is IEsEntityUpdate) {
            updateEvent.add(bean)
        }

        if (bean is IEsEntityDelete) {
            deleteEvent.add(bean)
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }

    private fun addLogHistory(entityClass: Class<out IEsDocument>) {
        var logHistory = entityClass.getAnnotation(DbEntityLogHistory::class.java)
        if (logHistory != null) {
            logHistoryMap.put(entityClass, logHistory.fields.map { it }.toTypedArray());
        }
    }


    private fun addRef(entityClass: Class<out IEsDocument>) {
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

    private fun addDustbin(entityClass: Class<out IEsDocument>) {
        var dustbin = entityClass.getAnnotation(RemoveToSysDustbin::class.java)
        if (dustbin != null) {
            dustbinEntitys.add(entityClass)
        }
    }

    fun onInserting(insert: EsBaseInsertClip): Array<Pair<IEsEntityInsert, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IEsEntityInsert, EventResult>>()
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

    fun onUpdating(update: EsBaseUpdateClip): Array<Pair<IEsEntityUpdate, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IEsEntityUpdate, EventResult>>()
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

    fun onDeleting(delete: EsBaseDeleteClip): Array<Pair<IEsEntityDelete, EventResult>> {

        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IEsEntityDelete, EventResult>>()
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