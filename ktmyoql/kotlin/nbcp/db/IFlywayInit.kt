package nbcp.db

import com.mongodb.client.model.IndexOptions
import nbcp.comm.*
import nbcp.db.mongo.MongoBaseMetaCollection
import nbcp.db.mongo.MongoSetEntityUpdateClip
import nbcp.db.mongo.batchInsert
import nbcp.db.mongo.updateWithEntity
import org.bson.Document
import org.springframework.core.io.ClassPathResource

abstract class FlywayVersionBaseService(val version: Int) {
    abstract fun exec();

    /**
     * @param itemFunc: 参数：实体名，文件名全路径，所有行数据。返回false停止。
     */
    fun loadResource(
            resourcePath: String,
            fileName: String,
            fileExt: String,
            itemFunc: (String, String, List<String>) -> Boolean
    ): Boolean {
        return ClassPathResource(resourcePath)
                .file
                .listFiles()
                .filter { it.isFile && it.name.endsWith(fileExt, true) }
                .filter {
                    if (fileName.isEmpty()) {
                        return@filter true;
                    }

                    return@filter it.name == fileName + fileExt
                }
                .all {
                    var fileName = it.path
                    var tableName = fileName.replace("\\", "/").split("/").last().split(".").first()
                    return@all itemFunc.invoke(tableName, fileName, it.readLines(const.utf8))
                }
    }


    /**
     * 初始化数据,目录：flyway-v${version}, 文件后缀 .dat
     */
    fun addResourceData(tableName: String = "", autoSave: Boolean = false) {
        loadResource("flyway-v${version}", tableName, ".dat") { tableName, fileName, lines ->
            if (autoSave) {
                lines.map { it.FromJson<JsonMap>()!! }.forEach {
                    db.mongo.mongoEvents.getCollection(tableName)!!.updateWithEntity(it).exec();
                }
            } else {
                var insert = db.mongo.mongoEvents.getCollection(tableName)!!.batchInsert()
                insert.addEntities(lines.map { it.FromJson<JsonMap>()!! })
                insert.exec();
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
                    if (it == "id") return@map "_id";
                    if (it.endsWith("._id")) return@map it.Slice(0, -4) + ".id"
                    return@map it;
                }
                .map { it to 1 })
        )
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
                .map { it.indexName() }

        collection.listIndexes().toList()
                .map { it.get("name").AsString() }
                .intersect(indexes)
                .forEach {
                    collection.dropIndex(it);
                }
    }

    fun <M : MongoBaseMetaCollection<Any>> M.createIndex(dbEntityIndex: DbEntityIndex) {
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

    fun initMongoIndex(rebuild: Boolean = true) {
        db.mongo.groups.forEach {
            it.getEntities().forEach { ent ->
                (ent as MongoBaseMetaCollection<Any>)
                        .apply {
                            this.createTable()

                            if (rebuild) {
                                this.dropDefineIndexes();
                            }

                            this.entityClass.getAnnotationsByType(DbEntityIndex::class.java).forEach { index ->
                                createIndex(index);
                            }
                        }
            }
        }
    }
}