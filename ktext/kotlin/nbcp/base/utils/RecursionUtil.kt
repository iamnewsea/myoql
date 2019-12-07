package nbcp.base.utils

import nbcp.base.extend.ForEachExt
import nbcp.base.extend.IsListType
import nbcp.base.extend.IsSimpleType
import java.lang.reflect.Modifier
import java.util.*

/**
 * Created by udi on 17-4-10.
 */

enum class RecursionReturnEnum private constructor(val Value: Int) {
    None(0),
    Go(1),
    StopSub(2),
    Abord(6),
    Remove(8);
}

//enum class ExecuteForReturnEnum{
//    Continue,
//    Break,
//
//}

object RecursionUtil {


    /* 递归执行
    * @param Exec 传入的是子项
    */
    fun <T> execute(Container: MutableList<T>, Subs: (T) -> MutableList<T>, Exec: (T, MutableList<T>, Int) -> RecursionReturnEnum): Int {
        if (Container.size == 0) return 0
        var counted = 0;
        var removeIndeies = mutableListOf<Int>();

        for (i in Container.indices) {
            val item = Container[i]
            counted++;
            val ret = Exec(item, Container, i)

            if (ret == RecursionReturnEnum.StopSub)
                continue
            else if (ret == RecursionReturnEnum.Abord) return counted;
            else if (ret == RecursionReturnEnum.Remove) {
                removeIndeies.add(i);
                continue;
            }

            counted += execute(Subs(item), Subs, Exec);
        }

        for (i in removeIndeies.reversed()) {
            Container.removeAt(i);
        }

        return counted;
    }


    fun <T> getOne(Container: List<T>, Subs: (T) -> List<T>, Exec: (T) -> Boolean): T? {
        for (i in Container.indices) {
            val item = Container[i]
            var retVal = Exec(item)
            if (retVal == true) return item;
            var subVal = getOne(Subs(item), Subs, Exec)
            if (subVal != null)
                return subVal;
        }
        return null
    }


    fun <T> remove(Container: MutableList<T>, Subs: (T) -> MutableList<T>, Exec: (T) -> Boolean) {
        var index = -1;
        while (true) {
            index++;

            if (index == Container.size) {
                break;
            }

            val item = Container[index]
            var retVal = Exec(item)
            if (retVal == true) {
                Container.removeAt(index);
                index--;
                continue;
            }

            remove(Subs(item), Subs, Exec)
        }
        return
    }

    //合并树,把 subTree 添加到 root 中去
    fun <T> unionTree(root: MutableList<T>, subTree: T, subs: (T) -> MutableList<T>, compare: (T, T) -> Boolean) {

        for (subItem in root) {
            if (compare(subItem, subTree)) {
                subs(subTree).forEach {
                    unionTree(subs(subItem), it, subs, compare);
                }
                return;
            }
        }
        root.add(subTree);
    }

    /**
     * 遍历对象
     * @param eachJsonItemCallback: key,value,parent三个参数
     */
    fun recursionJson(json: Any, eachJsonItemCallback: (String, Any?, Any) -> Boolean, deepth: Int = 0): Boolean {
        var type = json::class.java;
        if (type.IsSimpleType()) {
            return true;
        }


        if (type.isArray) {
            return (json as Array<*>).ForEachExt { it, index ->
                if (it == null) {
                    return@ForEachExt true
                }
                return@ForEachExt recursionJson(it, eachJsonItemCallback, deepth + 1)
            }
        } else if (type.IsListType()) {
            return (json as List<*>).ForEachExt { it, index ->
                if (it == null) {
                    return@ForEachExt true
                }

                return@ForEachExt recursionJson(it, eachJsonItemCallback, deepth + 1)
            }
        }


        return type.declaredFields.ForEachExt { it, index ->
            if (it.modifiers and Modifier.STATIC > 0) {
                return@ForEachExt true
            }

            if (it.modifiers and Modifier.TRANSIENT > 0) {
                return@ForEachExt true;
            }

            var key = it.name;
            it.isAccessible = true;

            var value = it.get(json);

            if (eachJsonItemCallback(key, value, json) === false) {
                return@ForEachExt false;
            }

            if (value != null) {
                if (recursionJson(value, eachJsonItemCallback, deepth + 1) === false) {
                    return@ForEachExt false;
                }
            }
            return@ForEachExt true
        }
    }
}