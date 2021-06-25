package nbcp.utils

import nbcp.comm.*
import org.apache.http.annotation.Obsolete
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import org.springframework.util.ClassUtils
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException
import java.net.JarURLConnection
import java.net.URL
import java.net.URLClassLoader
import java.time.LocalDateTime
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

}