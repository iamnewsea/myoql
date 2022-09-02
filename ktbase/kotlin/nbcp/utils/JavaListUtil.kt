package nbcp.utils

/**
 * 仅给Java使用。
 * Created by udi on 17-5-22.
 */

object JavaListUtil {
    @JvmStatic
    fun <T> toMutableList(list: Iterable<T>): MutableList<T> {
        return list.toMutableList();
    }

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
    fun <T> toSet(list: Array<T>): Set<T> {
        return list.toSet();
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
    fun <T, R> map(list: Collection<T>, transform: (T) -> R): List<R> {
        return list.map(transform);
    }


    @JvmStatic
    fun <T> filter(list: Collection<T>, predicate: (T) -> Boolean): List<T> {
        return list.filter(predicate);
    }

    @JvmStatic
    fun <T> filterNot(list: Collection<T>, predicate: (T) -> Boolean): List<T> {
        return list.filterNot(predicate);
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
    fun <T> take(list: Collection<T>, n: Int): List<T> {
        return list.take(n);
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
    fun <T, K> distinctBy(list: Collection<T>, selector: (T) -> K): List<T> {
        return list.distinctBy(selector);
    }

    @JvmStatic
    fun <T> distinct(list: Collection<T>): List<T> {
        return list.distinct();
    }

    @JvmStatic
    fun <T> reversed(list: Collection<T>): List<T> {
        return list.reversed();
    }

    @JvmStatic
    fun <T> intersect(list1: Collection<T>, list2: Collection<T>): Set<T> {
        return list1.intersect(list2);
    }


    @JvmStatic
    fun <T> union(list1: Collection<T>, list2: Collection<T>): Set<T> {
        return list1.union(list2);
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

    @JvmStatic
    fun <T> joinToString(list: Iterable<T>, separator: String): String {
        return list.joinToString(separator);
    }

    @JvmStatic
    fun <T> split(value: String, separator: String): List<String> {
        return value.split(separator);
    }


    @JvmStatic
    fun <T> trim(value: String, predicate: (Char) -> Boolean): String {
        return value.trim(predicate);
    }


    @JvmStatic
    fun <T> toTypedArray(list: Collection<T>, clazz: Class<T>): Array<T> {
        var ret = java.lang.reflect.Array.newInstance(clazz, list.size) as Array<T>;

        list.forEachIndexed { index, it ->
            java.lang.reflect.Array.set(ret, index, it);
        }
        return ret;
    }

    @JvmStatic
    fun <T, K> associateBy(list: Iterable<T>, keySelector: (T) -> K): Map<K, T> {
        return list.associateBy(keySelector);
    }

    @JvmStatic
    fun <K, V> toMap(list: Iterable<Pair<K, V>>): Map<K, V> {
        return list.toMap();
    }

    @JvmStatic
    fun <K, V> toList(map: Map<K, V>): List<Pair<K, V>> {
        return map.toList()
    }
}