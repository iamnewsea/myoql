package nbcp.mongo

import ch.qos.logback.classic.Level
import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.mongo.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class Test_Mongo_Query : TestBase() {

    @Test
    fun clone() {
        var d = MongoColumnName("ok")
        var d2 = d.CloneObject();
        println(d2.toString())
    }
    @Test
    fun test_query_datetime() {
        var start = LocalDateTime.now().minusHours(1)
        var end = LocalDateTime.now().plusHours(1);
        var query = db.mor_base.sysLog.query()
            .where { it.createAt match_between (start to end) }
            .where { it.level match 8 }
            .whereOr({ it.msg match_like "df" }, { it.tags match "df" })

        var d = db.mongo.getMergedMongoCriteria(query.whereData)
        var e = db.mongo.getCriteriaFromDocument(d.toDocument());
        println(d.toDocument().toJson())
        println(e.criteriaObject.toJson())
    }

    @Test
    fun testCond() {
        usingScope(LogLevelScope.info) {
            db.mor_base.sysAnnex.aggregate()
                .addPipeLine(
                    PipeLineEnum.addFields,
                    db.mongo.cond(db.mor_base.sysAnnex.group match "digitalthread", "1", "0").As("u")
                )
                .beginMatch()
                .where { it.ext match "png" }
                .endMatch()
                .addPipeLine(PipeLineEnum.sort, JsonMap("u" to 1))
                .limit(0, 2)
                .toList()
                .forEach {
                    println(it.ToJson())
                }

        }
    }
}