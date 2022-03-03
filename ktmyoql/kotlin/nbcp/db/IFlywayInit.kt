package nbcp.db

import com.mongodb.client.model.IndexOptions
import nbcp.comm.*
import nbcp.db.mongo.MongoBaseMetaCollection
import nbcp.db.mongo.MongoSetEntityUpdateClip
import nbcp.db.mongo.batchInsert
import nbcp.db.mongo.updateWithEntity
import nbcp.utils.ClassUtil
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource

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
        itemFunc: (String, List<String>) -> Boolean
    ): Boolean {
        return ClassUtil.findResources(resourcePath).map { it.split('/').filter { it.HasValue }.last() }
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
                return@all itemFunc.invoke(tableName, content.split("\n").filter { it.HasValue })
            }
    }


    /**
     * 初始化数据,目录：flyway-v${version}, 文件后缀 .dat
     */
    fun addResourceData(tableName: String = "", autoSave: Boolean = false) {
        loadResource("flyway-v${version}", tableName, ".dat") { tableName, lines ->
            var count = 0;
            if (autoSave) {
                lines.map { it.FromJson<JsonMap>()!! }
                    .forEach {
                        count += db.mongo.dynamicEntity(tableName).updateWithEntity(it).exec();
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
//            .sortedBy { it.length.toString().padStart(3, '0') + it }
            .map { it.replace(".", "_") }
            .joinToString(".")
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

        var indexes = this.entityClass.getAnnotationsByType(DbEntityIndex::class.java)
            .filter { it.value.any() }
            .filter { it.value.size == 1 && it.value.first() != "id" }
            .map { it.indexName() }

        collection.listIndexes().toList()
            .map { it.get("name").AsString() }
            .intersect(indexes)
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
            collection.createIndex(
                dbEntityIndex.toDocument(),
                IndexOptions().name(indexName).unique(dbEntityIndex.unique)
            )
        }
    }

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