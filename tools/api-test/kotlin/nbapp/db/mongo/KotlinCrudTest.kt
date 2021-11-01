package nbapp.db.mongo

import nbcp.comm.LogLevelScope
import nbcp.comm.MyLogLevel
import nbcp.db.cache.FromRedisCache
import nbcp.db.db
import nbcp.db.mongo.PipeLineEnum
import nbcp.db.mongo.aggregate
import org.bson.Document
import org.springframework.stereotype.Service

/**
 * Created by CodeGenerator at 2021-04-11 23:42:19
 */
@Service
@MyLogLevel(LogLevelScope.info)
class KotlinCrudTest {

    @FromRedisCache(table = "tab2", groupKey = "city", groupValue = "#city")
    fun cache_select(city: Int): MutableList<Document> {
        var result = db.mor_base.sysAnnex.aggregate()
                .addPipeLineRawString(PipeLineEnum.match, """ { "group" : "lowcode"} """.replace("##", "$"))
                .addPipeLineRawString(
                        PipeLineEnum.group, """
{
    _id: { 扩展名: "##ext" },
    总数: { ##sum : 1 },
    最小: { ##min: "##size" } ,
    最大: { ##max: "##size" }
}
            """.replace("##", "$")
                )
                .addPipeLineRawString(PipeLineEnum.sort, """ { "_id.扩展名":1 } """)
                .toMapList()

        /**
         * 生成的语句：
         *
        {
        aggregate: "sysAnnex",
        pipeline: [{$match: { "group" : "lowcode"} },{$group:
        {
        _id: { 扩展名: "$ext" },
        总数: { $sum : 1 },
        最小: { $min: "$size" } ,
        最大: { $max: "$size" }
        }
        },{$sort: { "_id.扩展名":1 } }] ,
        cursor: {} }

         * 返回的结果：
        [{"总数":1,"最小":26821,"最大":26821,"id":{"扩展名":"abc"}},{"总数":5,"最小":5229,"最大":170276,"id":{"扩展名":"png"}}]
         */

        return result;
    }
}