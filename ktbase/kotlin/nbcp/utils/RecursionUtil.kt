package nbcp.utils

import nbcp.comm.*

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
    @JvmStatic
    fun <T> filter(
        container: List<T>,
        producer: (T) -> MutableList<T>,
        idCallback: (T) -> String,
        callback: (Set<T>, Int) -> Boolean
    ) {
        var list_queryed_ids = mutableListOf<String>();
        var list_wbs_ids = mutableSetOf<String>();

        //如果菜单树中有匹配项，则显示向上的路径，显示下级所有节点。
        RecursionUtil.execute<T>(
            container,
            producer,
            { wbs, index ->
                if (callback(wbs, index) == false) {
                    return@execute RecursionReturnEnum.Go
                }

                val item = wbs.last();
                val item_id = idCallback(item)
                list_queryed_ids.add(item_id)

                if (container.any()) {
                    list_wbs_ids.addAll(
                        RecursionUtil.getWbs(
                            container,
                            producer,
                            { item2 ->
                                return@getWbs idCallback(item2) == item_id
                            }).map { idCallback(it) }
                    )
                } else {
                    list_wbs_ids.add(item_id);
                }
                return@execute RecursionReturnEnum.StopSub
            });


        RecursionUtil.execute<T>(
            container,
            producer,
            { wbs, _ ->
                var item = wbs.last();
                var item_id = idCallback(item)
                if (list_wbs_ids.contains(item_id) == false) {
                    return@execute RecursionReturnEnum.Remove;
                }

                if (list_queryed_ids.contains(item_id)) {
                    return@execute RecursionReturnEnum.StopSub;
                }

                return@execute RecursionReturnEnum.Go;
            }
        );
    }


    /** 递归执行
     * @param container:递归的集合。
     * @param producer: 生产者，获取下级集合。
     * @param consumer: 消费者，参数：Wbs对象，当前对象的索引。
     */
    @JvmStatic
    fun <T> execute(
        container: List<T>,
        producer: (T) -> MutableList<T>,
        consumer: (Set<T>, Int) -> RecursionReturnEnum
    ): Int {
        return _execute(container as MutableList<T>, producer, consumer);
    }

    private fun <T> _execute(
        container: MutableList<T>,
        producer: (T) -> MutableList<T>,
        consumer: (Set<T>, Int) -> RecursionReturnEnum,
        parents: Set<T> = setOf()
    ): Int {
        if (container.size == 0) return 0
        var counted = 0;
        var removeIndexes = mutableListOf<Int>();

        for (i in container.indices) {
            val item = container[i]
            counted++;
            var setT = parents.union(listOf(item));
            val ret = consumer(setT, i)

            if (ret == RecursionReturnEnum.StopSub)
                continue
            else if (ret == RecursionReturnEnum.Abord) return counted;
            else if (ret == RecursionReturnEnum.Remove) {
                removeIndexes.add(i);
                continue;
            }

            counted += _execute(producer(item), producer, consumer, setT);
        }

        for (i in removeIndexes.reversed()) {
            container.removeAt(i);
        }

        return counted;
    }


    /** 递归查找
     * @param container:递归的集合。
     * @param producer: 生产者，获取下级集合。
     * @param consumer: 生产者
     */
    @JvmStatic
    fun <T> findOne(
        container: Collection<T>,
        producer: (T) -> Collection<T>,
        consumer: (T) -> Boolean
    ): T? {
        for (i in container.indices) {
            val item = container.elementAt(i)
            var retVal = consumer(item)
            if (retVal == true) return item;
            var subVal = findOne(producer(item), producer, consumer)
            if (subVal != null)
                return subVal;
        }
        return null
    }

    /**
     * 查找从节点到当前节点的路径
     */
    @JvmOverloads
    @JvmStatic
    fun <T> getWbs(
        container: Collection<T>,
        producer: (T) -> Collection<T>,
        consumer: (T) -> Boolean,
        parents: List<T> = listOf()
    ): MutableList<T> {
        for (i in container.indices) {
            val item = container.elementAt(i)
            var retVal = consumer(item)
            var path = parents.union(listOf(item)).toMutableList()
            if (retVal == true) return path;
            var subVal = getWbs(producer(item), producer, consumer, path)
            if (subVal.any())
                return subVal;
        }
        return mutableListOf()
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
    @JvmStatic
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
     * 遍历Json, 应该拿到对象后，对象属性值及子属性操作，而不能增减父对象。尽量使用 execute 方法遍历树。
     * @param json: 递归对象
     * @param consumer:  消费每一个Json
     * @param consumerObject 如果遍历到非 Map，调用该回调
     */
    @JvmOverloads
    @JvmStatic
    fun recursionJson(
        json: Map<*, *>,
        consumerMap: (Map<*, *>) -> Boolean,
        consumerList: ((Collection<*>) -> Boolean)? = null,
        consumerObject: ((Any) -> Boolean)? = null,
        deepth: Int = 0
    ): Boolean {
        if (consumerMap(json) == false) {
            return false;
        }

        //判断对象是否是 Map
        return json.keys.toTypedArray().ForEachExt { key, _ ->
            var value = json.get(key);
            if (value == null) {
                return@ForEachExt true;
            }

            var type = value::class.java;
            if (type.IsSimpleType()) {
                return@ForEachExt true;
            } else if (type.isArray) {
                return@ForEachExt recursionArray(
                    value as Array<*>,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            } else if (type.IsCollectionType) {
                return@ForEachExt recursionList(
                    value as Collection<*>,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            } else if (type.IsMapType) {
                return@ForEachExt recursionJson(
                    value as Map<*, *>,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            } else {
                return@ForEachExt recursionObject(
                    value,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            }
        }
    }

    /**
     * 递归对象 ,内部包括 Map,Array,List,Object。
     * @param value 递归对象
     * @param consumerMap : 发现一个Map, 第一个参数是发现的Map,第二个参数是父key
     * @param consumerList : 发现一个List, 第一个参数是发现的List,第二个参数是父key
     * @param consumerObject: 发现一个 Object, 第一个参数是发现的Object,第二个参数是父key
     */
    @JvmOverloads
    @JvmStatic
    fun recursionAny(
        value: Any,
        consumerMap: (Map<*, *>) -> Boolean,
        consumerList: ((Collection<*>) -> Boolean)? = null,
        consumerObject: ((Any) -> Boolean)? = null
    ): Boolean {
        var type = value::class.java;
        if (type.IsSimpleType()) {
            return true;
        } else if (type.isArray) {
            return recursionArray(value as Array<*>, consumerMap, consumerList, consumerObject, 1);
        } else if (type.IsCollectionType) {
            return recursionList(value as Collection<*>, consumerMap, consumerList, consumerObject, 1);
        } else if (type.IsMapType) {
            return recursionJson(value as Map<*, *>, consumerMap, consumerList, consumerObject, 1);
        } else {
            return recursionObject(value, consumerMap, consumerList, consumerObject, 1);
        }
    }

    private fun recursionObject(
        value: Any,
        consumerMap: (Map<*, *>) -> Boolean,
        consumerList: ((Collection<*>) -> Boolean)? = null,
        consumerObject: ((Any) -> Boolean)? = null,
        deepth: Int = 0
    ): Boolean {

        if (consumerObject != null) {
            var ret = consumerObject.invoke(value);
            if (ret == false) {
                return false;
            }
        }

        return value::class.java.AllFields.ForEachExt { it, _ ->

//            var key = it.name;
            val fieldValue = it.get(value);

            if (fieldValue == null) {
                return@ForEachExt true;
            }

            var type = fieldValue::class.java;
            if (type.IsSimpleType()) {
                return@ForEachExt true;
            } else if (type.isArray) {
                return@ForEachExt recursionArray(
                    fieldValue as Array<*>,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            } else if (type.IsCollectionType) {
                return@ForEachExt recursionList(
                    fieldValue as Collection<*>,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            } else if (type.IsMapType) {
                return@ForEachExt recursionJson(
                    fieldValue as Map<*, *>,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            } else {
                return@ForEachExt recursionObject(fieldValue, consumerMap, consumerList, consumerObject, deepth + 1);
            }
        }
    }

    @JvmOverloads
    @JvmStatic
    fun recursionArray(
        array: Array<*>,
        consumerMap: (Map<*, *>) -> Boolean,
        consumerList: ((Collection<*>) -> Boolean)? = null,
        consumerObject: ((Any) -> Boolean)? = null,
        deepth: Int = 0
    ): Boolean {

        return recursionList(array.toList(), consumerMap, consumerList, consumerObject, deepth);
    }

    @JvmOverloads
    @JvmStatic
    fun recursionList(
        array: Collection<*>,
        consumerMap: (Map<*, *>) -> Boolean,
        consumerList: ((Collection<*>) -> Boolean)? = null,
        consumerObject: ((Any) -> Boolean)? = null,
        deepth: Int = 0
    ): Boolean {

        if (consumerList != null) {
            var ret = consumerList(array);
            if (ret == false) {
                return false;
            }
        }

        return array.ForEachExt { it, _ ->
            var value = it;

            if (value == null) {
                return@ForEachExt true;
            }

            var type = value::class.java;
            if (type.IsSimpleType()) {
                return@ForEachExt true;
            } else if (type.isArray) {
                return@ForEachExt recursionArray(
                    value as Array<*>,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            } else if (type.IsCollectionType) {
                return@ForEachExt recursionList(
                    value as Collection<*>,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            } else if (type.IsMapType) {
                return@ForEachExt recursionJson(
                    value as Map<*, *>,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            } else {
                return@ForEachExt recursionObject(
                    value,
                    consumerMap,
                    consumerList,
                    consumerObject,
                    deepth + 1
                );
            }
        }
    }
}