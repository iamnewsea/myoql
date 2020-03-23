package nbcp.base.utils

import nbcp.base.extend.AsInt
import nbcp.comm.*

import java.io.File
import java.io.RandomAccessFile
import java.io.Reader
import java.nio.charset.Charset

/**
 * 从末尾读取流
 */
class BufferTailReader(var file: File, var charset: Charset = utf8) {
    private var pos = -1L;
    private var reader: RandomAccessFile
    var currentLineIndex = 0;

    private var n: Byte = 10;
    private var r: Byte = 13;

    init {
        this.reader = RandomAccessFile(file, "r")
        this.pos = reader.length()
        reader.seek(this.pos);

//        while (true) {
//            var con = reader.read()
//            if (con < 0) {
//                break;
//            }
//
//            var by = con.toByte()
//            if (by == r) {
//                currentLineIndex++;
//
//                //判断下面是不是 \n
//                con = reader.read()
//                if( con < 0){
//                    break;
//                }
//                by = con.toByte()
//                if (by != n) {
//                    reader.seek(reader.filePointer - 1);
//                    continue;
//                }
//            } else if (by == n) {
//                currentLineIndex++;
//            }
//        }
//
//        //额外加1, 是因为在 readLine 时, 总会 -1
//        currentLineIndex++;
    }

    fun close() {
        this.reader.close();
    }

    fun readLine(): String? {
        //如果是0，代表是空文件，直接返回空结果
        if (pos < 0L) {
            return null;
        }

        var lineBytes = mutableListOf<Byte>()
        while (true) {
            pos--;
            if (pos < 0L) {
                break;
            }

            reader.seek(pos)
            var char = reader.readByte();


            if (char == n) {
                if (pos != 0L) {
                    reader.seek(pos-1);
                     char = reader.readByte();
                    if ( char == r) {
                        pos--;
                    }
                }
                break;
            }

            if (char == r) {
                break;
            }

            lineBytes.add(char);
        }

        currentLineIndex--;
        //保存结果
        return lineBytes.reversed().toByteArray().toString(charset);
    }
}