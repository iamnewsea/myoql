package nbapp.mvc.dev2

import nbcp.comm.*
import nbcp.db.*
import nbcp.db.cache.BrokeRedisCache
import nbcp.db.cache.BrokeRedisCacheData
import nbcp.db.cache.FromRedisCache
import nbcp.db.cache.FromRedisCacheData
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.*
import nbcp.web.*
import org.bson.Document
import org.springframework.boot.logging.LogLevel
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.time.*

/**
 * Created by CodeGenerator at 2021-04-11 23:42:19
 */
@Service
@MyLogLevel(LogLevelScope.info)
class AppCacheTestKotlinService {

    @FromRedisCache(300, "tab2", arrayOf(), "city", "#city")
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


    fun code_cache_select(city: Int): MutableList<Document> {
        var d1 = FromRedisCacheData(3000, "tab2", arrayOf(), "city", city.toString(), "自定义Sql:${city}")
            .usingRedisCache(Document::class.java) {
                var d1 = Document();
                d1.put("name", "cache-test");
                d1.put("city", city.toString());
                return@usingRedisCache d1;
            }

        return mutableListOf(d1)
    }


    @BrokeRedisCache("tab2", "city", "#city")
    fun cache_broke(city: Int) {
    }


    fun code_cache_broke(city: Int) {
        BrokeRedisCacheData("tab2", "city", city.toString()).brokeCache();
    }
}


