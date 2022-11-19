package nbcp.myoql.db.mongo

import nbcp.base.db.annotation.*
import nbcp.base.extend.ForEachExt
import nbcp.base.extend.HasValue
import nbcp.base.extend.usingScope
import nbcp.myoql.db.cache.RedisCacheColumns
import nbcp.myoql.db.cache.RedisCacheDefine
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.dbMongo
import nbcp.myoql.db.enums.DatabaseEnum
import nbcp.myoql.db.enums.MyOqlDbScopeEnum
import nbcp.myoql.db.mongo.component.MongoBaseMetaCollection
import nbcp.myoql.db.mongo.component.MongoBaseUpdateClip
import nbcp.myoql.db.mongo.event.*
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component


@Component
@ConditionalOnClass(MongoTemplate::class)
class MongoEntityCollector : BeanPostProcessor {
    companion object {
        //需要删 除后放入垃圾箱的实体
        @JvmStatic
        val dustbinEntities = mutableSetOf<MongoBaseMetaCollection<*>>()  //mongo entity class

        @JvmStatic
        val logHistoryMap = linkedMapOf<MongoBaseMetaCollection<*>, Array<String>>()

        // 冗余字段的引用。如 user.corp.name 引用的是  corp.name
        @JvmStatic
        val refsMap = mutableListOf<DbEntityFieldRefData>()

        @JvmStatic
        val queryEvents = mutableListOf<IMongoEntityQuery>()

        @JvmStatic
        val aggregateEvents = mutableListOf<IMongoEntityAggregate>()

        @JvmStatic
        val insertEvents = mutableListOf<IMongoEntityInsert>()

        //注册的 Update Bean
        @JvmStatic
        val updateEvents = mutableListOf<IMongoEntityUpdate>()

        //注册的 Delete Bean
        @JvmStatic
        val deleteEvents = mutableListOf<IMongoEntityDelete>()

//        @JvmStatic
//        val dataSources = mutableListOf<IMongoDataSource>()

        @JvmStatic
        val collectionVarNames = mutableListOf<IMongoCollectionVarName>()

        @JvmStatic
        val sysRedisCacheDefines = mutableMapOf<String, Array<out RedisCacheColumns>>()
    }

    /**
     * 根据名称查找定义的集合。
     */
    fun getCollection(collectionName: String): MongoBaseMetaCollection<Any>? {
        var ret: BaseMetaData<out Any>? = null
        dbMongo.groups.any { group ->
            ret = group.getEntities().firstOrNull() { it.tableName == collectionName }

            return@any ret != null
        }

        return ret as MongoBaseMetaCollection<Any>?
    }

    //    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
//        return super.postProcessBeforeInitialization(bean, beanName)
//    }
//
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is IDataGroup) {
            var group = bean::class.java.getAnnotation(MetaDataGroup::class.java)
            if (group.dbType == DatabaseEnum.MONGO) {
                dbMongo.groups.add(bean)

                bean.getEntities()
                    .forEach { moer ->
                        if (moer is MongoBaseMetaCollection<*>) {
                            /**
                             * 这里使用了实体的类．
                             */
                            //TODO 使用元数据类，会更好一些．但需要把实体注解，全部转移到元数据类上．

                            addDustbin(moer)
                            addLogHistory(moer);
                            addRedisCache(moer);

                            addRef(moer.entityClass)
                        }
                    }
            }
        }

//        if (SpringUtil.runningInTest) {
//            return super.postProcessAfterInitialization(bean, beanName)
//        }

        if (bean is IMongoEntityQuery) {
            queryEvents.add(bean)
        }

        if (bean is IMongoEntityInsert) {
            insertEvents.add(bean)
        }

        if (bean is IMongoEntityUpdate) {
            updateEvents.add(bean)
        }

        if (bean is IMongoEntityDelete) {
            deleteEvents.add(bean)
        }

