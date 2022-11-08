package nbcp.base

import nbcp.base.comm.*
import nbcp.myoql.model.DataTable
import org.junit.jupiter.api.Test

class TestKtExt_DataTable : TestBase() {

    @Test
    fun test_Csv_load() {

        val txt = """id,device_id,latitude,longitude,speed,angle,altitude,satellite_,time
"28098","9",31.246717000000000,121.593050000000005,0.020000000000000,0.000000000000000,14.300000000000001,"12",2020-05-29 09:56:44
"28099","9",31.246717000000000,121.593050000000005,0.020000000000000,0.000000000000000,14.100000000000000,"12",2020-05-29 09:56:45
        """

        val dt = DataTable.loadFromCsv(txt, JsonMap::class.java);

        println(dt.ToJson())
    }
}