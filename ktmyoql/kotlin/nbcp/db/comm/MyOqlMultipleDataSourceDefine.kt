package nbcp.db

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

abstract class MyOqlMultipleDataSourceDefine() : InitializingBean {
    private var map = mutableMapOf<String, DataSourceReadWriteProperties>()


    private var dbReadDataSource: MutableMap<String, String> = mutableMapOf()
    private var dbWriteDataSource: MutableMap<String, String> = mutableMapOf()


    /**
     * 返回配置数据源的名称
     */
    fun getDataSourceName(name: String, isRead: Boolean? = null): String {
        if (isRead != null && isRead) {
            return dbReadDataSource.getOrDefault(name, "")
        }

        return dbWriteDataSource.getOrDefault(name, "")
    }

    override fun afterPropertiesSet() {
        this.dbReadDataSource = mutableMapOf()
        this.dbWriteDataSource = mutableMapOf()

        map.forEach {
            var key = it.key
            var collections = it.value;
            this.dbReadDataSource.putAll(collections.readTables.map { it to key })
            this.dbWriteDataSource.putAll(collections.tables.map { it to key })
        }
    }
}
