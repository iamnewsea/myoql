package nbcp.utils

import nbcp.comm.*
import nbcp.db.CodeName
import nbcp.db.CodeValue
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.util.ClassUtils
import sun.net.www.protocol.file.FileURLConnection
import java.io.File
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

    val startJarPackage: String by lazy {
        return@lazy Thread.currentThread().stackTrace.last().className.split(".").Slice(0, -1).joinToString(".")
    }

    /**
     * 是否存在类
     */
    @JvmStatic fun existsClass(className: String): Boolean {
        try {
            getDefaultClassLoader().loadClass(className);
            return true;
        } catch (e: ClassNotFoundException) {
            return false;
        }
    }

    /**
     * 获取 AppClassLoader 。
     */
//    private @JvmStatic fun getLaunchedURLClassLoader(loader: ClassLoader? = null): ClassLoader {
//        val loaderValue = loader ?: ClassUtils.getDefaultClassLoader()
//        if (loaderValue == null) {
//            throw RuntimeException("找不到默认的类加载器")
//        }
//
//        if (loaderValue::class.java.name == "org.springframework.boot.loader.LaunchedURLClassLoader") {
//            return loaderValue;
//        }
//
//        if (loaderValue.parent == null) {
//            return loaderValue;
//        }
//        return getLaunchedURLClassLoader(loaderValue.parent);
//    }
//
//    private @JvmStatic fun getAppClassLoader(loader: ClassLoader? = null): ClassLoader {
//        val loaderValue = loader ?: ClassUtils.getDefaultClassLoader()
//        if (loaderValue == null) {
//            throw RuntimeException("找不到默认的类加载器")
//        }
//
//        if (loaderValue::class.java.name == "sun.misc.Launcher\$AppClassLoader") {
//            return loaderValue;
//        }
//
//        if (loaderValue.parent == null) {
//            return loaderValue;
//        }
//        return getAppClassLoader(loaderValue.parent);
//    }

    @JvmStatic fun getDefaultClassLoader(): ClassLoader {
        return ClassUtils.getDefaultClassLoader()
    }

    @JvmStatic fun getClasses(basePackage: String): Set<String> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner(false))
        ).allTypes
    }

    @JvmStatic fun getClassesWithBaseType(basePackage: String, baseType: Class<*>): Set<Class<*>> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner())
        ).getSubTypesOf(baseType)
    }

    @JvmStatic fun getClassesWithAnnotationType(basePackage: String, annotationType: Class<out Annotation>): Set<Class<*>> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner(false), TypeAnnotationsScanner())
        ).getTypesAnnotatedWith(annotationType)
    }


    @JvmStatic fun getMainApplicationLastModified(): LocalDateTime? {
        val list = ClasspathHelper.forResource("");
        if (list.any() == false) return null;

        val fileName = list.first().path;

        return Date(File(fileName).lastModified()).AsLocalDateTime()
    }


    /**
     * 判断是否是 Jar包启动。
     */
    @JvmStatic fun isJarStarting(): Boolean {
        return Thread.currentThread().contextClassLoader.getResource("/") != null;
    }

    @JvmStatic fun getStartingJarFile(url: URL): File? {
        val path = JsUtil.decodeURIComponent(url.path)
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
            val targetPath = File(path).parentFile
            val mvn_file = targetPath?.listFiles { it -> it.name == "maven-archiver" }?.firstOrNull()
                ?.listFiles { it -> it.name == "pom.properties" }?.firstOrNull()
            if (mvn_file == null) {
                return null;
//                throw RuntimeException("找不到 maven-archiver , 先打包再运行!")
            }

            val jarFile_lines = mvn_file.readLines()
            val version = jarFile_lines.first { it.startsWith("version=") }.split("=").last()
            val artifactId = jarFile_lines.first { it.startsWith("artifactId=") }.split("=").last()

            return File(targetPath.FullName + "/" + artifactId + "-" + version + ".jar")
        }

        return null;
