package nbcp.db.mongo

import nbcp.comm.ForEachExt
import nbcp.comm.HasValue
import nbcp.scope.*
import nbcp.comm.usingScope
import nbcp.db.*
import nbcp.db.mongo.event.*
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.io.Serializable


@Component
@ConditionalOnClass(MongoTemplate::class)
class MongoEntityCollector : BeanPostProcessor {
    companion object {
        //需要删 除后放入垃圾箱的实体
        @JvmStatic
        val dustbinEntities = mutableSetOf<Class<*>>()  //mongo entity class

//        //逻辑删
//        @JvmStatic
//        val logicalDeleteEntities = mutableSetOf<Class<*>>()

        @JvmStatic
        val logHistoryMap = linkedMapOf<Class<*>, Array<String>>()

        // 冗余字段的引用。如 user.corp.name 引用的是  corp.name
        @JvmStatic
        val refsMap = mutableListOf<DbEntityFieldRefData>()

        //注册的 Update Bean
        @JvmStatic
        val queryEvents = mutableListOf<IMongoEntityQuery>()

        @JvmStatic
        val insertEvents = mutableListOf<IMongoEntityInsert>()

        //注册的 Update Bean
        @JvmStatic
        val updateEvents = mutableListOf<IMongoEntityUpdate>()

        //注册的 Delete Bean
        @JvmStatic
        val deleteEvents = mutableListOf<IMongoEntityDelete>()

        @JvmStatic
        val dataSources = mutableListOf<IMongoDataSource>()

        @JvmStatic
        val collectionVarNames = mutableListOf<IMongoCollectionVarName>()
    }

    /**
     * 根据名称查找定义的集合。
     */
    fun getCollection(collectionName: String): MongoBaseMetaCollection<Any>? {
        var ret: BaseMetaData? = null
        db_mongo.groups.any { group ->
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
            if (group.dbType == DatabaseEnum.Mongo) {
                db_mongo.groups.add(bean)

                bean.getEntities()
                    .forEach { moer ->
                        if (moer is MongoBaseMetaCollection<*>) {
                            /**
                             * 这里使用了实体的类．
                             */
                            //TODO 使用元数据类，会更好一些．但需要把实体注解，全部转移到元数据类上．
                            var entityClass = moer.entityClass

                            addLogicalDelete(entityClass)
                            addDustbin(entityClass)
                            addRef(entityClass)
                            addLogHistory(entityClass);
                        }
                    }
            }
        }

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

        if (bean is IMongoDataSource) {
            dataSources.add(bean);
        }
        if (bean is IMongoCollectionVarName) {
            collectionVarNames.add(bean);
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }

    private fun addLogHistory(entityClass: Class<out Any>) {
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

    private fun addLogicalDelete(entityClass: Class<out Any>) {
//        var logicalDelete = entityClass.getAnnotation(LogicalDelete::class.java)
//        if (logicalDelete != null) {
//            logicalDeleteEntities.add(entityClass);
//        }
    }

    private fun addDustbin(entityClass: Class<out Any>) {
        var logicalDelete = entityClass.getAnnotation(LogicalDelete::class.java)
        if (logicalDelete != null) {
            return;
        }

        var dustbin = entityClass.getAnnotation(RemoveToSysDustbin::class.java)
        if (dustbin != null) {
            dustbinEntities.add(entityClass)
        }
    }

    fun onQuering(query: MongoBaseQueryClip): Array<Pair<IMongoEntityQuery, EventResult>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IMongoEntityQuery, EventResult>>()
        usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
            queryEvents.ForEachExt { it, _ ->
                var ret = it.beforeQuery(query);
                if (!ret.result) {
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
        usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
            insertEvents.ForEachExt { it, _ ->
                var ret = it.beforeInsert(insert);
                if (!ret.result) {
                    return@ForEachExt false;
                }
                list.add(it to ret)
                return@ForEachExt true
            }
        }
        return list.toTypedArray()
    }

    fun onUpdating(update: MongoBaseUpdateClip): List<UpdateEventResult> {
        var query = MongoBaseQueryClip(update.collectionName);
        query.whereData.addAll(update.whereData)
        var chain = EventChain(query)

        //先判断是否进行了类拦截.
        var list = mutableListOf<UpdateEventResult>()
        usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
            updateEvents.ForEachExt { it, _ ->
                var ret = it.beforeUpdate(update, chain);
                if (ret.result) {
                    list.add(UpdateEventResult(it, chain, ret))
                }
                return@ForEachExt true
            }
        }
        return list
    }

    fun onDeleting(delete: MongoDeleteClip<*>): List<DeleteEventResult> {

        var query = MongoBaseQueryClip(delete.collectionName);
        query.whereData.addAll(delete.whereData)
        var chain = EventChain(query)

        //先判断是否进行了类拦截.
        var list = mutableListOf<DeleteEventResult>()
        usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
            deleteEvents.ForEachExt { it, _ ->
                var ret = it.beforeDelete(delete, chain);
                if (ret.result) {
                    list.add(DeleteEventResult(it, chain, ret))
                }
                return@ForEachExt true
            }
        }
        return list
    }


    /**
     * 在拦截器中获取数据源。
     */
    fun getDataSource(collectionName: String, isRead: Boolean): MongoTemplate? {
        var ret: MongoTemplate? = null;

        dataSources.firstOrNull { mongoDataSource ->
            ret = mongoDataSource.run(collectionName, isRead)
            if (ret == null) {
                return@firstOrNull false;
            }

            return@firstOrNull true;
        }

        return ret;
    }


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