package nbcp.base.extend

import org.slf4j.Logger
import nbcp.base.comm.DiffData
import nbcp.base.utf8
import nbcp.base.utils.BufferTailReader
import java.util.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import kotlin.collections.LinkedHashMap
import nbcp.base.utils.MyUtil
import java.lang.reflect.Field
import java.nio.charset.Charset
import java.time.*
import java.time.temporal.Temporal
import java.io.*
import java.lang.reflect.Modifier


/**
 * Created by udi on 17-4-3.
 */

//fun Int.IsIn(vararg ts: Int): Boolean {
//    for (i in ts) {
//        if (i == this) return true;
//    }
//    return false;
//}


fun <T> T.IsIn(vararg values: T): Boolean {
    return this.IsIn(null, *values);
}

fun <T> T.IsIn(equalFunc: ((T, T) -> Boolean)? = null, vararg values: T): Boolean {
    for (i in values) {
        if (equalFunc == null) {
            if (this == i) return true;
        } else {
            if (equalFunc(this, i)) return true;
        }
    }

    if (values.size == 1 && values[0] is Collection<*>) {
        for (i in values[0] as Collection<*>) {
            if (equalFunc == null) {
                if (this == i) return true;
            } else {
                if (equalFunc(this, i as T)) return true;
            }
        }
    }

    return false;
}

//http://blog.csdn.net/doctor_who2004/article/details/50449561
//fun Date.ToISODate(): String {
//    val format = SimpleDateFormat(
//            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//    format.calendar = GregorianCalendar(
//            SimpleTimeZone(0, "GMT"))
//    return format.format(this);
//}

//fun Date.ToFormatString(format: String = ""): String {
//    var format = format;
//    if (format.HasValue == false) {
//        format = "yyyy-MM-dd HH:mm:ss";
//    }
//
//    var dateFormat = SimpleDateFormat(format)
////    format.calendar = GregorianCalendar(
////            SimpleTimeZone(0, "GMT"))
//    return dateFormat.format(this);
//}
//
//fun String.ToDateFromISODateFromat(): Date {
//    val format = SimpleDateFormat(
//            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//    format.calendar = GregorianCalendar(
//            SimpleTimeZone(0, "GMT"))
//    return format.parse(this);
//}






interface IDisposeable {
    fun dispose();
}


enum class LogScope : IDisposeable {
    TaskLog, //分组到 task 里.
    NoInfo,  //不记录 INFO
    NoLog;   //不记录任务日志

    override fun dispose() {
    }


}

private val _scopes = ThreadLocal.withInitial { Stack<IDisposeable>() }

val scopes: Stack<IDisposeable>
    get() = _scopes.get();

/**
 * 用法:
 * class LoggerScope:IDisposeable{
 *
 * }
 *
 * using(
 */
fun <T> using(init: IDisposeable, body: () -> T): T {
    scopes.push(init);
    try {
        return body();
    } finally {
        init.dispose();
        if (scopes.isEmpty() == false) {
            scopes.pop()
        }
    }
}
//fun HttpServletResponse.SetJsonContent(vararg json:Pair<String,Any>){
//    this.contentType = "application/json;charset=utf-8";
//    this.writer.write(json.toMap().ToJson())
//}

//fun <T> Supplier<T>.BeginGet(error: ((Exception) -> Unit)? = null): T? {
//    try {
//        var ret: T? = null;
//        Executors.newCachedThreadPool().execute { ret = this.get(); }
//        return ret;
//    } catch (e: Exception) {
//        if (error != null) {
//            error(e);
//        }
//    }
//    return null
//}





//inline fun <reified K, reified V> Map<K, V>.ToLinkedHashMap(): LinkedHashMap<K, V> {
//    var map = linkedMapOf<K, V>()
//    this.forEach {
//        map[it.key] = it.value;
//    }
//    return map;
//}



fun Serializable.ToSerializableByteArray(): ByteArray {
    ByteArrayOutputStream().use { byteOutStream ->
        ObjectOutputStream(byteOutStream).use { objStream ->
            objStream.writeObject(this);
            objStream.flush();

            return byteOutStream.toByteArray();
        }
    }
}

fun ByteArray.ToSerializableObject(): Serializable {
    ByteArrayInputStream(this).use { byteInStream ->
        ObjectInputStream(byteInStream).use { objStream ->
            return objStream.readObject() as Serializable
        }
    }
}