//        throw RuntimeException("不识别的协议类型 ${url.protocol}")
    }

    /**
     * 获取启动Jar所的路径
     * 调试时，会返回 target/classes/nbcp/base/utils
     */
    @JvmStatic fun getStartingJarFile(): File? {
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
        val classLoader = Thread.currentThread().contextClassLoader

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
        val url = classLoader.getResource("/") ?: classLoader.getResource("")
        return getStartingJarFile(url)
    }


//    @JvmStatic fun getApplicationMainClass(): Class<*>? {
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
    @JvmStatic fun classIsInJar(clazz: Class<*>, jarFileName: String): Boolean {
        var sects = clazz.protectionDomain.codeSource.location.path.split("/")
        return sects.contains(jarFileName) && sects.last().contains(jarFileName)
    }


    /**
     * 查找类。
     */
    @JvmOverloads
    @JvmStatic fun findClasses(basePack: String, filter: ((String) -> Boolean)? = null): List<Class<*>> {
        val baseResourcePath = basePack.replace(".", "/").trim('/');
        val ret = mutableListOf<Class<*>>();
        val resource = ClassPathResource(baseResourcePath);
        if (resource.exists() == false) {
            return listOf();
        }
        val url =
            resource.url; //得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit

        findResources(url, baseResourcePath, { jarEntryName ->
            //如果是死循环,则停止
            if (jarEntryName == basePack || jarEntryName == baseResourcePath) {
                return@findResources false
            }

            if (!jarEntryName.endsWith(".class")) {
                //如果是包名
                ret.addAll(findClasses(jarEntryName, filter))

                return@findResources false
            }

            val className = jarEntryName.Slice(0, -".class".length)
                .replace('/', '.')

            if (filter != null && !filter.invoke(className)) {
                return@findResources false;
            }

            val cls = Class.forName(className)
            ret.add(cls);
            return@findResources true
        })
        return ret;
    }

    /**
     * 判断是否存在资源
     */
    @JvmStatic fun existsResource(path: String): Boolean {
        return ClassPathResource(path).exists()
    }

    /**
     * @param basePath: 前后不带/
     */
    @JvmOverloads
    @JvmStatic fun findResources(basePath: String, filter: ((String) -> Boolean)? = null): List<String> {
        val resource = ClassPathResource(basePath);
        if (resource.exists() == false) return listOf();
        return findResources(resource.url, basePath.trim('/'), filter)
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

//    private @JvmStatic fun getResourceFromJar(
//            fullPath: String,
//            basePack: String,
//            jarPath: String,
//            filter: ((Class<*>) -> Boolean)? = null
//    ): List<Class<*>> {
//        var list = mutableListOf<Class<*>>()
//        var className = ""
//        var file = File(fullPath)
//        if (file.isFile) {
//            className = getClassName(file.name, basePack, jarPath);
//            if (className.HasValue) {
//                var cls = Class.forName(className)
//                if (filter?.invoke(cls) ?: true) {
//                    list.add(cls);
//                }
//            }
//            return list;
//        } else {
//            file.listFiles().forEach {
//                if (it.isFile) {
//                    className = getClassName(it.path, basePack, jarPath);
//                    if (className.HasValue) {
//                        var cls = Class.forName(className);
//                        if (filter?.invoke(cls) ?: true) {
//                            list.add(cls);
//                        }
//                    }
//                } else {
//                    list.addAll(getClassesFromFile(it.path, basePack, jarPath, filter))
//                }
//            }
//        }
//        return list;
//    }

    @JvmOverloads
    @JvmStatic fun findResources(url: URL, basePath: String, filter: ((String) -> Boolean)? = null): List<String> {
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
        } else if (connection is FileURLConnection) {
            var list = mutableListOf<String>()
            var base = url.file.split("/target/classes/")[1]

            url.openConnection().inputStream.readContentString()
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

        throw java.lang.RuntimeException("不识别的类型:${connection::class.java.name}!")
    }


}