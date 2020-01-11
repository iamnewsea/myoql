package nbcp.db.mongo

import nbcp.base.extend.ForEachExt
import nbcp.db.*
import nbcp.db.mongo.*
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component

@Component
class MongoEntityEvent : BeanPostProcessor {
    companion object {
        val groups = mutableSetOf<IDataGroup>()
        val dustbinEntitys = mutableSetOf<Class<*>>()  //mongo meta class
        val refsMap = mutableListOf<DbEntityFieldRefData>()
        val updateEvent = mutableListOf<IDbEntityUpdate>()
        val deleteEvent = mutableListOf<IDbEntityDelete>()
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {

        if (bean is IDataGroup) {
            groups.add(bean)

            bean.getEntities().forEach { moer ->
                if (moer is MongoBaseEntity<*>) {
                    var entityClass = moer.entityClass

                    addDustbin(entityClass)
                    addRef(entityClass)

                }
            }
        }
        if (bean is IDbEntityUpdate) {
            var ann = bean::class.java.getAnnotation(DbEntityUpdate::class.java)
            if (ann != null) {
                updateEvent.add(bean)
            }
        }


        if (bean is IDbEntityDelete) {
            var ann = bean::class.java.getAnnotation(DbEntityDelete::class.java)
            if (ann != null) {
                deleteEvent.add(bean)
            }
        }

        return super.postProcessAfterInitialization(bean, beanName)
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


    fun onUpdating(update: MongoUpdateClip<*>): Array<Pair<IDbEntityUpdate, DbEntityEventResult?>> {
        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IDbEntityUpdate, DbEntityEventResult?>>()
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

    fun onDeleting(delete: MongoDeleteClip<*>): Array<Pair<IDbEntityDelete, DbEntityEventResult?>> {

        //先判断是否进行了类拦截.
        var list = mutableListOf<Pair<IDbEntityDelete, DbEntityEventResult?>>()
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