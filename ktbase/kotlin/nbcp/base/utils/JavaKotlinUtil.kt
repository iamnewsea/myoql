package nbcp.base.utils

import java.io.File

/**
 * 仅给Java使用。
 * Created by udi on 17-5-22.
 */

object JavaKotlinUtil {
    @JvmStatic
    fun <T> toMutableList(list: Iterable<T>): MutableList<T> {
        return list.toMutableList();
    }

    @JvmStatic
    fun <T> mutableListOf(vararg elements: T): MutableList<T> = kotlin.collections.mutableListOf(*elements)


    @JvmStatic
    fun <T> arrayListOf(vararg elements: T): ArrayList<T> = kotlin.collections.arrayListOf(*elements)

    @JvmStatic
    fun <T> listOf(vararg elements: T): MutableList<T> = kotlin.collections.mutableListOf(*elements)

    @JvmStatic
    fun <T> setOf(vararg elements: T): MutableSet<T> = kotlin.collections.mutableSetOf(*elements)

    @JvmStatic
    fun <T> toSet(list: Iterable<T>): MutableSet<T> {
        return list.toMutableSet();
    }

    @JvmStatic
    fun <T> toSet(list: Array<T>): MutableSet<T> {
        return list.toMutableSet();
    }

    @JvmStatic
    fun <T> toMutableSet(list: Iterable<T>): MutableSet<T> {
        return list.toMutableSet()
    }

    @JvmStatic
    fun <M, N> to(o1: M, o2: N): Pair<M, N> {
        return o1 to o2;
    }

    @JvmStatic
    fun <T, R> map(list: Collection<T>, transform: (T) -> R): MutableList<R> {
        return list.map(transform).toMutableList();
    }


    @JvmStatic
    fun <T> filter(list: Collection<T>, predicate: (T) -> Boolean): MutableList<T> {
        return list.filter(predicate).toMutableList();
    }

    @JvmStatic
    fun <T> filterNot(list: Collection<T>, predicate: (T) -> Boolean): MutableList<T> {
        return list.filterNot(predicate).toMutableList();
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
    fun <T> all(list: Collection<T>, transform: (T) -> Boolean): Boolean {
        return list.all(transform);
    }

    @JvmStatic
    fun <T> count(list: Collection<T>, transform: (T) -> Boolean): Int {
        return list.count(transform);
    }

    @JvmStatic
    fun <T, R : Comparable<R>> sortBy(list: MutableList<T>, selector: (T) -> R?): Unit {
        list.sortBy(selector);
    }

    @JvmStatic
    fun <T> none(list: Collection<T>, transform: (T) -> Boolean): Boolean {
        return list.none(transform);
    }


    @JvmStatic
    fun <T> take(list: Collection<T>, n: Int): MutableList<T> {
        return list.take(n).toMutableList();
    }

    @JvmStatic
    fun <T> take(value: String, n: Int): String {
        return value.take(n);
    }

    @JvmStatic
    fun <T, R : Comparable<R>> maxByOrNull(list: Collection<T>, selector: (T) -> R): T? {
        return list.maxByOrNull(selector);
    }

    @JvmStatic
    fun <T, K> distinctBy(list: Collection<T>, selector: (T) -> K): MutableList<T> {
        return list.distinctBy(selector).toMutableList();
    }

    @JvmStatic
    fun <T> distinct(list: Collection<T>): MutableList<T> {
        return list.distinct().toMutableList();
    }

    @JvmStatic
    fun <T> reversed(list: Collection<T>): MutableList<T> {
        return list.reversed().toMutableList();
    }

    @JvmStatic
    fun <T> intersect(list1: Collection<T>, list2: Collection<T>): MutableSet<T> {
        return list1.intersect(list2).toMutableSet();
    }


    @JvmStatic
    fun <T> union(list1: Collection<T>, list2: Collection<T>): MutableSet<T> {
        return list1.union(list2).toMutableSet();
    }

    @JvmStatic
    fun <T> first(list: Collection<T>): T {
        return list.first();
    }

    @JvmStatic
    fun <T> first(list: Collection<T>, predicate: (T) -> Boolean): T {
        return list.first(predicate);
    }

    @JvmStatic
    fun <T> firstOrNull(list: Array<T>, predicate: (T) -> Boolean): T? {
        return list.firstOrNull(predicate);
    }

    @JvmStatic
    fun <T> firstOrNull(list: Array<T>): T? {
        return list.firstOrNull();
    }

    @JvmStatic
    fun <T> firstOrNull(list: Collection<T>, predicate: (T) -> Boolean): T? {
        return list.firstOrNull(predicate);
    }

    @JvmStatic
    fun <T> firstOrNull(list: Collection<T>): T? {
        return list.firstOrNull();
    }

    @JvmStatic
    fun <T> removeAll(list: MutableCollection<T>, predicate: (T) -> Boolean): Boolean {
        return list.removeAll(predicate);
    }

    @JvmStatic
    fun <T> plus(list1: List<T>, list2: List<T>): MutableList<T> {
        return (list1 + list2).toMutableList();
    }

    @JvmStatic
    fun <T> minus(list1: List<T>, list2: List<T>): MutableList<T> {
        return (list1 - list2).toMutableList()
    }

    @JvmStatic
    fun <T> plus(list1: Set<T>, list2: Set<T>): MutableSet<T> {
        return (list1 + list2).toMutableSet()
    }

    @JvmStatic
    fun <T> minus(list1: Set<T>, list2: Set<T>): MutableSet<T> {
        return (list1 - list2).toMutableSet()
    }

    @JvmStatic
    fun <T> joinToString(list: Iterable<T>, separator: String): String {
        return list.joinToString(separator);
    }

    @JvmStatic
    fun <T> split(value: String, separator: String): MutableList<String> {
        return value.split(separator).toMutableList();
    }


    @JvmStatic
    fun <T> trim(value: String, predicate: (Char) -> Boolean): String {
        return value.trim(predicate);
    }


    @JvmStatic
    fun <T> toTypedArray(list: Collection<T>, type: Class<T>): Array<T> {
        var ret = java.lang.reflect.Array.newInstance(type, list.size) as Array<T>;

        list.forEachIndexed { index, it ->
            java.lang.reflect.Array.set(ret, index, it);
        }
        return ret;
    }

    @JvmStatic
    fun <T, K> associateBy(list: Iterable<T>, keySelector: (T) -> K): MutableMap<K, T> {
        return list.associateBy(keySelector).toMutableMap();
    }

    @JvmStatic
    fun <K, V> toMap(list: Iterable<Pair<K, V>>): MutableMap<K, V> {
        return list.toMap().toMutableMap();
    }

    @JvmStatic
    fun <K, V> toList(map: Map<K, V>): MutableList<Pair<K, V>> {
        return map.toList().toMutableList()
    }


    /**
     * 递归删除文件
     */
    @JvmStatic
    fun deleteFileRecursively(file: File): Boolean {
        if (file.exists() == false) return false;
        return file.deleteRecursively()
    }


    @JvmStatic
    fun replace(value: String, oldValue: String, newValue: String, ignoreCase: Boolean): String {
        return value.replace(oldValue, newValue, ignoreCase);
    }
}