package nbcp.db.es.tool

import nbcp.comm.*
import nbcp.data.Sys
import nbcp.utils.*
import nbcp.db.*
import nbcp.tool.CodeGeneratorHelper

import java.io.File
import java.io.FileWriter
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.time.LocalDateTime

/**
 * 代码生成器
 */
class EsrGenerator4Java {
    private var nameMapping: StringMap = StringMap();
    private fun String.GetSafeKotlinName(): String {
        if (this.isEmpty()) return this;

        return this.split(".").map {
            if (Sys.devKeywords.contains(it)) {
                return@map """`${it}`"""
            }
            return@map it;
        }.joinToString(".")
    }

    private var targetEntityPathName: String = ""
    fun work(
        targetPath: String,  //目标文件
        basePackage: String,   //实体的包名
        packageName: String = "nbcp.db.es.table",
        packages: Array<String> = arrayOf(),   //import 包名
        entityFilter: ((Class<*>) -> Boolean) = { true },
        nameMapping: StringMap = StringMap(), // 名称转换
        ignoreGroups: List<String> = listOf("EsBase")  //忽略的包名
    ) {
        targetEntityPathName = MyUtil.joinFilePath(targetPath, packageName.split(".").joinToString("/"))
        this.nameMapping = nameMapping;

        var p = File.separator;

        File(targetEntityPathName).deleteRecursively();
        File(targetEntityPathName).mkdirs()

        var groups = getGroups(basePackage).filter { ignoreGroups.contains(it.key) == false };
        var embClasses = getEmbClasses(groups);

        println("开始生成 esr...")


        var fileHeader = """package ${packageName};

import nbcp.db.*;
import nbcp.db.es.*;
import nbcp.utils.*;
import nbcp.comm.*;
import java.util.*;
import java.util.stream.*;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.*;
${packages.map { "import " + it + ";" }.joinToString(const.line_break)}

//generate auto @${LocalDateTime.now().AsString()}
"""

        embClasses.forEach {
            writeToFile(it.simpleName + "Meta", fileHeader + genEmbEntity(it));
        }

        var count = 0;
        groups.forEach { group ->
            var groupName = group.key
            var groupEntities = group.value.filter(entityFilter)

            writeToFile(
                "${MyUtil.getBigCamelCase(groupName)}Group",
                fileHeader +
                        """
@Component("es.${groupName}")
@MetaDataGroup(dbType =DatabaseEnum.ElasticSearch, value = "${groupName}")
public class ${MyUtil.getBigCamelCase(groupName)}Group implements IDataGroup{
    @Override
    public Set<BaseMetaData> getEntities(){
        return new HashSet(){ { 
${
                            group.value
                                .map { "add(" + genVarName(it).GetSafeKotlinName() + ");" }
                                .map { it.ToTab(3) }
                                .joinToString("\n")
                        }
        }};
    }
"""
            )
            println("${groupName}:")
            groupEntities.forEach {
                count++;

                var dbName = it.getAnnotation(DbName::class.java)?.value.AsString(it.simpleName)

                dbName = MyUtil.getKebabCase(dbName)

                println("${count.toString().padStart(2, ' ')} 生成实体：${groupName}.${dbName}".ToTab(1))
                writeToFile("${MyUtil.getBigCamelCase(groupName)}Group", genVarEntity(it).ToTab(1))
            }

            writeToFile("${MyUtil.getBigCamelCase(groupName)}Group", "\n")

            groupEntities.forEach {
                writeToFile("${MyUtil.getBigCamelCase(groupName)}Group", genEntity(it).ToTab(1))
            }
            writeToFile("${MyUtil.getBigCamelCase(groupName)}Group", """}""")
        }

        writeToFile(
            "EsrUtil",
            fileHeader +

                    """
public class EsrUtil{
    public static EsColumnName esColumnJoin(String... args) {
        return new EsColumnName(Arrays.asList(args).stream().filter (it-> MyHelper.hasValue( it) ).collect(Collectors.joining(".")));
    }
}
"""
        )

        writeToFile(
            "EsrMetaMap",
            fileHeader +

                    """

public class EsrMetaMap {
    private String parentPropertyName;
    
    public EsrMetaMap(String... args){
        this.parentPropertyName = Arrays.asList(args).stream().filter (it-> MyHelper.hasValue( it) ).collect(Collectors.joining("."));
    }
    
    public String keys(String... keys) {
        return this.parentPropertyName + "." + Arrays.asList(keys).stream().filter (it-> MyHelper.hasValue( it) ).collect(Collectors.joining("."));
    }
}

"""
        )

        println("生成 mor 完成!")
    }

