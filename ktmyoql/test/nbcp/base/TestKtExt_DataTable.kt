package nbcp.base

import nbcp.TestBase
import nbcp.comm.*
import nbcp.model.DataTable
import org.junit.Test

class TestKtExt_DataTable : TestBase() {

    @Test
    fun test_Csv_load() {

        var txt = """id,device_id,latitude,longitude,speed,angle,altitude,satellite_,time
"28098","9",31.246717000000000,121.593050000000005,0.020000000000000,0.000000000000000,14.300000000000001,"12",2020-05-29 09:56:44
"28099","9",31.246717000000000,121.593050000000005,0.020000000000000,0.000000000000000,14.100000000000000,"12",2020-05-29 09:56:45
        """

        var dt = DataTable.loadFromCsv(txt, JsonMap::class.java);

        println(dt.ToJson())
    }
}