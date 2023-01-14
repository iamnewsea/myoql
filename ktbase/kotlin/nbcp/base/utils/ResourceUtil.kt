package nbcp.base.utils

import nbcp.base.extend.HasValue
import nbcp.base.extend.readContentString
import org.springframework.core.io.ClassPathResource
import java.net.JarURLConnection
import java.net.URL

object ResourceUtil {
    /**
     * 判断是否存在资源
     */
    @JvmStatic
    fun existsResource(path: String): Boolean {
        return ClassPathResource(path).exists()
    }

    /**
     * 从 resource 读取内容
     */
    @JvmStatic
    fun readResourceContentText(path: String): String {
        return ClassPathResource(path).inputStream.readContentString();
    }

    @JvmStatic
    fun readResourceContentBytes(path: String): ByteArray {
        return ClassPathResource(path).inputStream.readBytes();
    }



    @JvmOverloads
    @JvmStatic
    fun findResources(url: URL, basePath: String, filter: ((String) -> Boolean)? = null): List<String> {
        //转换为JarURLConnection
        val connection = url.openConnection()
        if (connection == null) {
            return listOf();
        }

        if (connection is JarURLConnection) {
            val jarFile = connection.jarFile
            if (jarFile == null) {
                return listOf();
            }

            var list = mutableListOf<String>()
            //得到该jar文件下面的类实体
            val jarEntryEnumeration = jarFile.entries()
            while (jarEntryEnumeration.hasMoreElements()) {
                val entry = jarEntryEnumeration.nextElement()
                val jarEntryName = entry.getName()

                if (jarEntryName.startsWith(basePath) == false) {
                    continue
                }


                if (filter?.invoke(jarEntryName) ?: true) {
                    list.add(jarEntryName);
                }
            }
            return list;
        } else {
            var list = mutableListOf<String>()
            var base = url.file.split("/target/classes/")[1]

            connection.inputStream.readContentString()
                    .split("\n")
                    .filter { it.HasValue }
                    .forEach { it ->
                        var jarClassName = base + "/" + it;

                        if (filter?.invoke(jarClassName) ?: true) {
                            list.add(jarClassName);
                        }
                    }
            return list;
        }

        //throw java.lang.RuntimeException("不识别的类型:${connection::class.java.name}!")
    }

    /**
     * @param basePath: 前后不带/
     */
    @JvmOverloads
    @JvmStatic
    fun findResources(basePath: String, filter: ((String) -> Boolean)? = null): List<String> {
        val resource = ClassPathResource(basePath);
        if (resource.exists() == false) return listOf();
        return findResources(resource.url, basePath.trim('/'), filter)
    }
}