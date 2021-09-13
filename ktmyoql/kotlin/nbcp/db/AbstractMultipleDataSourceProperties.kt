package nbcp.db

import org.springframework.beans.factory.InitializingBean

/**
 * 多数据定义
 */
abstract class AbstractMultipleDataSourceProperties : InitializingBean {
    /**
     *app:
     *  mongo:
     *    yapi-ds: mongodb://dev:123@mongo:27017/yapi
     *    db:
     *      yapi:
     *          - group
     *          - project
     *          - api
     *          - interface_cat
     *    read:
     *      yapi-read:
     *          - group
     *          - project
     */

    /**
     * key 数据库的bean名称。value 是表名
     */
    var db: Map<String, List<String>> = mapOf()
    var read: Map<String, List<String>> = mapOf()
    var write: Map<String, List<String>> = mapOf()

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

        db.forEach {
            var key = it.key
            var collections = it.value;
            this.dbReadDataSource.putAll(collections.map { it to key })
            this.dbWriteDataSource.putAll(collections.map { it to key })
        }

        read.forEach {
            var key = it.key
            var collections = it.value;
            this.dbReadDataSource.putAll(collections.map { it to key })
        }

        write.forEach {
            var key = it.key
            var collections = it.value;
            this.dbWriteDataSource.putAll(collections.map { it to key })
        }
    }
}