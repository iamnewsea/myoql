package nbcp.myoql.mongo

import nbcp.base.comm.JsonMap
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.enums.LogLevelScopeEnum
import nbcp.base.extend.*
import nbcp.base.extend.usingScope
import nbcp.myoql.TestBase
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.aggregate
import nbcp.myoql.db.mongo.base.MongoColumnName
import nbcp.myoql.db.mongo.enums.PipeLineEnum
import nbcp.myoql.db.mongo.extend.toDocument
import nbcp.myoql.db.mongo.extend.toExpression
import nbcp.myoql.db.mongo.query
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class Test_Mongo_Query : TestBase() {

    @Test
    fun clone() {
        var query = db.morBase.sysLog.query()
            .where { it.id.mongoHasValue() }

        println(query.whereData.ToJson(JsonStyleScopeEnum.WITH_NULL))
    }

    @Test
    fun test_query_where() {
        usingScope(LogLevelScopeEnum.DEBUG) {
            db.morBase.sysLog.query()
                .apply {
                    this.where(
                        """
var v = this.ext;
return  this.tags && this.tags.some(function(it) { return it == v } ) 
"""
                    )
                }
                .toList()
                .apply {
                    println(this.size)
                }
        }
    }

    @Test
    fun test_query_datetime() {
        var start = LocalDateTime.now().minusHours(1)
        var end = LocalDateTime.now().plusHours(1);
        var query = db.morBase.sysLog.query()
            .where { it.createAt mongoUntil (start to end) }
            .where { it.level mongoEquals 8 }
            .linkOr({ it.msg mongoLike "df" }, { it.tags mongoEquals "df" })

        var d = db.mongo.getMergedMongoCriteria(query.whereData)
        var e = db.mongo.getCriteriaFromDocument(d.toDocument());
        println(d.toDocument().toJson())
        println(e.criteriaObject.toJson())
    }

    @Test
    fun testCond() {
        usingScope(LogLevelScopeEnum.INFO) {
            db.morBase.sysAnnex.aggregate()
                .addPipeLine(
                    PipeLineEnum.ADD_FIELDS,
                    db.mongo.cond(db.morBase.sysAnnex.group mongoEquals "digitalthread", "1", "0").As("u")
                )
                .beginMatch()
                .where { it.ext mongoEquals "png" }
                .endMatch()
                .addPipeLine(PipeLineEnum.SORT, JsonMap("u" to 1))
                .limit(0, 2)
                .toList()
                .forEach {
                    println(it.ToJson())
                }

        }
    }

    @Test
    fun testElemMatch() {
        usingScope(LogLevelScopeEnum.INFO) {
            var where1 = JsonMap("\$gte" to 1, "\$lte" to 9)

            db.morBase.sysAnnex.query()
                .whereSelectElemMatchFirstItem { it.tags mongoElemMatch where1 }
                .whereData
                .apply {
                    println(this.ToJson())
                }

            /*
    {
        $project: {
         tags: {
            $filter: {
               input: "$tags",
               as: "item",
               cond:  {
                   $eq: ["$$item.score" ,  1  ]
               }
            }
         }
      }
    }

             */
            var project = JsonMap(
                "tags" to
                        db.mongo.filter(
                            "\$tags", "item",
                            (MongoColumnName("\$item") mongoEquals 3).toExpression()
                        )
            )

            db.morBase.sysAnnex.aggregate()
                .beginMatch()
                .where { it.tags mongoElemMatch where1 }
                .endMatch()
                .addPipeLine(PipeLineEnum.PROJECT, project)
                .toList()

        }
    }
}