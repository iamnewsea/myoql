package nbcp.utils

import nbcp.comm.*
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.springframework.util.ClassUtils
import java.io.File
import java.lang.RuntimeException
import java.net.JarURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.util.*


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
        var loaderValue = loader ?: ClassUtils.getDefaultClassLoader()
        if (loaderValue == null) return null;

        if (loaderValue::class.java.name == "sun.misc.Launcher\$AppClassLoader") {
            return loaderValue;
        }

        if (loaderValue.parent == null) {
            return null;
        }
        return getAppClassLoader(loaderValue.parent);
    }

    fun getDefaultClassLoader(): ClassLoader? {
        return getLaunchedURLClassLoader() ?: getAppClassLoader()
    }

    fun getClasses(basePackage: String): Set<String> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner(false))
        ).allTypes
    }

    fun getClassesWithBaseType(basePackage: String, baseType: Class<*>): Set<Class<*>> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner())
        ).getSubTypesOf(baseType)
    }

    fun getClassesWithAnnotationType(basePackage: String, annotationType: Class<out Annotation>): Set<Class<*>> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner(false), TypeAnnotationsScanner())
        ).getTypesAnnotatedWith(annotationType)
    }


    fun getMainApplicationLastModified(): LocalDateTime? {
        var list = ClasspathHelper.forResource("");
        if (list.any() == false) return null;

        var fileName = list.first().path;

        return Date(File(fileName).lastModified()).AsLocalDateTime()
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
            return File(path.Slice(index - 1, -"!/BOOT-INF/classes!/".length))
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


//    fun getApplicationMainClass(): Class<*>? {
//        RuntimeException().getStackTrace().reversed().firstOrNull {
//            if ("main" == it.methodName) {
//                return@firstOrNull true
//            }
//            return@firstOrNull false;
//        }?.className.apply {
//            if (this == null) return null;
//            return Class.forName(this)
//        }
//    }

    /**
     * 判断类是否是指定的Jar中
     */
    fun classIsInJar(clazz: Class<*>, jarFileName: String): Boolean {
        var sects = clazz.protectionDomain.codeSource.location.path.split("/")
        return sects.contains(jarFileName) && sects.last().contains(jarFileName)
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
                -oneClass.name.length - ".class".length
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

        return path2.Slice(0, -".class".length)
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
        val connection = url.openConnection() as JarURLConnection?
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