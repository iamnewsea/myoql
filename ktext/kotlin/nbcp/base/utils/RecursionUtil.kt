package nbcp.utils

import nbcp.comm.ForEachExt
import nbcp.comm.IsListType
import nbcp.comm.IsSimpleType
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
     */
    fun recursionJson(json: Any, consumer: (Any, Class<*>) -> Boolean, deepth: Int = 0): Boolean {
        var type = json::class.java;
        if (type.IsSimpleType()) {
            return true;
        }

        if (consumer(json, type) == false) {
            return false;
        }

        if (type.isArray) {
            return (json as Array<*>).ForEachExt { it, index ->
                if (it == null) {
                    return@ForEachExt true
                }
                if (recursionJson(it, consumer, deepth + 1) == false) {
                    return@ForEachExt false;
                }
                return@ForEachExt true
            }
        } else if (type.IsListType()) {
            return (json as List<*>).ForEachExt { it, index ->
                if (it == null) {
                    return@ForEachExt true
                }

                if (recursionJson(it, consumer, deepth + 1) == false) {
                    return@ForEachExt false;
                }

                return@ForEachExt true;
            }
        }


        //判断对象是否是 Map
        if (json is Map<*, *>) {
            return json.keys.toTypedArray().ForEachExt { key, index ->
                var value = json.get(key);
                if (value == null) {
                    return@ForEachExt true;
                }

                if (recursionJson(value, consumer, deepth + 1) === false) {
                    return@ForEachExt false;
                }
                return@ForEachExt true;
            }
        }


        return type.declaredFields.ForEachExt { it, index ->
            if (it.modifiers and Modifier.STATIC > 0) {
                return@ForEachExt true
            }

            if (it.modifiers and Modifier.TRANSIENT > 0) {
                return@ForEachExt true;
            }

            it.isAccessible = true;
            var key = it.name;
            var value = it.get(json);

            if (value == null) {
                return@ForEachExt true;
            }

            if (recursionJson(value, consumer, deepth + 1) === false) {
                return@ForEachExt false;
            }
            return@ForEachExt true
        }
    }
}