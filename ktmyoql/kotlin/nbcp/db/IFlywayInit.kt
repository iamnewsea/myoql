package nbcp.db

import nbcp.comm.FromJson
import nbcp.comm.JsonMap
import nbcp.comm.const
import nbcp.db.mongo.batchInsert
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
}