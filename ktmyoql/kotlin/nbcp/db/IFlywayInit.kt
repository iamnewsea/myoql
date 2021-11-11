package nbcp.db

import nbcp.comm.const
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
}