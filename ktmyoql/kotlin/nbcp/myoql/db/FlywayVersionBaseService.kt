package nbcp.myoql.db

import com.mongodb.client.model.IndexOptions
import nbcp.base.comm.*
import nbcp.db.mongo.MongoBaseMetaCollection
import nbcp.db.mongo.batchInsert
import nbcp.db.mongo.updateWithEntity
import nbcp.utils.ClassUtil
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import java.io.Serializable

abstract class FlywayVersionBaseService(val version: Int) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    abstract fun exec();

    /**
     * @param itemFunc: 参数：实体名，文件名全路径，所有行数据。返回false停止。
     */
    fun loadResource(
        resourcePath: String,
        fileName: String,
        fileExt: String,
        tableCallback: (String, List<String>) -> Boolean
    ): Boolean {
        return ClassUtil.findResources(resourcePath).map {
            return@map it.substring(resourcePath.length + 1)
                .split('/')
                .filter { it.HasValue }
                .lastOrNull() ?: ""
        }
            .filter { it.endsWith(fileExt, true) }
            .filter {
                if (fileName.isEmpty()) {
                    return@filter true;
                }

                return@filter it == fileName + fileExt
            }
            .all {
                var tableName = it.split(".").first()
                var content = ClassPathResource(resourcePath + "/" + it).inputStream.readContentString()
                return@all tableCallback.invoke(tableName, content.split("\n").filter { it.HasValue })
            }
    }

    /**
     * 推送所有数据
     */
    fun pushAllResourcesData(autoSave: Boolean) {
        return addResourceData("", autoSave)
    }

    fun pushResourcesData(tableName: String, autoSave: Boolean) {
        if (tableName.isEmpty()) {
            throw RuntimeException("tableName不能为空")
        }
        return addResourceData(tableName, autoSave)
    }

    /**
     * 初始化数据,目录：flyway-v${version}, 文件后缀 .dat
     */
    private fun addResourceData(tableName: String = "", autoSave: Boolean = true) {
        loadResource("flyway-v${version}", tableName, ".dat") { tableName, lines ->
            var count = 0;
            if (autoSave) {
                lines.map { it.FromJson<JsonMap>()!! }
                    .forEach {
                        count += db.mongo.dynamicEntity(tableName).updateWithEntity(it).doubleExecSave();
                    }

                logger.Important("同步:${tableName},${count}条数据")
            } else {
                var insert = db.mongo.dynamicEntity(tableName).batchInsert()
                insert.addEntities(lines.map { it.FromJson<JsonMap>()!! })
                insert.exec();
                logger.Important("添加:${tableName},${count}条数据")
            }
            return@loadResource true;
        }
    }


    private fun DbEntityIndex.indexName(): String {
        return "i." + this.value
            .joinToString("_")
    }

    private fun DbEntityIndex.toDocument(): Document {
        return Document(JsonMap(this.value
            .map {
                return@map db.mongo.getMongoColumnName(it);
            }
            .map { it to 1 }
        ))
    }


    fun <M : MongoBaseMetaCollection<Any>> M.createTable() {
        var mongoTemplate = this.getMongoTemplate()
        var db = mongoTemplate.db;
        if (mongoTemplate.collectionExists(this.tableName) == false) {
            db.createCollection(this.tableName);
        }
    }

    fun <M : MongoBaseMetaCollection<Any>> M.dropDefineIndexes() {
        var mongoTemplate = this.getMongoTemplate()
        var db = mongoTemplate.db;
        var collection = db.getCollection(this.tableName)

//        var indexes = this.entityClass.getAnnotationsByType(DbEntityIndex::class.java)
//            .filter { it.value.any() }
//            .filter { it.value.size == 1 && it.value.first() != "id" }
//            .map { it.indexName() }

        collection.listIndexes()
            .toList()
            .map { it.get("name").AsString() }
            .filter { it.startsWith("i.") } // i. 表示是组件自动创建的索引
            .forEach {
                collection.dropIndex(it);
            }
    }

    fun <M : MongoBaseMetaCollection<Any>> M.createIndex(dbEntityIndex: DbEntityIndex) {
        //忽略id！
        if (dbEntityIndex.value.any() == false) {
            return;
        }

        if (dbEntityIndex.value.size == 1 && dbEntityIndex.value.first() == "id") {
            return;
        }

        var mongoTemplate = this.getMongoTemplate()
        var db = mongoTemplate.db;
        var collection = db.getCollection(this.tableName)

        var indexName = dbEntityIndex.indexName();

        if (collection.listIndexes()
                .toList()
                .map { it.get("name").AsString() }
                .contains(indexName) == false
        ) {
            try {
                collection.createIndex(
                    dbEntityIndex.toDocument(),
                    IndexOptions().name(indexName).unique(dbEntityIndex.unique)
                )
            } catch (ex: Throwable) {
                throw RuntimeException(
                    "创建索引失败: ${this.tableName} ${dbEntityIndex.ToJson()},${ex.message}",
                    ex
                )
            }
        }
    }

    @JvmOverloads
    fun initMongoIndex(callback: ((MongoBaseMetaCollection<Any>) -> Boolean)? = null, rebuild: Boolean = true) {
        db.mongo.groups.forEach {
            it.getEntities().forEach { ent ->
                (ent as MongoBaseMetaCollection<Any>)
                    .apply {
                        if (callback != null && callback(this) == false) {
                            return@forEach
                        }
                        var indexes = this.entityClass.getAnnotationsByType(DbEntityIndex::class.java);

                        if (indexes.any() == false) {
                            return@apply
                        }

                        this.createTable()

                        if (rebuild) {
                            this.dropDefineIndexes();
                        }


                        indexes.forEach { index ->
                            createIndex(index);
                        }
                    }
            }
        }
    }
}