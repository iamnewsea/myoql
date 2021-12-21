@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm


fun MutableList<*>.RemoveRange(startIndex: Int, endIndex: Int) {
    var startIndexValue = startIndex
    var endIndexValue = endIndex


    if (startIndexValue > endIndexValue) {
        var tmp = startIndexValue
        startIndexValue = endIndexValue
        endIndexValue = tmp
    }

    for (i in startIndexValue..endIndexValue) {
        this.removeAt(startIndexValue)
    }
}

fun <T> MutableList<T>.Swap(index1: Int, index2: Int) {
    var tmp = this[index1];
    this[index1] = this[index2]
    this[index2] = tmp;
}


//把最里面的数据收集起来。
//inline fun <reified T> Collection<Array<T>>.Unwind(): Array<T> {
//    var list = mutableListOf<T>()
//    this.forEach {
//        it.forEach {
//            list.add(it)
//        }
//    }
//    return list.toTypedArray()
//}

//把最里面的数据收集起来。
inline fun <reified T> Collection<Collection<T>>.Unwind(): List<T> {
    var list = mutableListOf<T>()
    this.forEach {
        it.forEach {
            list.add(it)
        }
    }
    return list
}

/**
 * [startIndex,endIndex)
 * @param startIndex 包含startIndex
 * @param endIndex 不包含endIndex
 *
 *  [].Slice(0,-1) = 不要最后一个
 *  [].Slice(-1)   = 只取最后一个
 */
@JvmOverloads
fun <T> Array<out T>.Slice(startIndex: Int, endIndex: Int = Int.MIN_VALUE): List<T> {
    var endIndexValue = endIndex;
    if (endIndexValue == 0) return listOf()
    var startIndexValue = startIndex

    if (startIndexValue >= this.size) {
        return listOf()
    }
    // -10 从右边取10位.
    if (startIndexValue < 0) {
        startIndexValue = this.size + startIndexValue
    }
    if (startIndexValue < 0) {
        startIndexValue = 0
    }

    if (endIndexValue == Int.MIN_VALUE) {
        return this.slice(startIndexValue..(this.size - 1))
    }

    if (endIndexValue <= 0) {
        endIndexValue = this.size + endIndexValue
    }

    if (endIndexValue < startIndexValue) return listOf()

    if (endIndexValue > this.size) {
        endIndexValue = this.size
    }

    return this.slice(startIndexValue..(endIndexValue - 1))
}

/**
 * [startIndex,endIndex)
 */
@JvmOverloads
inline fun <reified T> Collection<T>.Slice(startIndex: Int, endIndex: Int = Int.MIN_VALUE): List<T> {
    return this.toTypedArray<T>().Slice(startIndex, endIndex)
}


inline fun <T> Iterator<T>.Filter(predicate: (T) -> Boolean): MutableList<T> {
    var list = mutableListOf<T>()

    for (element in this) {
        if (predicate(element)) {
            list.add(element);
        }
    }

    return list;
}

/*
比较两个数组的内容是否相同, 去除相同数据进行比较 .如:
[1,1,2] .equalArrayContent( [1,2,2] )  == true
 */
@JvmOverloads
fun Array<*>.EqualArrayContent(other: Array<*>, withIndex: Boolean = false): Boolean {
    return this.toList().EqualArrayContent(other.toList(), withIndex);
}


/*
比较两个数组的内容是否相同, 去除相同数据进行比较 .如:
[1,1,2] .equalArrayContent( [1,2,2] )  == true
 */
@JvmOverloads
fun Collection<*>.EqualArrayContent(other: Collection<*>, withIndex: Boolean = false): Boolean {
    if (this.size == 0 && other.size == 0) return true;
    else if (this.size == 0) return false;
    else if (other.size == 0) return false;

    if (withIndex) {
        this.forEachIndexed { index, item ->
            var otherItem = other.elementAt(index);
            if (item != otherItem) {
                return false;
            }
        }
        return true;
    }

    var one = this.distinct();
    var two = other.distinct();


    if (one.size != two.size) return false;
    return one.intersect(two).size == this.size;
}

inline fun <T> Iterable<T>.Skip(skipNumber: Int): List<T> {
    var ret = mutableListOf<T>();
    if (this.any() == false) return ret;

    var index = 0;
    for (element in this) {
        index++;
        if (index <= skipNumber) {
            continue;
        }

        ret.add(element)
    }
    return ret;
}

// A - B == B - A == 0
//inline fun <T> Collection<out T>.ContentSame(other: Collection<out T>): Boolean {
//    if (this.size != other.size) return false;
//    if (this.minus(other).size != 0) return false;
//    if (other.minus(this).size != 0) return false;
//    return true;
//}
//
//// A - B == B - A == 0
//inline fun <T> Array<out T>.ContentSame(other: Array<out T>): Boolean {
//    return this.toList().ContentSame(other.toList());
//}

/**
 * forEach的增强版.
 * @return  遍历完所有元素返回 true, 如果没有对象，返回 true.
 */