    var maxLevel = 9;


    fun writeToFile(className: String, content: String) {
        FileWriter(MyUtil.joinFilePath(targetEntityPathName, className + ".java"), true).use { moer_File ->
            moer_File.appendLine(content)
            moer_File.flush()
        }
    }

    fun getEntityName(name: String): String {
        var nameValue = name;
        nameMapping.forEach {
            nameValue = nameValue.replace(it.key, it.value)
        }
        return nameValue[0].lowercase() + nameValue.substring(1);
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

    /**
     * 递归返回嵌入实体。
     */
    fun findEmbClasses(clazz: Class<*>, deep: Int = 0): List<Class<*>> {

        if (deep == 6) return listOf();

        var ret = clazz.AllFields
            .filter {
                if (it.type.IsSimpleType()) return@filter false;
                if (Map::class.java.isAssignableFrom(it.type)) {
                    return@filter false;
                }

                return@filter true;
            }.map {
                if (it.type.isArray) {
                    return@map it.type.componentType;
                }
                if (List::class.java.isAssignableFrom(it.type)) {
                    return@map (it.genericType as ParameterizedType).GetActualClass(0, {
                        return@GetActualClass clazz.GetFirstTypeArguments()[0] as Class<*>;
                    })
                }
                return@map it.type;
            }.filter {
                if (it.IsSimpleType()) return@filter false;
                if (Map::class.java.isAssignableFrom(it)) {
                    return@filter false;
                }

                return@filter true;
            }
            .distinctBy { it.name }
            .toMutableList()

        var subClasses = mutableListOf<Class<*>>()
        ret.forEach {
            subClasses.addAll(findEmbClasses(it, deep + 1));
        }

        ret.addAll(subClasses);

        return ret.distinctBy { it.name }
    }

    fun getEmbClasses(groups: Map<String, MutableList<Class<*>>>): List<Class<*>> {
        return groups.values.Unwind()
            .map {
                return@map findEmbClasses(it)
            }
            .Unwind()
            .distinctBy { it.name }
            .sortedBy { it.name }
    }

    private fun getMetaValue(fieldName: String, fieldType: Class<*>, parentTypeName: String): String {


        if (fieldName == "id") {
            return "EsrUtil.esColumnJoin(this.parentPropertyName, \"_id\")"
        }

        if (fieldType.IsSimpleType()) {
            return "EsrUtil.esColumnJoin(this.parentPropertyName, \"${fieldName}\")"
        }

        if (fieldType.simpleName == parentTypeName) {
            return "EsrUtil.esColumnJoin(this.parentPropertyName, \"${fieldName}\") /*:递归类*/"
        }

        if (Map::class.java.isAssignableFrom(fieldType)) {
            return "new EsrMetaMap(this.parentPropertyName, \"${fieldName}\") /*:map*/"
        }

        if (fieldType.isArray) {
            return "EsrUtil.esColumnJoin(this.parentPropertyName, \"${fieldName}\") /*:array*/"
        }

        if (List::class.java.isAssignableFrom(fieldType)) {
            //应该递归.
            return ""
        }

        return """new ${
            fieldType.name.split(".").last()
        }Meta(EsrUtil.esColumnJoin(this.parentPropertyName,"${fieldName}"))""";
    }

    private fun getMetaValue(field: Field, parentType: Class<*>, parentTypeName: String, deepth: Int): String {

        if (deepth > maxLevel) {
            throw RuntimeException("-------------------已超过最大深度${field.name}:${field.type.name}-----------------");
        }

        var ret = getMetaValue(field.name, field.type, parentTypeName);
        if (ret.HasValue) return ret;


        if (List::class.java.isAssignableFrom(field.type)) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0, {
                return@GetActualClass parentType.GetFirstTypeArguments()[0] as Class<*>;
            })


            var v = getMetaValue(field.name, actType, parentTypeName);
            if (v.HasValue) return v;
        }

