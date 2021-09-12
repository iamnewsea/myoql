package nbcp.db.es


import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db

import org.slf4j.LoggerFactory
import java.lang.Exception
import nbcp.db.*;

/**
 * es 元数据实体的基类
 */
abstract class EsBaseMetaEntity<T : IEsDocument>(val entityClass: Class<T>, entityName: String) : BaseMetaData(entityName) {
    //    abstract fun getColumns(): Array<String>;
    companion object {
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


