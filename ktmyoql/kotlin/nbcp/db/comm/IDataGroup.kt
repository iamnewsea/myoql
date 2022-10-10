package nbcp.db

import java.io.Serializable


interface IDataGroup {
    /**
     * 获取该组下所有的 table ,collection 等.
     */
    fun getEntities(): Set<BaseMetaData<out Any>>
}

