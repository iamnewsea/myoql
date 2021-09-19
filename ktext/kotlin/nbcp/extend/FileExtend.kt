@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.utils.RecursionUtil
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.time.LocalDateTime

/**
 * 文件的全路径。
 */
val File.FullName: String
    get() {
        return this.path
    }


/**
 * 递归列出所有文件及子文件。
 */
@JvmOverloads
fun File.ListRecursionFiles(
    pathCallback: ((String) -> Boolean)? = null,
    fileCallback: ((String) -> Boolean)? = null
): List<String> {
    var ret = mutableSetOf<String>()
    if (this.isFile) {
        if (fileCallback?.invoke(this.FullName) ?: true) {
            ret.add(this.FullName)
        }
        return ret.toList();
    }

    if (pathCallback?.invoke(this.FullName) ?: true) {
        //遍历子
        this.listFiles().map {
            ret.addAll(it.ListRecursionFiles(pathCallback, fileCallback))
        }
    }
    return ret.toList();
}

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
    BufferTailReader(this).use { reader ->
        while (true) {
            var line = reader.readLine()
            if (line == null) {
                return reader.currentLineIndex;
            }

            if (action(line, reader.currentLineIndex) == false) {
                return reader.currentLineIndex;
            }
        }
        return reader.currentLineIndex;
    }

}


fun File.ReadHeadLines(action: ((String, Int) -> Boolean)): Int {
    if (this.isFile == false) return -1;

    BufferedReader(InputStreamReader(FileInputStream(this), const.utf8)).use { reader ->
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
        return index;
    }
}


/**
 * 文件缓存器,不缓存空字符串
 * 简单代替Redis，但不移除缓存。
 */
fun File.withCacheObject(cacheSeconds: Int, contentAction: () -> String): String {
    var exists = this.exists();
    var lines = listOf<String>()
    if (exists) {
        lines = this.readLines(const.utf8);

        var validate = validate@{
            if (lines.any() == false) {
                return@validate false
            }

            var line0 = lines.get(0);
            var line0_sect = line0.split("~")
            if (line0_sect.size != 2) {
                return@validate false;
            }

            var createAt = line0_sect[0].AsLocalDateTime();
            if (createAt == null) {
                return@validate false
            }

            if ((LocalDateTime.now() - createAt).seconds > cacheSeconds) {
                return@validate false
            }

            return@validate true;
        }

        exists = validate()
    } else {
        if (this.parentFile.exists() == false) {
            this.parentFile.mkdirs()
        }
    }


    if (exists) {
        return lines.subList(1, lines.size).joinToString(const.line_break)
    }

    var content = contentAction();
    if (content.isNullOrEmpty()) {
        return content;
    }

    "file:cache:${this.FullName}".withLock(5) {
        var line0 = LocalDateTime.now().AsString() + "~" + cacheSeconds
        this.writeText(line0 + const.line_break + content, const.utf8);
    }

    return content;
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
@JvmOverloads
fun File.FilterLines(
    matchLines: Int,
    extCount: Int = 0,
    filter: List<String> = emptyList(),
    not: List<String> = emptyList(),
    tail: Boolean = true
): List<String> {
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