package nbcp.mvc.test;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RestController;
import nbcp.comm.*;
import nbcp.db.*;
import nbcp.db.cache.CacheForBroke;
import nbcp.db.cache.CacheForBrokeData;
import nbcp.db.cache.CacheForSelect;
import nbcp.db.cache.CacheForSelectData;
import nbcp.db.mongo.*;
import nbcp.db.mongo.entity.*;
import nbcp.web.*;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.time.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by CodeGenerator at 2021-04-11 23:42:19
 */

@RestController
@RequestMapping("/dev2")
public class DbConnectionAutoController2 {

    @GetMapping("/test1")
    @MyLogLevel(LogScope.info)
    @CacheForSelect(cacheSeconds = 300, table = "a", joinTables = {}, key = "city", value = "2")
    List<Document> test1() {
        List<Document> result = MyOqlMongo.aggregate(db.getMor_base().getSysAnnex())
                .addPipeLineRawString(PipeLineEnum.match, "{ \"group\" : \"lowcode\"} ")
                .addPipeLineRawString(
                        PipeLineEnum.group, ("{\n" +
                                "    _id: { 扩展名: \"##ext\" },\n" +
                                "    总数: { ##sum : 1 },\n" +
                                "    最小: { ##min: \"##size\" } ,\n" +
                                "    最大: { ##max: \"##size\" }\n" +
                                "}").replace("##", "$")
                )
                .addPipeLineRawString(PipeLineEnum.sort, "{ \"_id.扩展名\":1 }")
                .toMapList();

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
    List<Document> test3() {
        Document d2 = new CacheForSelectData(3000, "a", new String[]{}, "city", "3", "test3").usingRedisCache(Document.class, () -> {
            Document d1 = new Document();
            d1.put("OK", "dfdf");
            return d1;
        });

        return new LinkedList<Document>() {{
            add(d2);
        }};
    }

    @GetMapping("/test/d1")
    @CacheForBroke(table = "a", key = "city", value = "2")
    void test_d1() {

    }


    @GetMapping("/test/d2")
    void test_d2() {
        new CacheForBrokeData("a", "city", "3").brokeCache();
    }
}


