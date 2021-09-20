package nbapp.service.cache;

import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Service;
import nbcp.comm.*;
import nbcp.db.*;
import nbcp.db.cache.BrokeRedisCache;
import nbcp.db.cache.BrokeRedisCacheData;
import nbcp.db.cache.FromRedisCache;
import nbcp.db.cache.FromRedisCacheData;
import nbcp.db.mongo.*;
import nbcp.db.mongo.entity.*;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by CodeGenerator at 2021-04-11 23:42:19
 */

@Service
@MyLogLevel(LogLevel.INFO)
public class AppCacheTestJavaService {
    /**
     * 查询时使用缓存。使用注解
     *
     * @param city
     * @return
     */
    @FromRedisCache(cacheSeconds = 300, table = "tab1", joinTables = {}, key = "city", value = "#city")
    public List<Document> cache_select(Integer city) {
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


    /**
     * 使用注解缓存数据，如果不方便执行第一个函数，执行这个，也能测试缓存。
     *
     * @param city
     * @return
     */
    public List<Document> code_cache_select(Integer city) {
        Document d2 = new FromRedisCacheData(3000, "tab1", new String[]{}, "city", city.toString(), "code_cache_select" + city)
                .usingRedisCache(Document.class, () -> {
                    Document d1 = new Document();
                    d1.put("name", "cache-test");
                    d1.put("city", city.toString());
                    return d1;
                });

        return new LinkedList<Document>() {{
            add(d2);
        }};
    }


    /**
     * 使用注解，破坏缓存
     *
     * @param city
     */
    @BrokeRedisCache(table = "tab1", key = "city", value = "#city")
    public void cache_broke(Integer city) {
        System.out.println();
    }


    /**
     * 使用代码，破坏缓存
     *
     * @param city
     */
    public void code_cache_broke(Integer city) {
        new BrokeRedisCacheData("tab1", "city", city.toString()).brokeCache();
    }
}


