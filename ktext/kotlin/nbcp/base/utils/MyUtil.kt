package nbcp.base.utils

import nbcp.comm.*
import nbcp.base.extend.*
import nbcp.db.CodeName
import java.io.File
import java.io.FileInputStream
import java.net.JarURLConnection
import java.net.URL
import java.time.*
import java.util.*
import java.util.jar.JarEntry
import java.util.Enumeration
import java.util.jar.JarFile


/**
 * Created by nbcp on 2017/3/28.
 */

/**
 * 工具类。
 */

object MyUtil {
    //    val OneHourSeconds: Int = 3600000;
//    val OneDayMilliseconds: Int = 86400000;
    val OneDaySeconds: Int = 86400;
    val SystemTimeZoneOffsetTotalSeconds = ZoneId.systemDefault().rules.getStandardOffset(Date().toInstant()).totalSeconds  //系统时区相差的秒数
    @JvmStatic
    private val random = Random();
    //    /**
//     * 北京时间的今天凌晨。
//     */
    val today: LocalDate
        get() {
            return LocalDate.now();
        }

    fun isLocalIp(Ip: String): Boolean {
        return Ip.isEmpty() || Ip.startsWith("192.168.") || Ip.startsWith("10.") || Ip.startsWith("172.") || Ip.startsWith("127.") || Ip.startsWith("0.") || Ip.startsWith("0:")
    }

    private fun getMd5(localFile: File): String {
        var fileStream = FileInputStream(localFile);
        try {
            return Md5Util.getFileMD5(fileStream);
        } catch (e: Exception) {
            return "";
        } finally {
            fileStream.close();
        }
    }

    /**
     * 生成最大数的随机数
     */
    fun getRandomWithMaxValue(max: Int): Int {
        return Math.abs(random.nextInt() % max);
    }

    /**
     * 生成指定长度的随机数
     */
    fun getRandomWithLength(length: Int): String {
        var ret = Math.abs(random.nextInt()).toString();
        while (true) {
            if (ret.length >= length) {
                break;
            }
            ret += Math.abs(random.nextInt()).toString();
        }
        ret = ret.slice(0..length - 1);
        return ret;
    }
//
//    /**
//     * @param days: 从1开始的天数。（mongo dayOfYear 返回的天数）
//     */
//    fun getDate(year: Int, days: Int): Date {
//        val format = java.text.SimpleDateFormat("yyyy-MM-dd")
//
//        var currentYear0 = format.parse("${year}-01-01")
//        return Date(currentYear0.time + (days - 1) * OneDayMilliseconds);
//    }
//
//    //判断是否是北京时间的同一天。
//    fun isSameBjDay(date1: Date, date2: Date): Boolean {
//        return (date1.time + 8 * OneHourMilliseconds) / OneDayMilliseconds == (date2.time + 8 * OneHourMilliseconds) / OneDayMilliseconds;
//    }

//----------------------

    inline fun <reified T> checkEmpty(obj: T, mapDefine: HashMap<String, String>): String {
        var type = T::class.java;
        for (key in mapDefine.keys) {
            if (key.contains('.') == false) {
                var m = type.getMethod("get" + key[0].toUpperCase() + key.substring(1));
                if (m == null) {
                    continue;
                }

                var v = m.invoke(obj);
                if (v == null || v.toString().isEmpty()) {
                    return mapDefine[key]!!
                }
                continue;
            }
        }
        return "";
    }

    /**
     * 判断是否是 Jar包启动。
     */
    fun isJarStarting(): Boolean {
        return Thread.currentThread().contextClassLoader.getResource("/") != null;
    }

