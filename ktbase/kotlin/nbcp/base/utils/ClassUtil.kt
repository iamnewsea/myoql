package nbcp.base.utils

import nbcp.base.extend.*
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory
import org.springframework.util.ClassUtils
import java.io.File
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

    @JvmStatic
    fun getDefaultClassLoader(): ClassLoader {
        return ClassUtils.getDefaultClassLoader()
    }

    /**
     * @param resourcePattern 形如： *星/星 表示 所有类
     * @param onlyInStartApplication 表示是否仅在当前启动应用内搜索
     */
    @JvmStatic
    fun findClassNames(resourcePattern: String, onlyInStartApplication: Boolean = false, callback: ((String) -> Boolean)? = null): Set<String> {

        //如果这里写： classpath:**/*.class ，表示查找启动工程下的类。
        var locationPattern = resourcePattern.replace(".", "/")
        if (onlyInStartApplication) {
            locationPattern = "classpath:${locationPattern}" + "/**/*.class"
        } else {
            locationPattern = "classpath*:${locationPattern}" + "/**/*.class"
        }

        return findResources(locationPattern, callback)
    }

    @JvmStatic
    fun findResources(
            locationPattern: String,
            callback: ((String) -> Boolean)? = null): Set<String> {
        val resolver = PathMatchingResourcePatternResolver()
        val ret = mutableSetOf<String>();
        val resources = resolver.getResources(locationPattern)
        for (res in resources) {
            val clsName = SimpleMetadataReaderFactory().getMetadataReader(res).classMetadata.className

            if (callback?.invoke(clsName) ?: true) {
                ret.add(clsName)
            }
        }

        return ret;
    }

    @JvmStatic
    fun findClasses(resourcePattern: String,
                    onlyInStartApplication: Boolean = false,
                    baseClass: Class<*>? = null): Set<Class<*>> {

        return findClassNames(resourcePattern, onlyInStartApplication)
                .map { Class.forName(it) }
                .filter { baseClass?.isAssignableFrom(it) ?: true }
                .toSet()
    }

    @JvmStatic
    fun findClassesWithAnnotationType(resourcePattern: String,
                                      onlyInStartApplication: Boolean = false,
                                      annotationType: Class<out Annotation>): Set<Class<*>> {
        return findClassNames(resourcePattern, onlyInStartApplication)
                .map { Class.forName(it) }
                .filter {
                    if (annotationType == null) return@filter true;
                    return@filter it.getAnnotationsByType(annotationType) != null
                }
                .toSet()
    }


    /**
     * 判断类是否是指定的Jar中
     */
    @JvmStatic
    fun classIsInJar(type: Class<*>, jarFileName: String): Boolean {
        var sects = type.protectionDomain.codeSource.location.path.split("/")
        return sects.contains(jarFileName) && sects.last().contains(jarFileName)
    }


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