package nbcp.db.sql

import nbcp.db.BaseMetaData

/**
 * Created by yuxh on 2018/7/18
 */
interface IDataGroup {
    /**
     * 获取该组下所有的 table ,collection 等.
     */
    fun getEntities():Set<BaseMetaData>
}