package nbcp.myoql.freemarker

import nbcp.base.extend.FullName
import nbcp.base.utils.MyUtil
import nbcp.myoql.TestBase
import nbcp.myoql.code.generator.tool.CrudCodeGeneratorUtil
import nbcp.myoql.db.db
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileWriter

class FreeMarkerMongoTest : TestBase() {

    @Test
    fun genMongoMvcCrud() {
        var group = "base";
        db.morBase.getEntities().forEach {
            var entity = it;
            var entity_upper = MyUtil.getBigCamelCase(it.tableName)

            var fileNameAuto = File.separator + group + File.separator + entity_upper + "Controller.kt";

            var content = CrudCodeGeneratorUtil.genMongoMvcCrud(group, "nancal.mp", entity);
            println("================================")
            println(content)
            println("================================")
        }
    }


    @Test
    fun genVueCard() {
        var group = "base";
        db.morBase.getEntities().forEach {
            var entity = it;
            var entity_upper = MyUtil.getBigCamelCase(it.tableName)

            var fileNameAuto = File.separator + group + File.separator + entity_upper + "Controller.kt";

            var content = CrudCodeGeneratorUtil.genVueCard(entity::class.java);
            println("================================")
            println(content)
            println("================================")
        }
    }
}