    /**
     * 获取启动Jar所的路径
     * 调试时，会返回 target/classes/nbcp/base/utils
     * @param clazz: 启动Jar所任意类
     */
    fun getStartingJarFile(): File {
        /**
        file:/opt/edu_report/admin-api-1.0.1.jar!/BOOT-INF/classes!/
        /D:/code/edu_report/server/admin/target/classes/
         */
        /**
         * 还有一种办法获取 file
         * var file = Thread.currentThread().contextClassLoader.getResource("/").path
         */
//        var file = clazz.protectionDomain.codeSource.location.path
        var jarFile = Thread.currentThread().contextClassLoader.getResource("/")
                ?: Thread.currentThread().contextClassLoader.getResource("")

        var file = jarFile.path
        print(file)
        var startIndex = 0
        if (file.startsWith("//file:/")) {
            startIndex = 7
        } else if (file.startsWith("file:/")) {
            startIndex = 5
        }

        //如果是Jar包
        var index = file.indexOf("!/BOOT-INF/classes!/");
        if (index > 0) {
            return File(file.slice(startIndex..(index - 1)))
        } else {
            //如果是调试模式
            index = file.indexOf("/target/classes/")
            if (index > 0) {
                var mvn_file = File(file.Slice(0, -8)).listFiles { it -> it.name == "maven-archiver" }.firstOrNull()?.listFiles { it -> it.name == "pom.properties" }?.firstOrNull()
                if (mvn_file != null) {
                    var jarFile_lines = mvn_file.readLines()
                    var version = jarFile_lines.first { it.startsWith("version=") }.split("=").last()
                    var artifactId = jarFile_lines.first { it.startsWith("artifactId=") }.split("=").last()

                    return File(file.Slice(0, -8) + artifactId + "-" + version + ".jar")
                }
            }
        }

        return File(file)
    }

    fun getPrivatePropertyValue(entity: Any, property: String): Any? {
        var type = entity::class.java.FindField(property);
        if (type != null) {
            type.isAccessible = true;
            return type.get(entity);
        }
        return null;
    }

    fun setPrivatePropertyValue(entity: Any, property: String, value: Any?) {
        var type = entity::class.java.FindField(property);
        if (type == null) {
            return;
        }
        type.isAccessible = true;
        type.set(entity, value)
    }

    fun setPropertyValue(entity: Any, property: String, value: Any?) {
        var type = entity::class.java.AllFields.firstOrNull { it.name == property };
        if (type == null) {
            return;
        }
//        type.isAccessible = true;
        type.set(entity, value)
    }

    /**
     * 通过反射把 源对象值传输到目标对象
     */
    fun transportValueWithReflect(src: Any, target: Any) {
        var srcFields = src::class.java.AllFields;
        target::class.java.AllFields.forEach { targetField ->
            var srcField = srcFields.firstOrNull { it.name == targetField.name }
            if (srcField == null) {
                return@forEach
            }
            targetField.set(target, srcField.get(src))
        }
    }

    //简单类型，请参考 Class.IsSimpleType
    fun <T> getSimpleClassDefaultValue(clazz: Class<T>): Any? {

        var className = clazz.name;
        if (clazz == Boolean::class.java || className == "java.lang.Boolean") {
            return false;
        }
        if (clazz == Character::class.java || clazz == Char::class.java) {
            return '\u0000';
        }

        if (clazz == Byte::class.java || className == "java.lang.Byte") {
            return 0;
        }
        if (clazz == Short::class.java || className == "java.lang.Short") {
            return 0;
        }
        if (clazz == Integer::class.java || clazz == Int::class.java) {
            return 0;
        }
        if (clazz == Long::class.java || className == "java.lang.Long") {
            return 0L;
        }
        if (clazz == Float::class.java || className == "java.lang.Float") {
            return 0F;
        }
        if (clazz == Double::class.java || className == "java.lang.Double") {
            return 0.0;
        }

        //不应该执行这句.
        if (clazz.isPrimitive) {
            return null;
        } else if (clazz.isEnum) {
            return clazz.declaredFields.first().get(null);
        } else if (clazz == String::class.java) {
            return "";
        } else if (clazz == java.time.LocalDate::class.java) {
            return LocalDate.MIN
        } else if (clazz == java.time.LocalTime::class.java) {
            return LocalTime.MIN
        } else if (clazz == java.time.LocalDateTime::class.java) {
            return LocalDateTime.MIN
        } else if (clazz == java.util.Date::class.java) {
            return Date(0)
        }
//        else if (clazz == org.bson.types.ObjectId::class.java) {
//            return ObjectId(0, 0, 0, 0)
//        }

        return null;
    }


