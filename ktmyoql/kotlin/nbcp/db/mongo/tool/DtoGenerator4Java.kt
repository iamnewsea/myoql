package nbcp.db.mongo.tool

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.*
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime

/**
 * 代码生成器
 */
class DtoGenerator4Java {
    private var nameMapping: StringMap = StringMap();
    private var targetEntityPathName: String = ""

    fun work(
        targetPath: String,  //目标文件
        basePackage: String,   //实体的包名
        packageName: String = "nbcp.db.mongo.dto",
        packages: Array<String> = arrayOf(),   //import 包名
        entityFilter: ((Class<*>) -> Boolean) = { true },
        nameMapping: StringMap = StringMap(), // 名称转换
        ignoreGroups: List<String> = listOf("MongoBase")  //忽略的包名
    ) {
        targetEntityPathName = MyUtil.joinFilePath(targetPath, packageName.split(".").joinToString("/"))
        this.nameMapping = nameMapping;

        var p = File.separator;

//        var moer_Path = targetFileName.replace("/", p).replace("\\", p);

        File(targetEntityPathName).deleteRecursively();
        File(targetEntityPathName).mkdirs()


        var groups = getGroups(basePackage).filter { ignoreGroups.contains(it.key) == false };


        println("开始生成 mor...")

        var fileHeader = """package ${packageName};

import nbcp.db.*;
import nbcp.db.mongo.*;
import nbcp.utils.*;
import nbcp.comm.*;
import java.util.*;
import java.util.stream.*;

${packages.map { "import " + it + ";" }.joinToString(const.line_break)}

//generate auto @${LocalDateTime.now().AsString()}
"""
        var count = 0;
        groups.forEach { group ->
            var groupName = group.key
            var groupEntities = group.value.filter(entityFilter);



            println("${groupName}:")




            groupEntities.forEach {
                count++;
                println("${count.toString().padStart(2, ' ')} 生成实体：${groupName}.${it.simpleName}".ToTab(1))


                writeToFile(
                    it.simpleName + "DTO",

                    fileHeader +
                            """
public class ${it.simpleName}DTO extends ${it.simpleName} {
}
"""
                )
            }

        }
        println("生成 mor dto 完成!")
    }

    var maxLevel = 9;


    fun writeToFile(className: String, content: String) {
        var moer_File = FileWriter(MyUtil.joinFilePath(targetEntityPathName, className + ".java"), true);
        moer_File.appendLine(content)
        moer_File.flush()
        moer_File.close();
    }


    fun getGroups(basePackage: String): HashMap<String, MutableList<Class<*>>> {
        var ret = HashMap<String, MutableList<Class<*>>>();


        ClassUtil.findClasses(basePackage)
            .filter { it.isAnnotationPresent(DbEntityGroup::class.java) }
            .forEach {
                var groupName = it.getAnnotation(DbEntityGroup::class.java).value;

                if (ret.containsKey(groupName) == false) {
                    ret[groupName] = mutableListOf();
                }

                ret[groupName]!!.add(it)
            }


        return ret
    }

}