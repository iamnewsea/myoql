package nbcp.utils

import nbcp.comm.*
import java.lang.reflect.Modifier

/**
 * Created by udi on 17-4-10.
 */

/**
 * 递归的返回状态
 */
enum class RecursionReturnEnum private constructor(val value: Int) {
    None(0),
    Go(1),
    StopSub(2),
    Abord(6), // 6 = 2 + 4  , Abord 包含了 StopSub
    Remove(8);
}

//enum class ExecuteForReturnEnum{
//    Continue,
//    Break,
//
//}

/**
 * 递归执行工具类。
 */
object RecursionUtil {

    /** 递归执行
     * @param container:递归的集合。
     * @param producer: 生产者，获取下级集合。
     * @param consumer: 生产者，参数：当前对象，父对象，当前对象的索引。
     */
    fun <T> execute(container: MutableList<T>, producer: (T) -> MutableList<T>, consumer: (T, MutableList<T>, Int) -> RecursionReturnEnum): Int {
        if (container.size == 0) return 0
        var counted = 0;
        var removeIndeies = mutableListOf<Int>();

        for (i in container.indices) {
            val item = container[i]
            counted++;
            val ret = consumer(item, container, i)

            if (ret == RecursionReturnEnum.StopSub)
                continue
            else if (ret == RecursionReturnEnum.Abord) return counted;
            else if (ret == RecursionReturnEnum.Remove) {
                removeIndeies.add(i);
                continue;
            }

            counted += execute(producer(item), producer, consumer);
        }

        for (i in removeIndeies.reversed()) {
            container.removeAt(i);
        }

        return counted;
    }


    /** 递归查找
     * @param container:递归的集合。
     * @param producer: 生产者，获取下级集合。
     * @param consumer: 生产者
     */
    fun <T> findOne(container: List<T>, producer: (T) -> List<T>, consumer: (T) -> Boolean): T? {
        for (i in container.indices) {
            val item = container[i]
            var retVal = consumer(item)
            if (retVal == true) return item;
            var subVal = findOne(producer(item), producer, consumer)
            if (subVal != null)
                return subVal;
        }
        return null
    }


//    fun <T> remove(Container: MutableList<T>, Subs: (T) -> MutableList<T>, Exec: (T) -> Boolean) {
//        var index = -1;
//        while (true) {
//            index++;
//
//            if (index == Container.size) {
//                break;
//            }
//
//            val item = Container[index]
//            var retVal = Exec(item)
//            if (retVal == true) {
//                Container.removeAt(index);
//                index--;
//                continue;
//            }
//
//            remove(Subs(item), Subs, Exec)
//        }
//        return
//    }

    /**
     * 合并树,把 subTree 添加到 root 中去
     * @param root: 树节点的元素列表。上一级是空的根节点。
     * @param outcomer: 外来者，把它附加到树结构中去。 如果不匹配，则按新节点增加。
     */
    fun <T> unionTree(root: MutableList<T>, outcomer: T, producer: (T) -> MutableList<T>, compare: (T, T) -> Boolean) {

        for (rootItem in root) {
            if (compare(rootItem, outcomer)) {
                producer(outcomer).forEach { outItem ->
                    unionTree(producer(rootItem), outItem, producer, compare);
                }
                return;
            }
        }

        //如果没找到，则插入
        root.add(outcomer);
    }

    /**
     * 遍历对象 ,包括 Map,Array,List,Object， 应该拿到对象后，对象属性值及子属性操作，而不能增减父对象。
     * @param json: 递归对象
     * @param consumer:  消费每一个Json
     * @param consumerObject 如果遍历到非 Map，调用该回调
     */
    fun recursionJson(json: Map<*, *>,
                      consumerMap: (Map<*, *>) -> Boolean,
                      consumerList: ((List<*>) -> Boolean)? = null,
                      consumerObject: ((Any) -> Boolean)? = null,
                      deepth: Int = 0): Boolean {
        if (consumerMap(json) == false) {
            return false;
        }

        //判断对象是否是 Map
        return json.keys.toTypedArray().ForEachExt { key, index ->
            var value = json.get(key);
            if (value == null) {
                return@ForEachExt true;
            }

            var type = value::class.java;
            if (type.IsSimpleType()) {
                return@ForEachExt true;
            } else if (type.isArray) {
                return@ForEachExt recursionArray(value as Array<*>, consumerMap, consumerList, consumerObject, deepth + 1);
            } else if (type.IsListType()) {
                return@ForEachExt recursionList(value as List<*>, consumerMap, consumerList, consumerObject, deepth + 1);
            } else if (type.IsMapType()) {
                return@ForEachExt recursionJson(value as Map<*, *>, consumerMap, consumerList, consumerObject, deepth + 1);
            } else {
                return@ForEachExt recursionObject(value, consumerMap, consumerList, consumerObject, deepth + 1);
            }
        }
    }