//infix fun String.lang(englishMessage: String): String {
//    var lang = HttpContext.request.getAttribute("lang")?.toString() ?: "cn"
//    if (lang == "en") return englishMessage;
//    return this;
//}


//fun <T> Array<out T>.slice (startIndex: Int, endIndex: Int = Int.MIN_VALUE): List<T> {
//    return listOf();
//}

//fun String.slice (startIndex: Int, endIndex: Int = 0): String {
//    return ""
//}


/**
 * 文件的全路径。
 */
val File.FullName: String
    get() {
        return this.path
    }


//infix fun Date.addDays(value: Int): Date {
//    return Date(this.time + value * MyUtil.OneDayMilliseconds)
//}
//
//
////北京时间的日期
//val Date.bjDay: Date
//    get () {
//        return Date(Math.floor(this.time / MyUtil.OneDayMilliseconds.toDouble()).toLong() * MyUtil.OneDayMilliseconds - MyUtil.OneHourMilliseconds * 8);
//    }



/**
 * 读取文件最后N行
 *
 * 根据换行符判断当前的行数，
 * 使用统计来判断当前读取第N行
 *
 * PS:输出的List是倒叙，需要对List反转输出
 *
 * @param file 待文件
 * @param action 读取的每一行的回调，两个参数： 行内容，倒序的行号。
 * @return Int，返回最后的倒序行号
</String> */
fun File.ReadTailLines(action: ((String, Int) -> Boolean)): Int {
    if (this.isFile == false) return -1;

    var reader = BufferTailReader(this)

    while (true) {
        var line = reader.readLine()
        if (line == null) {
            return reader.currentLineIndex;
        }

        if (action(line, reader.currentLineIndex) == false) {
            return reader.currentLineIndex;
        }
    }

    reader.close();
    return reader.currentLineIndex;
}


fun File.ReadHeadLines(action: ((String, Int) -> Boolean)): Int {
    if (this.isFile == false) return -1;

    var reader = BufferedReader(InputStreamReader(FileInputStream(this), utf8))

    var index = -1;
    while (true) {
        index++;
        var line = reader.readLine()
        if (line == null) {
            return index;
        }

        if (action(line, index) == false) {
            return index;
        }
    }

    reader.close();
    return index;
}

/**
 * @param startFlag 开始块包含的标志,返回 null 表示没找到。 返回 int 表示开始行的偏移量。 0 表示该行， 1表示下一行。 不能是负数
 * @param endFlag  结束块包含的标志,返回 null 表示没找到。 返回 int 表示开始行的偏移量。 0 表示该行， -1 表示上一行。 不能是正数。
 * @param action 第一参数是每一块的数据，第二个参数是倒排的块索引。返回值： null表示忽略该块。 true该块合法， false 停止
 * @return
 */
//fun File.readLastSects(splitSect: (String, MutableList<String>) -> Boolean, action: ((List<String>, Int) -> Boolean?)): List<String> {
//    var sects = mutableListOf<String>()
//    var sect = mutableListOf<String>()
//    var sectIndex = -1;
//
//    var actionResult: Boolean? = null;
//
//    this.readLastLine { line, index ->
//
//        if (index > 1000) {
//            return@readLastLine false;
//        }
//
//        sect.add(line);
//
//        if (splitSect(line, sect)) {
//            if (sect.any() == false) {
//                return@readLastLine true;
//            }
//
//            sectIndex++
//            sect.reverse();
//
//
//            actionResult = action(sect, sectIndex);
//            if (actionResult == true) {
//                sects.add(sect.joinToString("\n"))
//            }
//
//            if (actionResult == false) {
//                return@readLastLine false;
//            }
//            sect = kotlin.collections.mutableListOf();
//        }
//        return@readLastLine true;
//    }
//
//    if (sect.any()) {
//        sectIndex++
//        sect.reverse();
//        sects.add(sect.joinToString("\n"))
//    }
//
//    return sects;
//}

/**
 * 过滤行，读取每一行。
 */