inline fun <T> Iterable<T>.ForEachExt(action: (T, Int) -> Boolean): Boolean {
    if (!this.any()) return true;
    var index = -1;
    while (true) {
        index++;
        if (index >= this.count()) {
            break;
        }

        var element = this.elementAt(index);
        if (action(element, index) == false) {
            return false;
        }
    }
    return true;
}

//forEach的增强版
inline fun <T> Array<out T>.ForEachExt(action: (T, Int) -> Boolean): Boolean {
    return this.toList().ForEachExt(action);
}

/**
 * 小于0，插入第1位。
 * 大于长度，插入最后一位。
 */
fun <T> MutableList<T>.InsertBefore(index: Int, item: T) {
    if (index < 0) {
        this.add(0, item);
        return;
    }
    if (index >= this.size) {
        this.add(item);
    }
    this.add(index, item);
}

/**
 * 小于0，插入第1位。
 * 大于长度，插入最后一位。
 */
fun <T> MutableList<T>.InsertAfter(index: Int, item: T) {
    if (index < 0) {
        this.add(0, item);
        return;
    }
    if (index >= this.size - 1) {
        this.add(item);
    }

    this.add(index + 1, item);
}

/**
 * 获取相同数据的索引，返回 map ,key 是相同数据的 第一个数据的位置索引，  value 是相同数据的 第二个数据的位置索引。
 */
inline fun <T, R> Iterable<T>.IntersectIndexes(other: Collection<R>, equalFunc: (T, R) -> Boolean): Map<Int, Int> {
    if (this.any() == false) return mapOf();
    if (other.any() == false) return mapOf();

    var listIndex = mutableMapOf<Int, Int>()
    var index = -1;

    for (item in this) {
        index++;

        var index2 = -1;
        for (item2 in other) {
            index2++;

            if (equalFunc(item, item2)) {
                listIndex.set(index, index2);
                break;
            }
        }
    }

    return listIndex
}

/**
 * 减法
 */
inline fun <T, R> Iterable<T>.Minus(other: Collection<R>, equalFunc: (T, R) -> Boolean): List<T> {
    if (this.any() == false) return this.toList();
    if (other.any() == false) return this.toList();

    var toRemoveIndex = this.IntersectIndexes(other, equalFunc).keys

    return this.filterIndexed { index, _ -> toRemoveIndex.contains(index) == false };
}

/**
 * 获取相同数据部分
 */
inline fun <T, R> Iterable<T>.Intersect(other: Collection<R>, equalFunc: (T, R) -> Boolean): List<T> {
    if (this.any() == false) return this.toList();
    if (other.any() == false) return this.toList();

    var indexList = this.IntersectIndexes(other, equalFunc).keys

    return this.filterIndexed { index, _ -> indexList.contains(index) };
}

/**
 * 把数据分隔为 DiffData
 */
inline fun <T, R> Iterable<T>.SplitDiffData(other: Collection<R>, equalFunc: (T, R) -> Boolean): DiffData<T, R> {
    return DiffData.load(this, other, equalFunc);
}

/**
 * 把数据分组
 */
inline fun <reified T> Collection<T>.SplitGroup(operatorItem: (T) -> Boolean): List<List<T>> {
    var ret = mutableListOf<List<T>>()

    var prevIndex = 0;
    var index = -1;
    var subSect: List<T>;

    while (true) {
        index++;
        if (index >= this.size) {
            subSect = this.Slice(prevIndex);
            if (subSect.any()) {
                ret.add(subSect)
            }
            break;
        }

        if (operatorItem(this.elementAt(index))) {
            subSect = this.Slice(prevIndex, index);
            if (subSect.any()) {
                ret.add(subSect)
            }
            prevIndex = index + 1;
        }
    }

    return ret;
}


/**
 * 把某些项移到前面。
 */
fun <T> Collection<T>.MoveToFirst(itemCallback: (T) -> Boolean): List<T> {
    var firstPart = mutableListOf<T>();
    var normalPart = mutableListOf<T>()

    this.forEach {
        if (itemCallback(it)) {
            firstPart.add(it);
        } else {
            normalPart.add(it);
        }
    }

    return firstPart.asReversed() + normalPart;
}

/**
 * 修改数据
 */
fun <T> MutableList<T>.ModifyListMoveToFirst(itemCallback: (T) -> Boolean): List<T> {
    var length = this.size;
    for (i in 0 until length) {
        var item = this.get(i);
        if (itemCallback(item)) {
            this.Swap(0, i);
        }
    }

    return this;
}

/**
 *
 */
fun Collection<*>.getObject(index: Int): Any? {
    return this.elementAt<Any?>(index)
}

fun List<*>.getListObject(): List<Any?> {
    return this as List<Any?>
}

fun Array<*>.getArrayObject(): Array<Any?> {
    return this as Array<Any?>
}

fun List<Any?>.resetListItemType(clazz: Class<*>) {
    var list = this as ArrayList<Any?>
    for (i in list.indices) {
        val itemValue = list.getObject(i)
        if (itemValue == null) {
            continue;
        }

        list.set(i, itemValue.ConvertType(clazz))
    }
}


fun List<out String>.findWithIgnoreCase(item: String): String? {
    return this.firstOrNull { it VbSame item }
}