//        if (bean is IMongoDataSource) {
//            dataSources.add(bean);
//        }
        if (bean is IMongoCollectionVarName) {
            collectionVarNames.add(bean);
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }

    private fun addRedisCache(moer: MongoBaseMetaCollection<*>) {
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

    private fun addLogHistory(moer: MongoBaseMetaCollection<*>) {
        var moerClass = moer::class.java
        var logHistory = moerClass.getAnnotation(DbEntityLogHistory::class.java)
        if (logHistory != null) {
            logHistoryMap.put(moer, logHistory.value.map { it }.toTypedArray());
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

    private fun addLogicalDelete(entityClass: Class<out Any>) {
//        var logicalDelete = entityClass.getAnnotation(LogicalDelete::class.java)
//        if (logicalDelete != null) {
//            logicalDeleteEntities.add(entityClass);
//        }
    }

    private fun addDustbin(moer: MongoBaseMetaCollection<*>) {
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

    fun onQuering(query: MongoBaseQueryClip): List<QueryEventResult> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<QueryEventResult>()
        usingScope(arrayOf(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW, MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME)) {
            queryEvents.ForEachExt { it, _ ->
                var ret = it.beforeQuery(query);
                if (!ret.result) {
                    return@ForEachExt false;
                }
                list.add(QueryEventResult(it, ret))
                return@ForEachExt true
            }
        }
        return list
    }


    fun onAggregate(query: MongoAggregateClip<*, out Any>): List<AggregateEventResult> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<AggregateEventResult>()
        usingScope(arrayOf(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW, MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME)) {
            aggregateEvents.ForEachExt { it, _ ->
                var ret = it.beforeAggregate(query);
                if (!ret.result) {
                    return@ForEachExt false;
                }
                list.add(AggregateEventResult(it, ret))
                return@ForEachExt true
            }
        }
        return list
    }

    fun onInserting(insert: MongoBaseInsertClip): List<InsertEventResult> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<InsertEventResult>()
        usingScope(arrayOf(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW, MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME)) {
            insertEvents.ForEachExt { it, _ ->
                var ret = it.beforeInsert(insert);
                if (!ret.result) {
                    return@ForEachExt false;
                }
                list.add(InsertEventResult(it, ret))
                return@ForEachExt true
            }
        }
        return list
    }

    fun onUpdating(update: MongoBaseUpdateClip): List<UpdateEventResult> {
//        var query = MongoBaseQueryClip(update.actualTableName);
//        query.whereData.putAll(update.whereData)
//        var chain = EventChain(query)

        //先判断是否进行了类拦截.
        var list = mutableListOf<UpdateEventResult>()
        usingScope(arrayOf(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW, MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME)) {
            updateEvents.ForEachExt { it, _ ->
                var ret = it.beforeUpdate(update);
                if (ret.result) {
                    list.add(UpdateEventResult(it, ret))
                }
                return@ForEachExt true
            }
        }
        return list
    }

    fun onDeleting(delete: MongoDeleteClip<*>): List<DeleteEventResult> {
//        var query = MongoBaseQueryClip(delete.actualTableName);
//        query.whereData.putAll(delete.whereData)

        //先判断是否进行了类拦截.
        var list = mutableListOf<DeleteEventResult>()
        usingScope(arrayOf(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW, MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME)) {
            deleteEvents.ForEachExt { it, _ ->
                var ret = it.beforeDelete(delete);
                if (ret.result) {
                    list.add(DeleteEventResult(it, ret))
                }
                return@ForEachExt true
            }
        }
        return list
    }


//    /**
//     * 在拦截器中获取数据源。
//     */
//    fun getDataSource(collectionName: String, isRead: Boolean): MongoTemplate? {
//        var ret: MongoTemplate? = null;
//
//        dataSources.firstOrNull { mongoDataSource ->
//            ret = mongoDataSource.run(collectionName, isRead)
//            if (ret == null) {
//                return@firstOrNull false;
//            }
//
//            return@firstOrNull true;
//        }
//
//        return ret;
//    }


    fun getActualTableName(collectionName: String): String {
        var ret = collectionName;

        /**
         * 按所有规则走一遍
         */
        collectionVarNames.all {
            it.run(ret).apply {
                if (this.HasValue) {
                    ret = this;
                }
            }
            return@all true;
        }

        return ret;
    }
}