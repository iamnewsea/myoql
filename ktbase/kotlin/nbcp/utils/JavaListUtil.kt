package nbcp.utils

/**
 * 仅给Java使用。
 * Created by udi on 17-5-22.
 */

object JavaListUtil {
    @JvmStatic
    fun <T> mutableListOf(vararg elements: T): MutableList<T> = kotlin.collections.mutableListOf(*elements)

    @JvmStatic
    fun <T> listOf(vararg elements: T): List<T> = kotlin.collections.listOf(*elements)

    @JvmStatic
    fun <T> setOf(vararg elements: T): Set<T> = kotlin.collections.setOf(*elements)

    @JvmStatic
    fun <T> toSet(list: Iterable<T>): Set<T> {
        return list.toSet();
    }

    @JvmStatic
    fun <T> toMutableSet(list: Iterable<T>): MutableSet<T> {
        return list.toMutableSet()
    }

    @JvmStatic
    fun <T, R> map(list: Collection<T>, transform: (T) -> R): List<R> {
        return list.map(transform);
    }


    @JvmStatic
    fun <T> filter(list: Collection<T>, predicate: (T) -> Boolean): List<T> {
        return list.filter(predicate);
    }

    @JvmStatic
    fun <K, V> any(list: Map<K, V>): Boolean {
        return list.any();
    }

    @JvmStatic
    fun <T> any(list: Collection<T>): Boolean {
        return list.any();
    }


    @JvmStatic
    fun <K, V> any(list: Map<K, V>, transform: (Map.Entry<K, V>) -> Boolean): Boolean {
        return list.any(transform);
    }

    @JvmStatic
    fun <T> any(list: Collection<T>, transform: (T) -> Boolean): Boolean {
        return list.any(transform);
    }


    @JvmStatic
    fun <T> plus(list1: List<T>, list2: List<T>): List<T> {
        return list1 + list2;
    }

    @JvmStatic
    fun <T> minus(list1: List<T>, list2: List<T>): List<T> {
        return list1 - list2;
    }

    @JvmStatic
    fun <T> plus(list1: Set<T>, list2: Set<T>): Set<T> {
        return list1 + list2;
    }

    @JvmStatic
    fun <T> minus(list1: Set<T>, list2: Set<T>): Set<T> {
        return list1 - list2;
    }
}