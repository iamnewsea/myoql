package cn.dev8.util;


import lombok.var;

import java.util.*;
import java.util.function.Function;

/**
 * 集合工具类
 *
 * @author Yuxh
 */
public class SetUtil {


    public static <T> Set<T> slice(T[] list, int startIndex) {
        return slice(list, startIndex, list.length);
    }

    /**
     * 截取
     *
     * @param list
     * @param startIndex
     * @param endIndex
     * @param <T>
     * @return
     */
    public static <T> Set<T> slice(T[] list, int startIndex, int endIndex) {
        return slice( ListUtil.asList(list), startIndex, endIndex);
    }


    public static <T> Set<T> slice(Collection<T> list, int startIndex) {
        return slice(list, startIndex, list.size());
    }

    /**
     * @param list
     * @param startIndex
     * @param endIndex
     * @param <T>
     * @return 大于等于 startIndex, 小于 endIndex
     */
    public static <T> Set<T> slice(Collection<T> list, int startIndex, int endIndex) {
        if (startIndex < 0) {
            startIndex = list.size() + startIndex;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }

        if (endIndex < 0) {
            endIndex = list.size() + endIndex;
        }
        if (endIndex > list.size()) {
            endIndex = list.size();
        }

        var ret = new LinkedHashSet<T>();
        var index = -1;
        for (var it : list) {
            index++;
            if (index < startIndex || index >= endIndex) {
                continue;
            }
            ret.add(it);
        }
        return ret;
    }


    /**
     * List to Set
     *
     * @param collection
     * @param <T>
     * @return
     */
    public static <T> Set<T> asSet(Collection<T> collection) {
        var ret = new LinkedHashSet<T>();
        if (collection == null) {
            return ret;
        }
        for (T t : collection) {
            ret.add(t);
        }
        return ret;
    }

    /**
     * Array转为List
     *
     * @param arrayObject
     * @param <T>
     * @return
     */
    public static <T> Set<T> asSet(T[] arrayObject) {
        var ret = new HashSet<T>();

        for (var i = 0; i < arrayObject.length; i++) {
            ret.add((T) arrayObject[i]);
        }

        return ret;
    }


    public static <T> Set<T> asSet(Enumeration<T> enumeration, Class<T> type) {
        var ret = new HashSet<T>();

        while (enumeration.hasMoreElements()) {
            var item = enumeration.nextElement();
            ret.add((T) item);
        }

        return ret;
    }


    public static <T> Set<T> fromItems(T... arrayObject) {
        var ret = new HashSet<T>();

        for (var i = 0; i < arrayObject.length; i++) {
            ret.add((T) arrayObject[i]);
        }

        return ret;
    }


    /**
     * 获取集合最后一个
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T getLast(Set<T> list) {
        return getLast(list, null);
    }

    public static <T> T getLast(Set<T> list, Function<T, Boolean> callback) {
        if (list == null || list.size() == 0) {
            return null;
        }

        var ie = list.iterator();
        while (ie.hasNext()) {
            var item = ie.next();

            if (callback == null || callback.apply(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 把数组的集合 解成 集合
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> Set<T> unwindArray(Set<T[]> list) {
        var ret = new LinkedHashSet<T>();
        for (var i = 0; i < list.size(); i++) {
            var item = ListUtil.elementAt(list, i);
            for (var j = 0; j < item.length; j++) {
                ret.add(item[j]);
            }
        }
        return ret;
    }

    /**
     * 展开 数组的数组, 如： [  [a,b], [c,d] ] ==> [a,b,c,d]
     */
    public static <T> Set<T> unwind(Set<List<T>> list) {
        var ret = new LinkedHashSet<T>();
        for (var i = 0; i < list.size(); i++) {
            var item = ListUtil.elementAt(list, i);
            for (var j = 0; j < item.size(); j++) {
                ret.add(item.get(j));
            }
        }
        return ret;
    }
}
