package nbcp.base.extend

import nbcp.base.TestBase
import nbcp.base.comm.const
import nbcp.base.enums.AlignDirectionEnum
import nbcp.base.utils.CipherUtil
import nbcp.base.utils.FileUtil
import org.junit.jupiter.api.Test
import java.awt.Font
import java.io.File

class TestKtExt_String : TestBase() {

    @Test
    fun test_Tokenizer_Csv_format() {

        var txt = """"id,device_id,latitude,longitude,speed,angle,altitude,satellite_,time
"28098","9",31.246717000000000,121.593050000000005,0.020000000000000,0.000000000000000,14.300000000000001,"12",2020-05-29 09:56:44
"28099","9",31.246717000000000,121.593050000000005,0.020000000000000,0.000000000000000,14.100000000000000,"12",2020-05-29 09:56:45
        """

        txt.Tokenizer(
                { it == ',' || it == '\n' }, arrayOf(
                TokenQuoteDefine('"', '"', '"')
        )
        ).forEachIndexed { index, s ->
            println("[" + index.toString() + "]:" + s)
        }
    }

    @Test
    fun testRegEdit() {

        var txt = """
ENV JAVA_OPTS -xx:178
ENV JAVA_OPTS -xx:178
ENV JAVA_OPTS -xx:178

aaa
"""

        var t = txt.regexMultiLineEdit(
                Regex("^ENV\\s+(JAVA_OPTS)\\s+", RegexOption.MULTILINE),
                "0,-1",
                "+=>",
                " -XX:256 \$1"
        );

        println(t)

        println("!!!!!!!!!!!!!!!!!!!!")
        println("!!!!!!!!!!!!!!!!!!!!")
    }

    @Test
    fun test_Tokenizer_Kotlin() {

        var basePath = Thread.currentThread().contextClassLoader.getResource("./").path.split("/target/")[0];

        var path = File(FileUtil.resolvePath(basePath, "./kotlin"));

        var _walk_all_path: ((File) -> Boolean)? = null;
        var walk_all_path: ((File) -> Boolean) = walk@{ file ->
            if (file.isFile) {
                println(file.FullName)
                var txt = file.readText(const.utf8);

                txt.Tokenizer(
                        { it == ',' || it == '\n' }, arrayOf(
                        TokenQuoteDefine('"', '"', '"')
                )
                ).forEachIndexed { index, s ->
                    println("[" + index.toString() + "]:" + s)
                }

                return@walk false;
            } else {
                file.listFiles().all { return@all _walk_all_path!!(it) }
                return@walk true;
            }
            return@walk true;
        }

        _walk_all_path = walk_all_path;

        _walk_all_path(path);


    }

    @Test
    fun test_Tokenizer_sql_format() {

        var txt = """
select * from[pub space].tab where`id`  like'% 1 %'  
        """

        txt.Tokenizer().forEachIndexed { index, s ->
            println("[" + index.toString() + "]:" + s)
        }
    }


    @Test
    fun abc2() {
        var f =  Font("宋体", Font.PLAIN, 12);
        var fm = sun.font.FontDesignMetrics.getMetrics(f);
        println(fm.charWidth('A'))
        println(fm.charWidth('a'))
        println(fm.charWidth('─'))
        println(fm.charWidth('│'))
        println(fm.charWidth('中'))
        println(fm.charWidth('╔'))
    }

    @Test
    fun test_3des() {
        var key1 = CipherUtil.get3desKey();
        println("key:" + key1)
        var result1 =
                CipherUtil.decrypt3des("println(ConvertToLocalDateTime(\"20111203101530\",DateTimeFormatter.", key1)
        println("结果:" + result1);
        println("原文:" + CipherUtil.decrypt3des(result1, key1))


        var key = CipherUtil.get3desKey();
        println("key:" + key)
        var result = CipherUtil.encrypt3des("println(ConvertToLocalDateTime(\"20111203101530\",DateTimeFormatter.", key)
        println("结果:" + result);
        println("原文:" + CipherUtil.decrypt3des(result, key))
    }

    @Test
    fun test_radix() {
        var v = System.currentTimeMillis()
        println(v)
        println(v.toString(36))
        println(java.lang.Long.valueOf(v.toString(36), 36))
    }


    @Test
    fun test_cn() {
        println("afd".hasCn())
        println("吕".hasCn())
        println("从".hasCn())
        println("111从22".hasCn())
    }
}