package nbcp.db

import nbcp.comm.ConvertJson
import nbcp.comm.HasValue
import nbcp.comm.JsonMap
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.InitializingBean


data class DataSourceConnectionProperties(
    var uri: String = "",
    var username: String = "",
    var password: String = "",
    var port: Int = 0
)

data class DataSourceReadWriteProperties(
    var ds: DataSourceConnectionProperties = DataSourceConnectionProperties(),
    var tables: List<String> = listOf(),
    var readTables: List<String> = listOf()
)

abstract class MyOqlMultipleDataSourceDefine(val baseConfigPrefix: String) : InitializingBean {
    private var map = mutableMapOf<String, DataSourceReadWriteProperties>()
    private var dbReadDataSource: MutableMap<String, String> = mutableMapOf()
    private var dbWriteDataSource: MutableMap<String, String> = mutableMapOf()


    /**
     * 返回配置数据源的名称
     */
    fun getDataSourceName(name: String, isRead: Boolean? = null): String {
        if (isRead != null && isRead) {
            dbReadDataSource.getOrDefault(name, "")
                .apply {
                    if (this.HasValue) {
                        return this;
                    }
                }
        }

        return dbWriteDataSource.getOrDefault(name, "")
    }

    override fun afterPropertiesSet() {
        this.dbReadDataSource = mutableMapOf()
        this.dbWriteDataSource = mutableMapOf()

        var map2 = org.springframework.boot.context.properties.bind.Binder.get(SpringUtil.context.environment)
            .bindOrCreate(baseConfigPrefix, JsonMap::class.java)
        map2.forEach {
            if (it.value is Map<*, *> == false) {
                return@forEach
            }

            var v = (it.value as MutableMap<String, Any?>)
            if (v.containsKey("tables")) {
                v.put("tables", getList(v.get("tables")))
            }
            if (v.containsKey("read-tables")) {
                v.put("read-tables", getList(v.get("read-tables")))
            }
            this.map.put(it.key, it.value!!.ConvertJson(DataSourceReadWriteProperties::class.java))
        }

        map.forEach {
            var key = it.key
            var collections = it.value;
            this.dbReadDataSource.putAll(collections.readTables.map { it to key })
            this.dbWriteDataSource.putAll(collections.tables.map { it to key })
        }
    }

    private fun getList(v: Any?): List<Any?> {
        if (v == null) return listOf();
        if (v is Map<*, Any?>) {
            return v.values.toList();
        }
        if (v is List<Any?>) return v;
        throw RuntimeException("不识别的类型:${v::class.java.simpleName}")
    }
}
