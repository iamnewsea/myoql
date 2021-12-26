package nbcp.db

import com.mongodb.client.model.IndexOptions
import nbcp.comm.AsString
import nbcp.comm.FromJson
import nbcp.comm.JsonMap
import nbcp.comm.const
import nbcp.db.mongo.MongoBaseMetaCollection
import nbcp.db.mongo.batchInsert
import org.bson.Document
import org.springframework.core.io.ClassPathResource

abstract class FlywayVersionBaseService(val version: Int) {
    abstract fun initData();

    /**
     * @param itemFunc: 参数：实体名，文件名全路径，所有行数据。返回false停止。
     */
    fun loadResource(
        resourcePath: String,
        fileExt: String,
        itemFunc: (String, String, List<String>) -> Boolean
    ): Boolean {
        return ClassPathResource(resourcePath)
            .file
            .listFiles()
            .filter { it.isFile && it.name.endsWith(fileExt, true) }
            .all {
                var fileName = it.path
                var tableName = fileName.replace("\\", "/").split("/").last().split(".").first()
                return@all itemFunc.invoke(tableName, fileName, it.readLines(const.utf8))
            }
    }


    /**
     * 初始化数据,目录：flyway-v${version}, 文件后缀 .dat
     */
    fun addResourceData() {
        loadResource("flyway-v${version}", ".dat") { tableName, fileName, lines ->
            var insert = db.mongo.mongoEvents.getCollection(tableName)!!.batchInsert()
            insert.addEntities(lines.map { it.FromJson<JsonMap>()!! })
            insert.exec();
            return@loadResource true;
        }
    }


    private fun DbEntityIndex.indexName(): String {
        return "i_" + this.value.sortedBy { it.length.toString().padStart(3, '0') + it }.joinToString("_")
    }

    private fun DbEntityIndex.toDocument(): Document {
        return Document(JsonMap(this.value.map { it to 1 }))
    }


    fun <M : MongoBaseMetaCollection<Any>> M.createTable() {
        var mongoTemplate = this.getMongoTemplate()
        var db = mongoTemplate.db;
        if (mongoTemplate.collectionExists(this.tableName) == false) {
            db.createCollection(this.tableName);
        }
    }

    fun <M : MongoBaseMetaCollection<Any>> M.createIndex(dbEntityIndex: DbEntityIndex) {
        var mongoTemplate = this.getMongoTemplate()
        var db = mongoTemplate.db;
        var collection = db.getCollection(this.tableName)


        if (collection.listIndexes()
                .toList()
                .map { it.get("name").AsString() }
                .contains(dbEntityIndex.indexName()) == false
        ) {
            collection.createIndex(
                dbEntityIndex.toDocument(),
                IndexOptions().name(dbEntityIndex.indexName()).unique(dbEntityIndex.unique)
            )
        }
    }

    fun initMongoIndex() {
        db.mongo.groups.forEach {
            it.getEntities().forEach { ent ->
                (ent as MongoBaseMetaCollection<Any>)
                    .apply {
                        this.createTable()

                        this.entityClass.getAnnotationsByType(DbEntityIndex::class.java).forEach { index ->
                            this.createIndex(index);
                        }
                    }
            }
        }
    }
}