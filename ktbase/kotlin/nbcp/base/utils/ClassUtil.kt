package nbcp.base.utils

import nbcp.base.comm.JsonMap
import nbcp.base.extend.*
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.springframework.core.io.ClassPathResource
import org.springframework.util.ClassUtils
import java.io.File
import java.lang.reflect.Field
import java.net.JarURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.reflect.KClass


/**
 * windows下：
 * 启动Jar loader.path里Jar包是: file:/D:/nancal/yun/lowcode-api/portal/admin/target/lib/zip4j-2.6.4.jar ， protocol=file
 * Jar包里依赖的Jar包是: jar:file:/D:/nancal/yun/lowcode-api/portal/admin/target/lowcode-admin-api-1.0.1.jar!/BOOT-INF/lib/shop-entity-1.0.1.jar!/  , protocol=jar
 */
object ClassUtil {


    /**
     * 是否存在类
     */
    @JvmStatic
    fun existsClass(className: String): Boolean {
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

    @JvmStatic
    fun getDefaultClassLoader(): ClassLoader {
        return ClassUtils.getDefaultClassLoader()
    }

    @JvmStatic
    fun getClasses(basePackage: String): Set<String> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner(false))
        ).allTypes
    }

    @JvmStatic
    fun getClassesWithBaseType(basePackage: String, baseType: Class<*>): Set<Class<*>> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner())
        ).getSubTypesOf(baseType)
    }

    @JvmStatic
    fun getClassesWithAnnotationType(basePackage: String, annotationType: Class<out Annotation>): Set<Class<*>> {
        return Reflections(
            ConfigurationBuilder()
                .forPackages(basePackage)
                .setScanners(SubTypesScanner(false), TypeAnnotationsScanner())
        ).getTypesAnnotatedWith(annotationType)
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
    @JvmStatic
    fun classIsInJar(type: Class<*>, jarFileName: String): Boolean {
        var sects = type.protectionDomain.codeSource.location.path.split("/")
        return sects.contains(jarFileName) && sects.last().contains(jarFileName)
    }


    /**
     * 查找类。
     */
    @JvmOverloads
    @JvmStatic
    fun findClasses(basePack: String, filter: ((String) -> Boolean)? = null): List<Class<*>> {
        val baseResourcePath = basePack.replace(".", "/").trim('/');
        val ret = mutableListOf<Class<*>>();
        val resource = ClassPathResource(baseResourcePath);
        if (resource.exists() == false) {
            return listOf();
        }
        val url =
            resource.url; //得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit

        ResourceUtil. findResources(url, baseResourcePath, { jarEntryName ->
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


    @JvmStatic
    fun <T : Any> getTypeDefaultValue(type: KClass<T>): Any? {
        return getTypeDefaultValue(type.java)
    }

    //简单类型，请参考 Class.IsSimpleType
    @JvmStatic
    fun <T> getTypeDefaultValue(type: Class<T>): Any? {

//        var className = clazz.name;
        if (type == Boolean::class.java || type == java.lang.Boolean::class.java) {
            return false;
        }
        if (type == java.lang.Character::class.java || type == Char::class.java) {
            return '\u0000';
        }

        if (type == Byte::class.java || type == java.lang.Byte::class.java) {
            return 0;
        }
        if (type == Short::class.java || type == java.lang.Short::class.java) {
            return 0;
        }
        if (type == java.lang.Integer::class.java || type == Int::class.java) {
            return 0;
        }
        if (type == Long::class.java || type == java.lang.Long::class.java) {
            return 0L;
        }
        if (type == Float::class.java || type == java.lang.Float::class.java) {
            return 0F;
        }
        if (type == Double::class.java || type == java.lang.Double::class.java) {
            return 0.0;
        }

        //不应该执行这句.
        if (type.isPrimitive) {
            return type.constructors.firstOrNull { it.parameters.size == 0 }?.newInstance()
        } else if (type.isEnum) {
            return type.declaredFields.first().get(null);
        } else if (type == String::class.java) {
            return "";
        } else if (type == java.time.LocalDate::class.java) {
            return LocalDate.MIN
        } else if (type == java.time.LocalTime::class.java) {
            return LocalTime.MIN
        } else if (type == java.time.LocalDateTime::class.java) {
            return LocalDateTime.MIN
        } else if (type == java.util.Date::class.java) {
            return Date(0)
        }
//        else if (clazz == org.bson.types.ObjectId::class.java) {
//            return ObjectId(0, 0, 0, 0)
//        }

        return type.constructors.firstOrNull { it.parameters.size == 0 }?.newInstance()
    }







}