        return "----找不到getMetaValue----"
    }


    /**
     * @return key = 字段， value = 是否是基本类型。
     */
    private fun getEntityValue(name: String, clazz: Class<*>): Pair<String, Boolean> {

        val retTypeIsBasicType = true;

        if (name == "id") {
            return "\"_id\"" to retTypeIsBasicType;
        }

        if (clazz.IsSimpleType() ||
            Map::class.java.isAssignableFrom(clazz)
        ) {
            return "\"${name}\"" to retTypeIsBasicType
        }

        if (List::class.java.isAssignableFrom(clazz) ||
            clazz.isArray
        ) {
            //应该递归调用自己.
            return "" to retTypeIsBasicType
        }

        return """${clazz.name.split(".").last()}Meta("${name}")""" to false;
    }


    private fun getEntityValue(field: Field): Pair<String, Boolean> {
        var (ret, retTypeIsBasicType) = getEntityValue(field.name, field.type);
        if (ret.HasValue) return ret to retTypeIsBasicType;


        if (List::class.java.isAssignableFrom(field.type)) {
            var actType = (field.genericType as ParameterizedType).GetActualClass(0);

            var (ret2, retTypeIsBasicType2) = getEntityValue(field.name, actType);
            if (ret2.HasValue) return ret2 to retTypeIsBasicType2;
        } else if (field.type.isArray) {
            var (ret2, retTypeIsBasicType2) = getEntityValue(field.name, field.type.componentType);
            if (ret2.HasValue) return ret2 to retTypeIsBasicType2;
        }

        return "-----找不到getEntityValue-----" to false;
    }

    fun genEmbEntity(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var props_fun = entType.AllFields
            .filter { it.name != "Companion" }
            .map {
                var v1 = getMetaValue(it, entType, entTypeName, 1)
                var v1_type = "EsColumnName";
                if (v1.startsWith("new ")) {
                    var v1_index_end = v1.indexOf('(');
                    v1_type = v1.Slice(4, v1_index_end);
                }

                return@map """${CodeGeneratorHelper.getFieldComment(it)}${
                    JavaCoderUtil.getAnnotationCodes(it.annotations).map { const.line_break + it }.joinToString("")
                }
private ${v1_type} ${it.name} = null;
public ${v1_type} get${MyUtil.getBigCamelCase(it.name)}(){
    return ${it.name};
}""".removeEmptyLine()
            }


        var props_set = entType.AllFields
            .filter { it.name != "Companion" }
            .map {
                var v1 = getMetaValue(it, entType, entTypeName, 1)
                if (v1.startsWith("new ")) {
                    var v1_index_end = v1.indexOf('(');
                }

                return@map """ 
this.${it.name} = ${v1};
""".removeEmptyLine()
            }

        var entityTypeName = entTypeName;
//        var entityVarName = getEntityName(entTypeName);

        var ent = """public class ${entityTypeName}Meta extends EsColumnName {
    private String parentPropertyName;
    ${entityTypeName}Meta(String parentPropertyName) {
        this.parentPropertyName = parentPropertyName;
        
${props_set.map { const.line_break + it }.joinToString(const.line_break).ToTab(2)}
    }
    
    ${entityTypeName}Meta(EsColumnName value) {
        this(value.toString());
    }

${props_fun.map { const.line_break + it }.joinToString(const.line_break).ToTab(1)}

    @Override
    public String toString() {
        return EsrUtil.esColumnJoin(this.parentPropertyName).toString();
    }
}
"""
        return ent;
    }

    fun genVarName(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        return getEntityName(entTypeName);
    }

    private fun genVarEntity(entType: Class<*>): String {
        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }

        var entityTypeName = entTypeName + "Entity";
        var entityVarName = getEntityName(entTypeName);

        return """public ${entityTypeName} ${entityVarName} = new ${entityTypeName}();
public ${entityTypeName} ${entityVarName}(String collectionName){
    return new ${entityTypeName}(collectionName);
}""";
    }


    fun genEntity(entType: Class<*>): String {

        var entTypeName = entType.name.split(".").last();
        if (entTypeName.endsWith("\$Companion")) {
            return "";
        }
        var props = entType.AllFields
            .filter { it.name != "Companion" }
            .map {

                var (retValue, retTypeIsBasicType) = getEntityValue(it)
                if (retTypeIsBasicType) {
                    return@map "public EsColumnName ${it.name} = new EsColumnName(${retValue});".ToTab(1)
                } else {

                    var v1_type = "EsColumnName"
                    var l_index = retValue.indexOf('(')
                    if (l_index > 0) {
                        v1_type = retValue.Slice(0, l_index);
                    }

                    return@map "public ${v1_type} ${it.name} = new ${retValue};".ToTab(1)
                }
            }


        var entityTypeName = entTypeName + "Entity"
//        var entityVarName = getEntityName(entTypeName)

        var dbName = entType.getAnnotation(DbName::class.java)?.value.AsString(entType.simpleName)
        dbName = MyUtil.getKebabCase(dbName)


        var idMethods = mutableListOf<String>()

        //每一项是 用逗号分隔的主键组合
        var uks = mutableListOf<String>();

        uks.addAll(CodeGeneratorHelper.getEntityUniqueIndexesDefine(entType))

        uks.forEach { uk ->
            var keys = uk.split(",")

            //检测
            keys.forEach {
                if (entType.GetFieldPath(*it.split(".").toTypedArray()) == null) {
                    throw RuntimeException("${entTypeName} 找不到 ${it} 属性!")
                }
            }

            idMethods.add(
                """
    public EsQueryClip<${entityTypeName}, ${entType.name}> queryBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                } (${
                    keys.map {
                        "${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.javaTypeName
                        } ${MyUtil.getSmallCamelCase(it)}"
                    }.joinToString(",")
                }) {
        return this.query()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    public EsDeleteClip<${entityTypeName}> deleteBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                } (${
                    keys.map {
                        "${MyUtil.getSmallCamelCase(it)}: ${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.javaTypeName
                        }"
                    }.joinToString(",")
                }) {
        return this.delete()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }

    public EsUpdateClip<${entityTypeName}> updateBy${
                    keys.map { MyUtil.getBigCamelCase(it) }.joinToString("")
                } (${
                    keys.map {
                        "${MyUtil.getSmallCamelCase(it)}: ${
                            entType.GetFieldPath(
                                *it.split(".").toTypedArray()
                            )!!.type.javaTypeName
                        }"
                    }.joinToString(",")
                }) {
        return this.update()${keys.map { ".where{ it.${it} match ${MyUtil.getSmallCamelCase(it)} }" }.joinToString("")}
    }
"""
            )
        }

        val ent = """${
            CodeGeneratorHelper.getEntityComment(
                entType,
                ""
            )
        }${JavaCoderUtil.getAnnotationCodes(entType.annotations).map { const.line_break + it }.joinToString("")}
public class ${entityTypeName} extends EsBaseMetaEntity<${entType.name}> {
    public String collectionName;
    public String databaseId;

    public ${entityTypeName}(String collectionName,String databaseId){
        super(${entType.name.GetSafeKotlinName()}.class, "${dbName}", MyHelper.AsString(collectionName,"${dbName}"), databaseId);
    
        this.collectionName = collectionName;
        this.databaseId = databaseId;
    }
    
    public ${entityTypeName}(){
        this("","");
    }
    
        
${props.joinToString("\n")}
${idMethods.joinToString("\n")}

    public EsQueryClip<${entityTypeName}, ${entType.name}> query(){
        return new EsQueryClip(this);
    }
    
    public EsUpdateClip<${entityTypeName}, ${entType.name}> update(){
        return new EsUpdateClip(this);
    }
    
    public EsDeleteClip<${entityTypeName}> delete(){
        return new EsDeleteClip(this);
    }
}
"""

        return ent;
    }
}