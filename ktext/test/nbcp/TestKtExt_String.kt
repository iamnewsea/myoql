package nbcp

import ch.qos.logback.classic.Level
import nbcp.comm.*
import nbcp.utils.*
import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.IdName
import nbcp.db.IdUrl
import org.junit.Test
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TestKtExt_String : TestBase() {

    @Test
    fun test_Tokenizer_Csv_format() {

        var txt = """"id,device_id,latitude,longitude,speed,angle,altitude,satellite_,time
"28098","9",31.246717000000000,121.593050000000005,0.020000000000000,0.000000000000000,14.300000000000001,"12",2020-05-29 09:56:44
"28099","9",31.246717000000000,121.593050000000005,0.020000000000000,0.000000000000000,14.100000000000000,"12",2020-05-29 09:56:45
        """

        txt.Tokenizer({it == ',' || it == '\n'}, arrayOf(
                TokenQuoteDefine('"','"','"') )
        ).forEachIndexed { index, s ->
            println("[" + index.toString() + "]:" + s)
        }
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
    fun test_type_convert() {
        println("2020-08-05 13:17:18".AsLocalDateTime().toString())
    }
}