fun File.FilterLines(matchLines: Int, extCount: Int = 0, filter: List<String> = emptyList(), not: List<String> = emptyList(), tail: Boolean = true): List<String> {
    var matchLines = matchLines;
    if (matchLines == 0) {
        if (filter.any()) {
            matchLines = 10;
        } else {
            matchLines = 100;
        }
    }


    var lines = sortedMapOf<Int, String>()

    var tailExtLines = mutableMapOf<Int, String>()

    var hitNextCount = 0;
    var matchedLines = 0;
    var action: ((String, Int) -> Boolean) = action@{ line, index ->

        if (extCount > 0) {
            tailExtLines.set(index, line);
            if (tailExtLines.size > extCount) {
                tailExtLines.remove(tailExtLines.keys.first());
            }
        }

        if (hitNextCount > 0) {
            hitNextCount--;
        }

        if (matchedLines < matchLines && isMatched(line, filter, not)) {
            matchedLines++;

            hitNextCount = extCount;
            lines.set(index, line);
            lines.putAll(tailExtLines)
        } else if (hitNextCount > 0) {
            lines.set(index, line);
        }

        if (matchedLines >= matchLines && hitNextCount == 0) {
            return@action false;
        }

        return@action true;
    }

    if (tail) {
        this.ReadTailLines(action)
    } else {
        this.ReadHeadLines(action);
    }

    //补全回车
    var retLines = lines.toList().toMutableList();

    var prevLine = -1;
    var prevText = "";
    var index = retLines.size;

    while (true) {
        index--;
        if (index < 0) {
            break;
        }

        var line = retLines[index].first;
        var txt = retLines[index].second;

        if (prevLine == -1) {
            prevLine = line;
            prevText = txt;
            continue
        }

        if (prevLine - 1 != line) {
            if (retLines[index].second != "" && prevText != "") {
                retLines.InsertAfter(index, 0 to "")
            }
        }

        prevLine = line;
        prevText = txt;
    }

    return retLines.map {
        if (it.first == 0) {
            return@map it.second;
        }
        return@map (it.first + 1).toString() + ": " + it.second
    };
}

private fun isMatched(line: String, filter: List<String>, not: List<String>): Boolean {
    if (filter.any() || not.any()) {
        if (line.any() == false) {
            return false;
        }
    } else {
        return true;
    }

    var ret = true;
    if (filter.any()) {
        ret = filter.ForEachExt { f, index ->
            if (line.contains(f, true) == false) {
                return@ForEachExt false;
            }
            return@ForEachExt true;
        }
    }
    if (ret == false) return false;

    if (not.any()) {
        ret = not.ForEachExt { f, index ->
            if (f == "") {
                return@ForEachExt line.trim() != "";
            }
            if (line.contains(f, true)) {
                return@ForEachExt false;
            }
            return@ForEachExt true;
        }
    }

    return ret;
}

fun Temporal.BetweenSeconds(nextTime: Temporal): Int {
    return Duration.between(this.AsLocalDateTime(), nextTime.AsLocalDateTime()).getSeconds().AsInt();
}

fun Temporal.BetweenDays(nextTime: Temporal): Int {
    return (Duration.between(this.AsLocalDateTime(), nextTime.AsLocalDateTime()).getSeconds() / MyUtil.OneDaySeconds).AsInt();
}


//包含只读属性. 即: 可能没有 setField 方法.
//val Class<*>.properties: Array<Field>
//    get() {
//        var ret = mutableListOf<Field>()
//
//        var method_names = this.methods.filter {
//            var isStatic = (it.modifiers and Modifier.STATIC != 0)
//            if (isStatic) return@filter false;
//            return@filter true;
//        }.map { it.name }
//
//        this.allFields.filter {
//            var isStatic = (it.modifiers and Modifier.STATIC != 0)
//            if (isStatic) return@filter false;
//
//            if (method_names.any { m ->
//                        var name = it.name.slice(0, 1).toUpperCase() + it.name.slice(1)
//                        return@any m.equals("get" + name)
//                    }) {
//                return@filter true;
//            }
//
//            return@filter false;
//        }.forEach {
//            it.isAccessible = true;
//            ret.add(it)
//        }
//        return ret.toTypedArray();
//    }

fun Logger.Error(err: Throwable) {
    this.error(err.message, err);
}

//返回非空的描述
val Throwable.Detail: String
    get() = this.message.AsString(this::class.java.simpleName)

fun InputStream.GetHtmlString(): String {
    return String(this.readBytes(), utf8)
}

//通过内存复制对象.
fun <T : Serializable> T.CloneObject(): T {
    var obj = this;
    //写入字节流
    var out = ByteArrayOutputStream();
    var obs = ObjectOutputStream(out);
    obs.writeObject(obj);
    obs.close();

    //分配内存，写入原始对象，生成新对象
    var ios = ByteArrayInputStream(out.toByteArray());
    var ois = ObjectInputStream(ios);

    //返回生成的新对象
    var cloneObj = ois.readObject() as T;
    ois.close();
    return cloneObj;
}