    fun getLoadedClasses(): Vector<Class<*>> = MyUtil.getPrivatePropertyValue(Thread.currentThread().contextClassLoader, "classes") as Vector<Class<*>>

    fun findClasses(basePack: String, oneClass: Class<*>): List<Class<*>> {

        var basePackPath = basePack.replace(".", "/")
        var ret = mutableListOf<Class<*>>();

        //通过当前线程得到类加载器从而得到URL的枚举
        val urlEnumeration = Thread.currentThread().contextClassLoader.getResources(basePackPath)
        var jarPath = File(Thread.currentThread().contextClassLoader.getResource(oneClass.name.replace('.', '/') + ".class").path.Slice(0, 0 - oneClass.name.length - ".class".length)).path;

        while (urlEnumeration.hasMoreElements()) {
            val url = urlEnumeration.nextElement()//得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit
            val protocol = url.protocol//大概是jar
            if ("jar".equals(protocol, ignoreCase = true)) {
                ret.addAll(getClassesFromJar(url, basePack))
            } else if ("file".equals(protocol, ignoreCase = true)) {
                ret.addAll(getClassesFromFile(url.path, basePack, jarPath))
            }
        }

        return ret;
    }

    private fun getClassName(fullPath: String, basePack: String, jarPath: String): String {
        if (fullPath.endsWith(".class") == false) {
            return "";
        }

        if (fullPath.startsWith(jarPath) == false) {
            return ""
        }

        var path2 = fullPath.Slice(jarPath.length + 1).replace(File.separatorChar, '.');

        if (path2.contains(basePack) == false) {
            return ""
        }

        return path2.Slice(0, 0 - ".class".length)
    }

    private fun getClassesFromFile(fullPath: String, basePack: String, jarPath: String): List<Class<*>> {
        var list = mutableListOf<Class<*>>()
        var className = ""
        var file = File(fullPath)
        if (file.isFile) {
            className = getClassName(file.name, basePack, jarPath);
            if (className.HasValue) {
                list.add(Class.forName(className));
            }
            return list;
        } else {
            file.listFiles().forEach {
                if (it.isFile) {
                    className = getClassName(it.path, basePack, jarPath);
                    if (className.HasValue) {
                        list.add(Class.forName(className));
                    }
                } else {
                    list.addAll(getClassesFromFile(it.path, basePack, jarPath))
                }
            }
        }
        return list;
    }

    private fun getClassesFromJar(url: URL, basePack: String): List<Class<*>> {
        //转换为JarURLConnection
        val connection = url.openConnection() as JarURLConnection
        if (connection == null) {
            return listOf();
        }

        val jarFile = connection.getJarFile()
        if (jarFile == null) {
            return listOf();
        }


        var list = mutableListOf<Class<*>>()
        //得到该jar文件下面的类实体
        val jarEntryEnumeration = jarFile.entries()
        while (jarEntryEnumeration.hasMoreElements()) {
            val entry = jarEntryEnumeration.nextElement()
            val jarEntryName = entry.getName()

            //这里我们需要过滤不是class文件和不在basePack包名下的类
            if (jarEntryName.endsWith(".class") == false) {
                continue;
            }

            val className = jarEntryName.Slice(0, -".class".length).replace('/', '.', false);

            if (basePack.HasValue && className.startsWith(basePack) == false) {
                continue
            }

            val cls = Class.forName(className)

            list.add(cls);
        }

        return list;
    }


    /**
     * 大驼峰
     */
    fun getBigCamelCase(value: String): String {
        return value.split("-", "_", ".").map { it[0].toUpperCase() + it.Slice(1) }.joinToString("")
    }

    fun getSmallCamelCase(value: String): String {
        var ret = getBigCamelCase(value);
        return ret[0].toLowerCase() + value.Slice(1)
    }
}

