package nbcp.db.es


import nbcp.base.extend.*
import nbcp.base.utils.SpringUtil
import nbcp.db.db

import org.slf4j.LoggerFactory
import java.lang.Exception


/**
 * es 元数据实体的基类
 */
abstract class EsBaseEntity<T : IEsDocument>(val entityClass: Class<T>, entityName: String) : BaseDbEntity(entityName) {
    //    abstract fun getColumns(): Array<String>;
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }


    /**
     * 插入单条实体
     */
    fun doInsert(entity: T): String {
        var batchInsert = EsBaseInsertClip(this.tableName)
        batchInsert.addEntity(entity)
        batchInsert.exec();
        return entity.id
    }
}


