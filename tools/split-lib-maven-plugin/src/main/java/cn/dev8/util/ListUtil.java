package cn.dev8.util;


import lombok.var;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集合工具类
 *
 * @author Yuxh
 */
public class ListUtil {



    public static <T> Set<T> interSect(Collection<T> list1, Collection<T> list2) {
        return interSect(list1, list2, null);
    }

    /**
     * 求交集
     *
     * @param list1
     * @param list2
     * @param <T>
     * @return
     */
    public static <T> Set<T> interSect(Collection<T> list1, Collection<T> list2, BiFunction<T, T, Boolean> equals) {
        var ret = new LinkedHashSet<T>();
        for (var it : list1) {
            if (indexOf(list2, it, equals) > -1) {
                ret.add(it);
            }
        }
        return ret;
    }


    public static <T> Set<T> minus(Collection<T> total, Collection<T> other) {
        return minus(total, other, null);
    }

    /**
     * 求差集
     *
     * @param total
     * @param other
     * @param <T>
     * @return
     */
    public static <T> Set<T> minus(Collection<T> total, Collection<T> other, BiFunction<T, T, Boolean> equals) {
        var ret = new LinkedHashSet<T>();
        for (var it : total) {
            if (indexOf(other, it, equals) < 0) {
                ret.add(it);
            }
        }
        return ret;
    }


    public static <T> Set<T> union(Collection<T> list1, Collection<T> list2) {
        return union(list1, list2, null);
    }

    /**
     * 求并集
     *
     * @param list1
     * @param list2
     * @param equals
     * @param <T>
     * @return
     */
    public static <T> Set<T> union(Collection<T> list1, Collection<T> list2, BiFunction<T, T, Boolean> equals) {
        var ret = new LinkedHashSet<T>();

        for (var it : list1) {
            if (indexOf(ret, it, equals) < 0) {
                ret.add(it);
            }
        }

        for (var it : list2) {
            if (indexOf(ret, it, equals) < 0) {
                ret.add(it);
            }
        }
        return ret;
    }


    public static <T> Set<T> distinct(Collection<T> list) {
        return distinct(list, null);
    }

    public static <T> Set<T> distinct(Collection<T> list, BiFunction<T, T, Boolean> equals) {
        var ret = new LinkedHashSet<T>();

        for (var it : list) {
            if (indexOf(ret, it, equals) < 0) {
                ret.add(it);
            }
        }
        return ret;
    }


    public static <T> List<T> slice(Collection<T> list, int startIndex) {
        return slice(list, startIndex, list.size());
    }

