package nbcp.db.es

import nbcp.comm.*
import nbcp.db.*
import nbcp.scope.*
import nbcp.utils.SpringUtil
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
@ConditionalOnClass(RestHighLevelClient::class)
class EsEntityCollector : BeanPostProcessor {
    companion object {
        @JvmStatic
        //需要删 除后放入垃圾箱的实体
        val dustbinEntities = mutableSetOf<Class<*>>()  //es entity class

        @JvmStatic
        val logHistoryMap = linkedMapOf<Class<*>, Array<String>>()

        // 冗余字段的引用。如 user.corp.name 引用的是  corp.name
        @JvmStatic
        val refsMap = mutableListOf<DbEntityFieldRefData>()

        @JvmStatic
        val queryEvents = mutableListOf<IEsEntityQuery>()

        //注册的 Update Bean
        @JvmStatic
        val insertEvents = mutableListOf<IEsEntityInsert>()

        //注册的 Update Bean
        @JvmStatic
        val updateEvents = mutableListOf<IEsEntityUpdate>()

        //注册的 Delete Bean
        @JvmStatic
        val deleteEvents = mutableListOf<IEsEntityDelete>()

        @JvmStatic
        val dataSources = mutableListOf<IEsDataSource>()

        /**
         * 根据名称查找定义的集合。
         */
        @JvmStatic
        fun getCollection(collectionName: String): EsBaseMetaEntity<Serializable>? {
            var ret: BaseMetaData? = null
            db.es.groups.any { group ->
                ret = group.getEntities().firstOrNull() { it.tableName == collectionName }

                return@any ret != null
            }

            return ret as EsBaseMetaEntity<Serializable>?
        }
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {

        if (bean is IDataGroup) {
            var group = bean::class.java.getAnnotation(MetaDataGroup::class.java)
            if (group.dbType == DatabaseEnum.ElasticSearch) {
                db.es.groups.add(bean)

                bean.getEntities().forEach { moer ->
                    if (moer is EsBaseMetaEntity<*>) {
                        var entityClass = moer.entityClass

                        addDustbin(entityClass)
                        addRef(entityClass)
                        addLogHistory(entityClass);
                    }
                }
            }
        }


//        if (SpringUtil.runningInTest) {
//            return super.postProcessAfterInitialization(bean, beanName)
//        }


        if (bean is IEsEntityQuery) {
            queryEvents.add(bean)
        }

        if (bean is IEsEntityInsert) {
            insertEvents.add(bean)
        }

        if (bean is IEsEntityUpdate) {
            updateEvents.add(bean)
        }

        if (bean is IEsEntityDelete) {
            deleteEvents.add(bean)
        }

        if (bean is IEsDataSource) {
            dataSources.add(bean)
        }
        return super.postProcessAfterInitialization(bean, beanName)
    }

    private fun addLogHistory(entityClass: Class<out Serializable>) {
        var logHistory = entityClass.getAnnotation(DbEntityLogHistory::class.java)
        if (logHistory != null) {
            logHistoryMap.put(entityClass, logHistory.value.map { it }.toTypedArray());
        }
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

    private fun addDustbin(entityClass: Class<out Serializable>) {
        var dustbin = entityClass.getAnnotation(RemoveToSysDustbin::class.java)
        if (dustbin != null) {
            dustbinEntities.add(entityClass)
        }
    }

    fun onQuering(query: EsBaseQueryClip): Array<Pair<IEsEntityQuery, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IEsEntityQuery, EventResult>>()
        usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
            queryEvents.ForEachExt { it, _ ->
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

    fun onInserting(insert: EsBaseInsertClip): Array<Pair<IEsEntityInsert, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IEsEntityInsert, EventResult>>()
        usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
            insertEvents.ForEachExt { it, _ ->
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
        usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
            updateEvents.ForEachExt { it, _ ->
                var ret = it.beforeUpdate(update);
                if (!ret.result) {
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
        usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
            deleteEvents.ForEachExt { it, _ ->
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

    /**
     * 在拦截器中获取数据源。
     */
    fun getDataSource(collectionName: String, isRead: Boolean): RestHighLevelClient? {
        var ret: RestHighLevelClient? = null;

        dataSources.firstOrNull { esDataSource ->
            ret = esDataSource.run(collectionName, isRead)
            if (ret == null) {
                return@firstOrNull false;
            }

            return@firstOrNull true;
        }

        return ret;
    }
}