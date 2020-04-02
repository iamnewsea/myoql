package nbcp.db.sql

import nbcp.base.extend.ForEachExt
import nbcp.db.*
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

/**
 * 事件处理中心
 */
@Component
class SqlEntityEvent : BeanPostProcessor {
    companion object {
        //需要删 除后放入垃圾箱的实体
        val dustbinEntitys = mutableSetOf<Class<*>>()  //mongo meta class
        // 冗余字段的引用。如 user.corp.name 引用的是  corp.name
        val refsMap = mutableListOf<DbEntityFieldRefData>()
        //注册的 Insert Bean
        val insertEvent = mutableListOf<ISqlEntityInsert>()

        //注册的 Update Bean
        val updateEvent = mutableListOf<ISqlEntityUpdate>()
        //注册的 Delete Bean
        val deleteEvent = mutableListOf<ISqlEntityDelete>()
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {

        if (bean is IDataGroup) {
            db.sql.groups.add(bean)

            bean.getEntities().forEach { moer ->
                if (moer is SqlBaseTable<*>) {
                    var entityClass = moer.tableClass

                    addDustbin(entityClass)
                    addRef(entityClass)

                }
            }
        }

        if( bean is ISqlEntityInsert){
            insertEvent.add(bean)
        }

        if (bean is ISqlEntityUpdate) {
            updateEvent.add(bean)
        }

        if (bean is ISqlEntityDelete) {
            deleteEvent.add(bean)
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }


    private fun addRef(entityClass: Class<out IBaseDbEntity>) {
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

    private fun addDustbin(entityClass: Class<out IBaseDbEntity>) {
        var dustbin = entityClass.getAnnotation(RemoveToSysDustbin::class.java)
        if (dustbin != null) {
            dustbinEntitys.add(entityClass)
        }
    }


    fun onInserting(insert: SqlInsertClip<*, *>): Array<Pair<ISqlEntityInsert, DbEntityEventResult?>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<ISqlEntityInsert, DbEntityEventResult?>>()
        insertEvent.ForEachExt { it, index ->
            var ret = it.beforeInsert(insert);
            if (ret != null && ret.result == false) {
                return@ForEachExt false;
            }
            list.add(it to ret)
            return@ForEachExt true
        }
        return list.toTypedArray()
    }


    fun onUpdating(update: SqlUpdateClip<*, *>): Array<Pair<ISqlEntityUpdate, DbEntityEventResult?>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<ISqlEntityUpdate, DbEntityEventResult?>>()
        updateEvent.ForEachExt { it, index ->
            var ret = it.beforeUpdate(update);
            if (ret != null && ret.result == false) {
                return@ForEachExt false;
            }
            list.add(it to ret)
            return@ForEachExt true
        }
        return list.toTypedArray()
    }

    fun onDeleting(delete: SqlDeleteClip<*, *>): Array<Pair<ISqlEntityDelete, DbEntityEventResult?>> {

        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<ISqlEntityDelete, DbEntityEventResult?>>()
        deleteEvent.ForEachExt { it, index ->
            var ret = it.beforeDelete(delete);
            if (ret != null && ret.result == false) {
                return@ForEachExt false;
            }
            list.add(it to ret)
            return@ForEachExt true
        }
        return list.toTypedArray()
    }
}