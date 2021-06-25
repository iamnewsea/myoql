package nbcp.utils

import nbcp.comm.FullName
import nbcp.comm.HasValue
import nbcp.comm.ListRecursionFiles
import nbcp.comm.Slice
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ConfigurationBuilder
import org.springframework.util.ClassUtils
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException
import java.net.JarURLConnection
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarFile
import java.util.ArrayList

import java.util.jar.JarEntry

import java.util.Enumeration


/**
 * windows下：
 * 启动Jar loader.path里Jar包是: file:/D:/nancal/yun/lowcode-api/portal/admin/target/lib/zip4j-2.6.4.jar ， protocol=file
 * Jar包里依赖的Jar包是: jar:file:/D:/nancal/yun/lowcode-api/portal/admin/target/lowcode-admin-api-1.0.1.jar!/BOOT-INF/lib/shop-entity-1.0.1.jar!/  , protocol=jar
 */
object ClassUtil {

    /**
     * 获取 AppClassLoader 。
     */
    private fun getLaunchedURLClassLoader(loader: ClassLoader? = null): ClassLoader? {
        var loader = loader ?: ClassUtils.getDefaultClassLoader()
        if (loader == null) return null;

        if (loader::class.java.name == "org.springframework.boot.loader.LaunchedURLClassLoader") {
            return loader;
        }

        if (loader.parent == null) {
            return null;
        }
        return getLaunchedURLClassLoader(loader.parent);
    }

    private fun getAppClassLoader(loader: ClassLoader? = null): ClassLoader? {
        var loader = loader ?: ClassUtils.getDefaultClassLoader()
        if (loader == null) return null;

        if (loader::class.java.name == "sun.misc.Launcher\$AppClassLoader") {
            return loader;
        }

        if (loader.parent == null) {
            return null;
        }
        return getAppClassLoader(loader.parent);
    }

    fun getDefaultClassLoader(): ClassLoader? {
        return getLaunchedURLClassLoader() ?: getAppClassLoader()
    }

//    /**
//     * 加载的类,加载 AppClassLoader 下的类。
//     */
//    private fun getLoadedClasses(
//        fileCallback: ((String) -> Boolean)? = null
//    ): List<String> {
//        var list = mutableListOf<String>()
//
//        var loader = getDefaultClassLoader() as URLClassLoader
//        var classes = (MyUtil.getPrivatePropertyValue(loader, "classes") as List<Class<*>>?)?.map { it.name }
//
//        if (classes != null) {
//            list.addAll(classes.filter {
//                if (!(fileCallback?.invoke(it) ?: true)) {
//                    return@filter false;
//                }
//                return@filter true;
//            })
//
//        } else {
//            loader.urLs
//                .filter { it.protocol == "jar" }
//                .forEach { url ->
//                    var jarFile = MyUtil.getPrivatePropertyValue(url, "handler", "jarFile") as JarFile?
//                    if (jarFile == null) {
//                        //url.path= file:/D:/nancal/yun/lowcode-api/portal/admin/target/lowcode-admin-api-1.0.1.jar!/BOOT-INF/classes!/
//                        var path = url.path;
//                        if (path.startsWith("file:")) {
//                            path = path.substring(5);
//                        } else {
//                            throw RuntimeException("期待以 file: 开头 ${path}")
//                        }
//
//                        var index1 = path.indexOf(".jar!/");
//                        if (index1 > 0) {
//                            path = path.substring(0, index1);
//                        }
//
//                        jarFile = JarFile(path);
//                    }
//
//                    //nbcp/mvc/admin/CorpUserController$list$$inlined$apply$lambda$1.class
//                    //nbcp/mvc/admin/CorpUserController.class
//                    var list2 = jarFile.entries().toList()
//                        .filter { it.name.contains("$") == false && it.name.endsWith(".class") }
//                        .map { it.name.take(it.name.length - 6).replace('/', '.') }
//
//                    // /C:/Users/zhang/.m2/repository/com/alibaba/nacos/nacos-api/1.4.1/nacos-api-1.4.1.jar
////            if (path.endsWith(".jar", true)) {
////                list2 = JarFile(path).entries()
////                    .toList()
////                    .filter { it.name.endsWith(".class") }
////                    .map { it.name.take(it.name.length - 6).replace('/', '.') }
////
////            } else {
////                var file = File(path);
////                var file_len = file.FullName.length;
////                // /D:/nancal/yun/lowcode-api/portal/admin/target/classes/
////                list2 = file.ListRecursionFiles()
////                    .filter { it.endsWith(".class") }
////                    .map { it.substring(file_len + 1, it.length - 6).replace(File.separatorChar, '.') }
////            }
//
//
//                    list2 = list2.filter {
//                        if (!(fileCallback?.invoke(it) ?: true)) {
//                            return@filter false;
//                        }
//
//                        return@filter true;
//                    }
//
//                    list.addAll(list2);
//                }
//        }
//        return list;
//    }


    fun getClasses(basePackage: String): Set<String> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner(false))
        ).allTypes
    }

    fun getClasses(basePackage: String, baseType: Class<*>): Set<Class<*>> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner())
        ).getSubTypesOf(baseType)
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

}