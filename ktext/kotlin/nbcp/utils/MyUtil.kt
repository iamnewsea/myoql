package nbcp.utils

import nbcp.comm.*
import sun.misc.Launcher
import java.io.File
import java.io.Serializable
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.net.JarURLConnection
import java.net.URL
import java.time.*
import java.util.*


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
    val SystemTimeZoneOffsetTotalSeconds =
        ZoneId.systemDefault().rules.getStandardOffset(Date().toInstant()).totalSeconds  //系统时区相差的秒数

    @JvmStatic
    private val random = Random();

    //    /**
//     * 北京时间的今天凌晨。
//     */
    val today: LocalDate
        get() {
            return LocalDate.now();
        }


    /**
     * 通过 path 获取 value,每级返回的值必须是 Map<String,V> 否则返回 null
     * @param keys: 可以传多个key，也可以使用 . 分隔；如果查询数组，使用 products[],products[0], products.[] 或 products.[0] 或 "products","[]"
     */
    fun getPathValue(data: Any, vararg keys: String): Any? {
        if (keys.any() == false) return null;
        var key = keys.first();
        var left_keys = keys.Slice(1);

        if (key.isEmpty()) {
            throw RuntimeException("${keys}中包含空值")
        }
        var keys2 = key.split(".");
        if (keys2.size > 1) {
            var v = getPathValue(data, *keys2.toTypedArray());
            if (v == null) {
                return null;
            }

            if (left_keys.any() == false) {
                return v;
            }

            return getPathValue(v, *left_keys.toTypedArray())
        }


        if (key.endsWith("]")) {
            if (key != "[]" && key.endsWith("[]")) {
                return getPathValue(data, key.Slice(0, -2), "[]");
            }
            var start_index = key.lastIndexOf('[');
            if (start_index > 0) {
                return getPathValue(data, key.slice(0 until start_index), key.Slice(start_index))
            }
        }

        if (data is Map<*, *>) {
            var v = data.get(key)
            if (v == null) return null;
            if (left_keys.any() == false) return v;

            return getPathValue(v, *left_keys.toTypedArray())
        } else if (key == "[]") {
            var data2: List<*>
            if (data is Array<*>) {
                data2 = data.filter { it != null }
            } else if (data is Collection<*>) {
                data2 = data.filter { it != null }
            } else {
                throw RuntimeException("数据类型不匹配,${keys} 中 ${key} 需要是数组类型")
            }

            if (left_keys.any() == false) return data2;

            return data2
                .map { MyUtil.getPathValue(it!!, *left_keys.toTypedArray()) }
                .filter { it != null }

        } else if (key.startsWith("[") && key.endsWith("]")) {
            var index = key.substring(1, key.length - 1).AsInt(-1)
            if (index < 0) {
                throw RuntimeException("索引值错误:${key}")
            }

            var data2: Any? = null
            if (data is Array<*>) {
                data2 = data.get(index)
            } else if (data is Collection<*>) {
                data2 = data.elementAt(index)
            } else {
                throw RuntimeException("数据类型不匹配,${keys} 中 ${key} 需要是数组类型")
            }

            if (data2 == null) {
                return null;
            }

            if (left_keys.any() == false) return data2;

            return MyUtil.getPathValue(data2, *left_keys.toTypedArray())
        }

        //如果是对象

        var v = getPrivatePropertyValue(data, key)
        if (v == null) return null;
        if (left_keys.any() == false) {
            return v;
        }

        return getPathValue(v, *left_keys.toTypedArray())
    }

    /**
     * 获取正在执行的方法信息
     */
    fun getCurrentMethodInfo(): StackTraceElement {
        return Thread.currentThread().getStackTrace()[2]!!
    }

    fun getCenterEachLine(lines: List<String>): List<String> {
        var map = lines.map { it to it.sumBy { if (it.toLong() < 256) 1 else 2 } }

        var max = map.map { it.second }.maxOrNull() ?: 0
        if (max % 2 == 1) {
            max++;
        }

        return map.map {
            if (it.second == 0) return@map it.first

            var harf = ((max - it.second) / 2).AsInt()
            var part = ' '.NewString(harf)
            return@map part + it.first
        }
    }

