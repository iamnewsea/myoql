package nbcp.mvc.dev2

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import nbcp.comm.*
import nbcp.db.*
import nbcp.db.cache.CacheForBroke
import nbcp.db.cache.CacheForBrokeData
import nbcp.db.cache.CacheForSelect
import nbcp.db.cache.CacheForSelectData
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.*
import nbcp.web.*
import org.bson.Document
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestMapping
import javax.servlet.http.HttpServletRequest
import java.time.*

/**
 * Created by CodeGenerator at 2021-04-11 23:42:19
 */
@Api(description = "数据连接", tags = arrayOf("DbConnection"))
@RestController
@RequestMapping("/dev")
class DbConnectionAutoController {

    @GetMapping("/test1")
    @MyLogLevel(LogScope.info)
    @CacheForSelect(300, "a", arrayOf(), "city", "2")
    fun test1(): MutableList<Document> {
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


    @GetMapping("/test2")
    @MyLogLevel(LogScope.info)
    fun test3(): MutableList<Document> {
        var d1 = CacheForSelectData(3000, "a", arrayOf(), "city", "3", "test3").usingRedisCache(Document::class.java) {
            var d1 = Document();
            d1.put("OK", "dfdf")
            return@usingRedisCache d1;
        }

        return mutableListOf(d1)
    }

    @GetMapping("/test/d1")
    @CacheForBroke("a", "city", "2")
    fun test_d1() {

    }


    @GetMapping("/test/d2")
    fun test_d2() {
        CacheForBrokeData("a", "city", "3").brokeCache();
    }
}


