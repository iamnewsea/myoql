package cn.dev8.util;

import lombok.var;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ArrayUtil {

    /**
     * 自动转为 Array
     *
     * @param collection
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T[] asArray(Collection<T> collection, Class<T> type) {
        if (collection == null) return null;


        var ret = Array.newInstance(type, collection.size());
        var index = -1;
        for (Object item : collection) {
            index++;
            Array.set(ret, index, item);
        }
        return (T[]) ret;

    }

    public static <T> T[] asArray(Enumeration<T> em, Class<T> type) {
        if (em == null) return null;

        var list = new ArrayList<T>();
        while (em.hasMoreElements()) {
            var it = em.nextElement();
            list.add((T) it);
        }
        return asArray(list, type);
    }


    /**
     * 转为 List
     *
     * @param arrayObject
     * @param <T>
     * @return
     */
    public static <T> T[] fromItems(T... arrayObject) {
        var ret = Array.newInstance(arrayObject[0].getClass(), arrayObject.length);

        for (var i = 0; i < arrayObject.length; i++) {
            Array.set(ret, i, arrayObject[i]);
        }

        return (T[]) ret;
    }




    public static <T> T getLast(T[] list) {
        return getLast(list, null);
    }


    /**
     * 获取数组最后一个
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T getLast(T[] list, Function<T, Boolean> callback) {
        if (isNullOrEmpty(list)) {
            return null;
        }

        for (int i = list.length - 1; i >= 0; i--) {
            var item = list[i];

            if (callback == null || callback.apply(item)) {
                return item;
            }
        }
        return null;
    }


    public static <T> List<T> slice(T[] list, int startIndex) {
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
    public static <T> List<T> slice(T[] list, int startIndex, int endIndex) {
        return ListUtil.slice(ListUtil.asList(list), startIndex, endIndex);
    }




    public static <T> Set<T> interSect(T[] list1, T[] list2) {
        return interSect(list1, list2, null);
    }

    public static <T> Set<T> interSect(T[] list1, T[] list2, BiFunction<T, T, Boolean> equals) {
        return ListUtil. interSect(Arrays.asList(list1), Arrays.asList(list2), equals);
    }



    public static <T> Set<T> minus(T[] total, T[] other) {
        return minus(total, other, null);
    }

    public static <T> Set<T> minus(T[] total, T[] other, BiFunction<T, T, Boolean> equals) {
        return ListUtil.minus(Arrays.asList(total), Arrays.asList(other), equals);
    }



    public static <T> Set<T> union(T[] list1, T[] list2) {
        return union(list1, list2, null);
    }

    public static <T> Set<T> union(T[] list1, T[] list2, BiFunction<T, T, Boolean> equals) {
        return ListUtil.union(Arrays.asList(list1), Arrays.asList(list2), equals);
    }



    public static <T> int indexOf(T[] array, Function<T, Boolean> callback) {
        return ListUtil.indexOf(Arrays.asList(array), callback);
    }

    /**
     * 查找索引
     *
     * @param array
     * @param find
     * @param <T>
     * @return
     */
    public static <T> int indexOfItem(T[] array, Object find) {
        return indexOf(array, it -> it.equals(find));
    }

    /**
     * 判断是否有值
     *
     * @param value
     * @return
     */
    public static boolean hasValue(Object[] value) {
        if (value == null) {
            return false;
        }
        return Array.getLength(value) > 0;
    }


    public static boolean isNullOrEmpty(Object[] value) {
        return !hasValue(value);
    }


    /**
     * 判断是否包含
     *
     * @param list
     * @param item
     * @return
     */
    public static boolean contains(Object[] list, Object item) {
        return indexOf(list, it -> it.equals(item)) > -1;
    }
}