//    /**
//     * 按大写字母拆分
//     */
//    fun splitWith(value: String,splitCallback:((Char)->Boolean)): List<String> {
//        var ret = mutableListOf<String>()
//
//        var prevIndex = 0;
//        for (index in 1 until value.length) {
//            var item = value[index];
//
//            if (item.isUpperCase()) {
//                ret.add(value.substring(prevIndex, index))
//                prevIndex = index;
//            }
//        }
//
//        ret.add(value.substring(prevIndex));
//
//        return ret.filter { it.HasValue };
//    }


    /**
     * https://www.w3school.com.cn/media/media_mimeref.asp
     */
    private val mimeLists = StringMap(
        "" to "application/octet-stream",
        "css" to "text/css",
        "htm" to "text/html",
        "html" to "text/html",
        "js" to "application/javascript",
        "xml" to "text/xml",
        "gif" to "image/gif",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "png" to "image/jpeg",
        "tiff" to "image/tiff",
        "json" to "application/json",
        "txt" to "text/plain",
        "mp3" to "audio/mpeg",
        "avi" to "video/x-msvideo",
        "mp4" to "video/mpeg4",
        "doc" to "application/msword",
        "docx" to "application/msword",
        "pdf" to "application/pdf",
        "xls" to "application/vnd.ms-excel",
        "xlsx" to "application/vnd.ms-excel",
        "ppt" to "application/vnd.ms-powerpoint",
        "exe" to "application/octet-stream",
        "zip" to "application/zip",
        "m3u" to "audio/x-mpegurl",
        "svg" to "image/svg+xml",
        "h" to "text/plain",
        "c" to "text/plain",
        "dll" to "application/x-msdownload"
    )

    fun getMimeType(extName: String): String {
        return mimeLists.getStringValue(extName.toLowerCase()).AsString()
    }


    /**
     * 最好传前8个字节，判断文件类型。
     */
    fun getFileTypeWithBom(byteArray8: ByteArray): String {
        //https://blog.csdn.net/hch15112345824/article/details/86640092
        //https://blog.csdn.net/gagapencil/article/details/40392363

        //小于4个字节，返回空。
        if (byteArray8.size < 4) {
            return "";
        }

        var value = byteArray8.ToHexLowerString()
        var map = mapOf(
            "4D546864" to "mid",
            "FFD8FF" to "jpg",
            "89504E47" to "png",
            "47494638" to "gif",
            "49492A00" to "tif",
            "424D" to "bmp",
            "41433130" to "dwg",
            "38425053" to "psd",
            "7B5C727466" to "rtf",
            "3C3F786D6C" to "xml",
            "68746D6C3E" to "html",
//                "44656C69766572792D646174653A" to "eml",
//                "CFAD12FEC5FD746F" to "dbx",
//                "2142444E" to "pst",
            "D0CF11E0" to "doc",
//                "5374616E64617264204A" to "mdb",
            "FF575043" to "wpd",
//                "252150532D41646F6265" to "ps",
            "255044462D312E" to "pdf",
            "AC9EBD8F" to "qdf",
            "E3828596" to "pwl",
            "504B0304" to "zip",
            "52617221" to "rar",
            "57415645" to "wav",
            "41564920" to "avi",
            "2E7261FD" to "ram",
            "2E524D46" to "rm",
            "000001BA" to "mpg",
            "000001B3" to "mpg",
            "6D6F6F76" to "mov",
            "3026B2758E66CF11" to "asf"
        );

        return map.filterKeys { value.startsWith(it) }.values.firstOrNull() ?: ""
    }

    fun isLocalIp(Ip: String): Boolean {
        return Ip.isEmpty() || Ip.startsWith("192.168.") || Ip.startsWith("10.") || Ip.startsWith("172.") || Ip.startsWith(
            "127."
        ) || Ip.startsWith("0.") || Ip.startsWith("0:")
    }

    /**
     * 生成大于等于0，小于指定最大值的随机数,即 [0,max)
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

    /**
     * 获取 Base64
     */
    fun getBase64(target: String): String {
        return Base64.getEncoder().encodeToString(target.toByteArray(utf8));
    }

    fun getBase64(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes);
    }

    fun getFromBase64(base64: String): ByteArray {
        return Base64.getDecoder().decode(base64);
    }

    fun getStringContentFromBase64(base64: String): String {
        return String(getFromBase64(base64), utf8)
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

    fun getStartingJarFile(url: URL): File {
        var path = JsUtil.decodeURIComponent(url.path)
        if (url.protocol == "jar") {
            //值是： file:/D:/code/sites/server/admin/target/admin-api-1.0.1.jar!/BOOT-INF/classes!/
            var index = 0;
            if (path.startsWith("file:/")) {
                index = "file:/".length;
            }
            return File(path.Slice(index - 1, 0 - "!/BOOT-INF/classes!/".length))
        } else if (url.protocol == "file") {
            //值是： /D:/code/sites/server/admin/target/classes/
            //处理文件路径中中文的问题。
            var targetPath = File(path).parentFile
            var mvn_file = targetPath.listFiles { it -> it.name == "maven-archiver" }.firstOrNull()
                ?.listFiles { it -> it.name == "pom.properties" }?.firstOrNull()
            if (mvn_file != null) {
                var jarFile_lines = mvn_file.readLines()
                var version = jarFile_lines.first { it.startsWith("version=") }.split("=").last()
                var artifactId = jarFile_lines.first { it.startsWith("artifactId=") }.split("=").last()

                return File(targetPath.FullName + "/" + artifactId + "-" + version + ".jar")
            } else {
                throw RuntimeException("找不到 maven-archiver , 先打包再运行!")
            }
        }
        throw RuntimeException("不识别的协议类型 ${url.protocol}")
    }

    /**
     * 获取启动Jar所的路径
     * 调试时，会返回 target/classes/nbcp/base/utils
     */
    fun getStartingJarFile(): File {
//        val stackTraceElements = RuntimeException().stackTrace
//        for (stackTraceElement in stackTraceElements) {
//            if ("main" == stackTraceElement.methodName) {
//                return stackTraceElement.className
//            }
//        }
        /**
        file:/opt/edu_report/admin-api-1.0.1.jar!/BOOT-INF/classes!/
        /D:/code/edu_report/server/admin/target/classes/
         */
        /**
         * 还有一种办法获取 file
         * var file = Thread.currentThread().contextClassLoader.getResource("/").path
         */
//        var file = clazz.protectionDomain.codeSource.location.path
        var classLoader = Thread.currentThread().contextClassLoader

        /**
         * jar -Dloader.path=libs 方式:
         * 1. 使用 /, 表示启动的Jar包 !/BOOT-INF/classes!/
         * 2. 使用 ./ 或 空串 表示 libs 目录
         *
         * jar 方式：
         * 1. 使用 /, 表示启动的Jar包 !/BOOT-INF/classes!/
         * 2. 使用 ./ 或 空串 表示Jar包
         *
         * 调试时：
         * 1. 使用 / 返回 null
         * 2. 使用 ./ 或 空串 ,返回 /D:/code/sites/server/admin/target/classes/
         */
        var url = classLoader.getResource("/") ?: classLoader.getResource("")
        return getStartingJarFile(url)
    }

    fun getPrivatePropertyValue(entity: Any, type: Field): Any? {
        type.isAccessible = true;
        return type.get(entity);
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

//    fun setPropertyValue(entity: Any, property: String, value: Any?) {
//        var type = entity::class.java.AllFields.firstOrNull { it.name == property };
//        if (type == null) {
//            return;
//        }
////        type.isAccessible = true;
//        type.set(entity, value)
//    }

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
        if (clazz == Boolean::class.java || clazz == java.lang.Boolean::class.java) {
            return false;
        }
        if (clazz == java.lang.Character::class.java || clazz == Char::class.java) {
            return '\u0000';
        }

        if (clazz == Byte::class.java || clazz == java.lang.Byte::class.java) {
            return 0;
        }
        if (clazz == Short::class.java || clazz == java.lang.Short::class.java) {
            return 0;
        }
        if (clazz == java.lang.Integer::class.java || clazz == Int::class.java) {
            return 0;
        }
        if (clazz == Long::class.java || clazz == java.lang.Long::class.java) {
            return 0L;
        }
        if (clazz == Float::class.java || clazz == java.lang.Float::class.java) {
            return 0F;
        }
        if (clazz == Double::class.java || clazz == java.lang.Double::class.java) {
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


    /**
     * 获取 AppClassLoader 。
     */
    fun getAppClassLoader(loader: ClassLoader? = null): ClassLoader? {
        var loader = loader ?: Thread.currentThread().contextClassLoader
        if (loader == null) return null;

        if (loader::class.java.name == "sun.misc.Launcher\$AppClassLoader") {
            return loader;
        }

        return getAppClassLoader(loader.parent);
    }

    /**
     * 加载的类,加载 AppClassLoader 下的类。
     */
    fun getLoadedClasses(): List<Class<*>> =
        (MyUtil.getPrivatePropertyValue(getAppClassLoader()!!, "classes") as Vector<Class<*>>).toList()

    /**
     * 查找类。
     */
    fun findClasses(basePack: String, oneClass: Class<*>, filter: ((Class<*>) -> Boolean)? = null): List<Class<*>> {

        var basePackPath = basePack.replace(".", "/")
        var ret = mutableListOf<Class<*>>();

        //通过当前线程得到类加载器从而得到URL的枚举
        var classLeader = Thread.currentThread().contextClassLoader
        val urlEnumeration = classLeader.getResources(basePackPath)
        var jarPath = File(
            classLeader.getResource(oneClass.name.replace('.', '/') + ".class").path.Slice(
                0,
                0 - oneClass.name.length - ".class".length
            )
        ).path;

        while (urlEnumeration.hasMoreElements()) {
            val url =
                urlEnumeration.nextElement()//得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit
            val protocol = url.protocol//大概是jar
            if ("jar".equals(protocol, ignoreCase = true)) {
                ret.addAll(getClassesFromJar(url, basePack, filter))
            } else if ("file".equals(protocol, ignoreCase = true)) {
                ret.addAll(getClassesFromFile(url.path, basePack, jarPath, filter))
            }
        }

        return ret;
    }

    /**
     * 列出下一级的资源文件
     * 可以从 SpringApplication.classLoader.getResource("") 中获取,也可以从 Thread.currentThread().contextClassLoader.getResource("") 中获取
     * @param fileFilter: 参数为资源路径，统一格式：分隔符为"/",开头不包括"/"，如果结尾为"/"表示目录
     */
    fun listResourceFiles(fileFilter: ((String) -> Boolean)? = null): Set<String> {
        var classLoader = Thread.currentThread().contextClassLoader
        var url = classLoader.getResource("/") ?: classLoader.getResource("")
        var list = mutableSetOf<String>()

        //jar方式使用 /，空字符串都可以。 调试时只能使用 字符串。
        if (url.protocol == "jar") {
            var conn = url.openConnection() as JarURLConnection
            var ents = conn.jarFile.entries();
            while (ents.hasMoreElements()) {
                var item = ents.nextElement();
                var name = item.name;

                if (fileFilter != null && fileFilter(name) == false) {
                    continue;
                }


                list.add(name);
            }
        } else if (url.protocol == "file") {
            var path = File(url.path).FullName + File.separator

            list = list_files_recursion(File(path), path, fileFilter)
        }

        return list;
    }

    private fun list_files_recursion(
        path: File,
        base: String,
        fileFilter: ((String) -> Boolean)? = null
    ): MutableSet<String> {
        var list = mutableSetOf<String>()
        path.listFiles { it ->
            var relativeName = it.FullName.substring(base.length).replace("\\", "/");
            if (it.isFile) {
                if (fileFilter?.invoke(relativeName) ?: true) {
                    list.add(relativeName);
                }
            } else {
                fileFilter?.invoke(relativeName)
                list.addAll(list_files_recursion(it, base, fileFilter))
            }
            return@listFiles true;
        }
        return list;
    }

    private fun getClassName(fullPath: String, basePack: String, jarPath: String): String {
        if (fullPath.endsWith(".class") == false) {
            return "";
        }

        if (fullPath.startsWith(jarPath) == false) {
            return ""
        }

        var path2 = fullPath.substring(jarPath.length + 1).replace(File.separatorChar, '.');

        if (path2.contains(basePack) == false) {
            return ""
        }

        return path2.Slice(0, 0 - ".class".length)
    }

    private fun getClassesFromFile(
        fullPath: String,
        basePack: String,
        jarPath: String,
        filter: ((Class<*>) -> Boolean)? = null
    ): List<Class<*>> {
        var list = mutableListOf<Class<*>>()
        var className = ""
        var file = File(fullPath)
        if (file.isFile) {
            className = getClassName(file.name, basePack, jarPath);
            if (className.HasValue) {
                var cls = Class.forName(className)
                if (filter?.invoke(cls) ?: true) {
                    list.add(cls);
                }
            }
            return list;
        } else {
            file.listFiles().forEach {
                if (it.isFile) {
                    className = getClassName(it.path, basePack, jarPath);
                    if (className.HasValue) {
                        var cls = Class.forName(className);
                        if (filter?.invoke(cls) ?: true) {
                            list.add(cls);
                        }
                    }
                } else {
                    list.addAll(getClassesFromFile(it.path, basePack, jarPath, filter))
                }
            }
        }
        return list;
    }

    private fun getClassesFromJar(url: URL, basePack: String, filter: ((Class<*>) -> Boolean)? = null): List<Class<*>> {
        //转换为JarURLConnection
        val connection = url.openConnection() as JarURLConnection
        if (connection == null) {
            return listOf();
        }

        val jarFile = connection.jarFile
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

            if (filter?.invoke(cls) ?: true) {
                list.add(cls);
            }
        }

        return list;
    }


    /**
     * 大驼峰 ,仅保留字母，数字
     */
    fun getBigCamelCase(value: String): String {
        return value.split(Regex("""[\W_]+""")).map { it[0].toUpperCase() + it.substring(1) }.joinToString("")
    }

    /**
     * 小驼峰
     */
    fun getSmallCamelCase(value: String): String {
        var ret = getBigCamelCase(value);
        return ret[0].toLowerCase() + ret.substring(1)
    }

    /**
     * 短横线格式，全小写
     */
    fun getKebabCase(value: String): String {
        return getSmallCamelCase(value).replace(Regex("[A-Z]"), {
            return@replace "-" + it.value.toLowerCase()
        })
    }

    fun trimStart(value: String, trimPart: String, ignoreCase: Boolean = false): String {
        if (value.startsWith(trimPart, ignoreCase) == false) {
            return value;
        }
        return trimStart(value.substring(trimPart.length), trimPart, ignoreCase)
    }

    fun trimEnd(value: String, trimPart: String, ignoreCase: Boolean = false): String {
        if (value.endsWith(trimPart, ignoreCase) == false) {
            return value;
        }
        return trimEnd(value.substring(0, value.length - trimPart.length), trimPart, ignoreCase)
    }

    fun trim(value: String, trimPart: String, ignoreCase: Boolean = false): String {
        return trimEnd(trimStart(value, trimPart, ignoreCase), trimPart, ignoreCase)
    }

    /**
     * 格式化模板
     */
    fun formatTemplateJson(
        /**
         * 如 dbr.${group|w}
         */
        text: String,
        /**
         * 如: {group:"abc"}
         */
        json: StringMap,
        /**
         * 不是默认函数的时候,调用 funcCallback 自定义处理.
         * 第一个参数是key, 第二个是value, 第三个参数是函数名,第四个参数是函数参数 , 返回新值
         * 如果模板使用了函数,而没有传递,抛出异常.
         * 如: ${id|type} ,type(id) 不是默认定义,需要通过 funcCallback 传
         */
        funcCallback: ((String, String?, String, String) -> String?)? = null,
        style: String = "\${}"
    ): String {

        var map: StringKeyMap<((String) -> String)> = StringKeyMap()
        map.put("-", { MyUtil.getKebabCase(it) })
        map.put("W", { MyUtil.getBigCamelCase(it) })
        map.put("w", { MyUtil.getSmallCamelCase(it) })
        map.put("U", { it.toUpperCase() })
        map.put("u", { it.toLowerCase() })

        var map2: StringKeyMap<((String).(String) -> String)> = StringKeyMap()
        map2.put("trim", { MyUtil.trim(this, it) })


        return text.formatWithJson(
            json, style,
            { key ->
                key.split("|").first()
            },
            { fullKey, value ->
                // fullKey 即 ${fullKey} == group|w
                // 如果 json中定义了值,使用json的.如: json == {"group|w": "大写第二个字母值"}
                // value是group的原始值 == "wx"
                var sects = fullKey.split("|")
                if (sects.size <= 1) {
                    return@formatWithJson value;
                }

                // key == group
                var key = sects.first();
                var result: String? = value
                sects.Skip(1).forEach { funcString ->
                    //如：   substring:2,3
                    var funcContent = funcString.split(":")
                    var funcName = funcContent.first();
                    var params = listOf<String>()
                    if (funcContent.size > 2) {
                        throw RuntimeException("表达式中多个冒号非法：${funcString}")
                    } else if (funcContent.size == 2) {
                        // 如：   substring:2,3 中的 2,2 参数部分
                        params = funcContent[1].split(",")
                    }


                    if (params.size == 1) {
                        var param = params[0];
                        var paramValue = param;
                        if (param.startsWith("'") && param.endsWith("'")) {
                            paramValue = param.substring(1, param.length - 1);
                        } else if (param.IsNumberic()) {
                            paramValue = param;
                        } else {
                            paramValue = json.get(param).AsString()
                        }

                        //如果定义了默认的funcName
                        if (value != null && map2.containsKey(funcName)) {
                            var funcBody = map2.get(funcName)!!
                            result = funcBody.invoke(value, paramValue)
                        } else if (funcCallback != null) {
                            result = funcCallback.invoke(key, value, funcName, paramValue)
                        } else {
                            throw RuntimeException("找不到 ${funcName}")
                        }
                    } else if (params.size == 0) {
                        if (value != null && map.containsKey(funcName)) {
                            var funcBody = map.get(funcName)!!
                            result = funcBody.invoke(value)
                        } else if (funcCallback != null) {
                            result = funcCallback.invoke(key, value, funcName, "")
                        } else {
                            throw RuntimeException("找不到 ${funcName}")
                        }

                    }

                    return@forEach
                }

                if (result == null) {
                    throw RuntimeException("无法处理 ${fullKey}")
                }

                return@formatWithJson result!!
            });
    }
}

