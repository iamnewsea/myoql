package nbcp.mongo

import nbcp.base.TestBase
import nbcp.base.comm.*
import nbcp.db.db
import nbcp.myoql.db.mongo.*
import nbcp.myoql.db.mongo.enums.PipeLineEnum
import nbcp.scope.JsonStyleScopeEnum
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class Test_Mongo_Query : TestBase() {

    @Test
    fun clone() {
        var query = db.morBase.sysLog.query()
            .where{ it.id.match_hasValue()}

        println(query.whereData.ToJson(JsonStyleScopeEnum.WithNull))
    }

    @Test
    fun test_query_where() {
        usingScope(LogLevelScopeEnum.debug) {
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
        usingScope(LogLevelScopeEnum.info) {
            db.morBase.sysAnnex.aggregate()
                .addPipeLine(
                    PipeLineEnum.addFields,
                    db.mongo.cond(db.morBase.sysAnnex.group match "digitalthread", "1", "0").As("u")
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

    @Test
    fun testElemMatch() {
        usingScope(LogLevelScopeEnum.info) {
            var where1 = JsonMap("\$gte" to 1, "\$lte" to 9)

            db.morBase.sysAnnex.query()
                .where_select_elemMatch_first_item { it.tags match_elemMatch where1 }
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
                            (MongoColumnName("\$item") match 3).toExpression()
                        )
            )

            db.morBase.sysAnnex.aggregate()
                .beginMatch()
                .where { it.tags match_elemMatch where1 }
                .endMatch()
                .addPipeLine(PipeLineEnum.project, project)
                .toList()

        }
    }
}