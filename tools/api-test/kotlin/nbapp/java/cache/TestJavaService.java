package nbapp.java.cache;

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
public class TestJavaService {
    /**
     * 查询时使用缓存。使用注解
     *
     * @param city
     * @return
     */
    @FromRedisCache(cacheSeconds = 3000, table = "tab1", joinTables = {}, groupKey = "city", groupValue = "#city")
    public List<Document> queryByCity(Integer city) {

        System.out.println("从数据库查询 city: " + city);
        List<Document> list = new LinkedList();

        //随便给个查询
        Document d1 = new Document();
        d1.put("name", "cache-注解");
        d1.put("city", city.toString());


        list.add(d1);

        return list;
    }


    @FromRedisCache(cacheSeconds = 3000, table = "tab1", joinTables = {}, groupKey = "id", groupValue = "#id")
    public List<Document> queryById(Integer id) {
        System.out.println("从数据库查询 id: " + id);
        List<Document> list = new LinkedList();

        //随便给个查询
        Document d1 = new Document();
        d1.put("name", "cache-注解");
        d1.put("id", id.toString());

        list.add(d1);

        return list;
    }

    /**
     * 使用注解缓存数据，如果不方便执行第一个函数，执行这个，也能测试缓存。
     *
     * @param city
     * @return
     */
    public List<Document> queryByCityOtherCondition(Integer city) {
        Document d2 = db.usingRedisCache("tab1", new String[]{}, "city", city.toString(), "code_cache_select" + city)
                .getJson(Document.class, () -> {

                    System.out.println("从数据库查询 city: " + city);
                    //随便给个查询
                    Document d1 = new Document();
                    d1.put("name", "cache-方法");
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
    @BrokeRedisCache(table = "tab1", groupKey = "city", groupValue = "#city")
    public void deleteAllCity(Integer city) {
        System.out.println();
    }


    /**
     * 使用代码，破坏缓存
     *
     * @param city
     */
    public void updateAllCity(Integer city) {
        new BrokeRedisCacheData("tab1", "city", city.toString()).brokeCache();
    }
}