    /**
     * @param list
     * @param startIndex
     * @param endIndex
     * @param <T>
     * @return 大于等于 startIndex, 小于 endIndex
     */
    public static <T> List<T> slice(Collection<T> list, int startIndex, int endIndex) {
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

        var ret = new ArrayList<T>();
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
     * 查找指定元素的索引
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> int indexOf(Collection<T> list, Function<T, Boolean> callback) {
        var index = -1;
        for (var it : list) {
            index++;
            if (callback.apply(it)) {
                return index;
            }
        }
        return -1;
    }



    public static <T> int indexOf(Collection<T> list, T find, BiFunction<T, T, Boolean> equals) {
        var index = -1;
        for (var it : list) {
            index++;
            if (equals != null ? equals.apply(it, find) : it.equals(find)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * 判断是否有值
     *
     * @param value
     * @return
     */
    public static boolean hasValue(Collection value) {
        if (value == null) {
            return false;
        }
        return !value.isEmpty();
    }




    public static boolean isNullOrEmpty(Collection value) {
        return !hasValue(value);
    }

    /**
     * 获取指定位置的元素
     *
     * @param list
     * @param index
     * @param <T>
     * @return
     */
    public static <T> T elementAt(Collection<T> list, Integer index) {
        var ret = list.stream().skip(index).findFirst();
        return ret.orElse(null);
    }

    /**
     * 判断是否包含
     *
     * @param list
     * @param item
     * @return
     */
    public static boolean contains(Collection list, Object item) {
        return indexOf(list, it -> it.equals(item)) > -1;
    }




    public static <T> List<T> asList(Collection<T> collection) {
        if (collection == null) {
            return null;
        }

        var ret = new ArrayList<T>();
        for (T t : collection) {
            ret.add(t);
        }
        return ret;
    }
    public static <T> List<T> asList(T[] array) {
        var ret = new ArrayList<T>();

        for (var i = 0; i < array.length; i++) {
            ret.add(array[i]);
        }

        return ret;
    }

    /**
     * 转为 List
     *
     * @param arrayObject
     * @param <T>
     * @return
     */
    public static <T> List<T> fromItems(T... arrayObject) {
        var ret = new ArrayList<T>();

        for (var i = 0; i < arrayObject.length; i++) {
            ret.add((T) arrayObject[i]);
        }

        return ret;
    }

    public static <T> List<T> asList(Enumeration enumeration) {
        if (enumeration == null) return null;

        var list = new ArrayList<T>();
        while (enumeration.hasMoreElements()) {
            var it = enumeration.nextElement();
            list.add((T) it);
        }
        return list;
    }



    /**
     * 获取集合最后一个
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T getLast(List<T> list) {
        return getLast(list, null);
    }

    public static <T> T getLast(List<T> list, Function<T, Boolean> callback) {
        if (list == null || list.size() == 0) {
            return null;
        }

        for (int i = list.size() - 1; i >= 0; i--) {
            var item = list.get(i);

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
    public static <T> List<T> unwindArray(List<T[]> list) {
        var ret = new ArrayList<T>();
        for (var i = 0; i < list.size(); i++) {
            var item = list.get(i);
            for (var j = 0; j < item.length; j++) {
                ret.add(item[j]);
            }
        }
        return ret;
    }

    /**
     * 展开 数组的数组, 如： [  [a,b], [c,d] ] ==> [a,b,c,d]
     */
    public static <T> List<T> unwind(List<List<T>> list) {
        var ret = new ArrayList<T>();
        for (var i = 0; i < list.size(); i++) {
            var item = list.get(i);
            for (var j = 0; j < item.size(); j++) {
                ret.add(item.get(j));
            }
        }
        return ret;
    }

    /**
     * 移除
     *
     * @param list
     * @param item
     * @param <T>
     * @return
     */
    public static <T> boolean removeOf(Collection<T> list, Object item) {
        Iterator itr = list.iterator();
        Object ret = null;
        while (itr.hasNext()) {
            ret = itr.next();
            if (Objects.equals(ret, item)) {
                itr.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Java 默认的 remove 在移除索引时有大坑， 推荐使用 removeAt
     *
     * @param index
     * @param <T>
     * @return
     * @link https://www.geeksforgeeks.org/remove-element-arraylist-java/
     */
    @SuppressWarnings("unchecked")
    public static <T> T removeAt(Collection<T> list, int index) {
        Iterator itr = list.iterator();
        var pos = -1;
        Object ret = null;
        while (itr.hasNext()) {
            pos++;
            ret = itr.next();
            if (pos == index) {
                itr.remove();
                break;
            }
        }
        return (T) ret;
    }

    /**
     * 移除最后一个
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T removeLast(Collection<T> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        var lastIndex = list.size() - 1;
        return removeAt(list, lastIndex);
    }


    /**
     * 求交集
     *
     * @param value1
     * @param value2
     * @return
     */
    public static <T extends Comparable<? super T>> Set<T> intersect(Collection<T> value1, Collection<T> value2) {
        if (value1 == null || value2 == null) {
            return new LinkedHashSet<>();
        }
        if (value1.isEmpty() || value2.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return value1.stream().filter(it -> value2.contains(it)).collect(Collectors.toSet());
    }


    /**
     * 第一个
     *
     * @param list
     * @param func
     * @param <T>
     * @return
     */
    public static <T> T firstOrNull(Collection<T> list, Function<T, Boolean> func) {
        if (list.size() == 0) {
            return null;
        }
        for (var it : list) {
            if (func.apply(it)) {
                return it;
            }
        }

        return null;
    }

}
