package cn.dev8.util;

import lombok.var;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ListUtil {
    public static <T> List<T> unwindWithList(List<? extends Collection<T>> list) {
        var ret = new ArrayList<T>();
        for (var i = 0; i < list.size(); i++) {
            ret.addAll(list.get(i));
        }
        return ret;
    }

    public static <T> T elementAt(Collection<T> list, Integer index) {
        var iterator = list.iterator();
        var count = 0;
        while (iterator.hasNext()) {
            if (index == count) {
                return iterator.next();
            }
            count++;
            iterator.next();
        }
        return null;
    }

    public static <T> List<T> fromArray(Object arrayObject) {
        var ret = new ArrayList<T>();

        for (var i = 0; i < Array.getLength(arrayObject); i++) {
            ret.add((T) Array.get(arrayObject, i));
        }

        return ret;
    }


    public static <T> T removeLast(Collection<T> list) {
        if (list == null || list.size() == 0) {
            return null;
        }
        var lastIndex = list.size() - 1;
        return removeAt(list, lastIndex);
    }

    public static <T> int indexOf(T[] list, T item) {
        var index = -1;
        for (var it : list) {
            index++;
            if (it.equals(item)) {
                return index;
            }
        }
        return -1;
    }

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
}