    fun recursionAny(value: Any,
                     consumerMap: (Map<*, *>) -> Boolean,
                     consumerList: ((List<*>) -> Boolean)? = null,
                     consumerObject: ((Any) -> Boolean)? = null,
                     deepth: Int = 0): Boolean {
        var type = value::class.java;
        if (type.IsSimpleType()) {
            return true;
        } else if (type.isArray) {
            return recursionArray(value as Array<*>, consumerMap, consumerList, consumerObject, deepth + 1);
        } else if (type.IsListType()) {
            return recursionList(value as List<*>, consumerMap, consumerList, consumerObject, deepth + 1);
        } else if (type.IsMapType()) {
            return recursionJson(value as Map<*, *>, consumerMap, consumerList, consumerObject, deepth + 1);
        } else {
            return recursionObject(value, consumerMap, consumerList, consumerObject, deepth + 1);
        }
    }

    fun recursionObject(value: Any,
                        consumerMap: (Map<*, *>) -> Boolean,
                        consumerList: ((List<*>) -> Boolean)? = null,
                        consumerObject: ((Any) -> Boolean)? = null,
                        deepth: Int = 0): Boolean {

        if (consumerObject != null) {
            var ret = consumerObject.invoke(value);
            if (ret == false) {
                return false;
            }
        }

        var type = value::class.java;

        return type.AllFields.ForEachExt { it, index ->

            //            var key = it.name;
            var value = it.get(value);

            if (value == null) {
                return@ForEachExt true;
            }

            var type = value::class.java;
            if (type.IsSimpleType()) {
                return@ForEachExt true;
            } else if (type.isArray) {
                return@ForEachExt recursionArray(value as Array<*>, consumerMap, consumerList, consumerObject, deepth + 1);
            } else if (type.IsListType()) {
                return@ForEachExt recursionList(value as List<*>, consumerMap, consumerList, consumerObject, deepth + 1);
            } else if (type.IsMapType()) {
                return@ForEachExt recursionJson(value as Map<*, *>, consumerMap, consumerList, consumerObject, deepth + 1);
            } else {
                return@ForEachExt recursionObject(value, consumerMap, consumerList, consumerObject, deepth + 1);
            }
        }
    }

    fun recursionArray(array: Array<*>,
                       consumerMap: (Map<*, *>) -> Boolean,
                       consumerList: ((List<*>) -> Boolean)? = null,
                       consumerObject: ((Any) -> Boolean)? = null,
                       deepth: Int = 0): Boolean {

        return recursionList(array.toList(), consumerMap, consumerList, consumerObject, deepth);
    }

    fun recursionList(array: List<*>,
                      consumerMap: (Map<*, *>) -> Boolean,
                      consumerList: ((List<*>) -> Boolean)? = null,
                      consumerObject: ((Any) -> Boolean)? = null,
                      deepth: Int = 0): Boolean {

        if (consumerList != null) {
            var ret = consumerList(array);
            if (ret == false) {
                return false;
            }
        }

        return array.ForEachExt { it, index ->
            var value = it;

            if (value == null) {
                return@ForEachExt true;
            }

            var type = value::class.java;
            if (type.IsSimpleType()) {
                return@ForEachExt true;
            } else if (type.isArray) {
                return@ForEachExt recursionArray(value as Array<*>, consumerMap, consumerList, consumerObject, deepth + 1);
            } else if (type.IsListType()) {
                return@ForEachExt recursionList(value as List<*>, consumerMap, consumerList, consumerObject, deepth + 1);
            } else if (type.IsMapType()) {
                return@ForEachExt recursionJson(value as Map<*, *>, consumerMap, consumerList, consumerObject, deepth + 1);
            } else {
                return@ForEachExt recursionObject(value, consumerMap, consumerList, consumerObject, deepth + 1);
            }
        }
    }
}