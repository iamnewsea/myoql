package nbapp.java.mvc;

import nbapp.java.cache.TestJavaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import nbcp.db.mongo.entity.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by CodeGenerator at 2021-04-11 23:42:19
 */

@RestController
@RequestMapping("/test/java")
public class CacheTestJavaController {

    @Autowired
    private TestJavaService testJavaService;

    @GetMapping("/cache")
    void cach_city1() throws InterruptedException {
        /**
         * 场景： 缓存数据，可以按 城市 进行分组隔离。 即：北京的缓存 和 南京的缓存 是隔离的，当有用户破坏了北京的缓存， 南京的缓存不受影响。
         *
         * 以下是按 city 和 id 两个维度进行隔离的例子
         */
        System.out.println("加载，city:1,2 ,id=3,4");
        System.out.println(testJavaService.queryByCity(1));
        System.out.println(testJavaService.queryByCityOtherCondition(1));
        System.out.println(testJavaService.queryById(3));
        System.out.println(testJavaService.queryById(4));
        System.out.println(testJavaService.queryByCity(2));
        System.out.println(testJavaService.queryByCityOtherCondition(2));
        System.out.println("加载完成----------------------------！");
        System.out.println("查从缓存中出!");
        System.out.println(testJavaService.queryByCity(1));
        System.out.println(testJavaService.queryByCityOtherCondition(1));
        System.out.println(testJavaService.queryById(3));
        System.out.println(testJavaService.queryById(4));
        System.out.println(testJavaService.queryByCity(2));
        System.out.println(testJavaService.queryByCityOtherCondition(2));
        System.out.println("查询完成----------------------------！");


        System.out.println("破 city:1");
        testJavaService.deleteAllCity(1);
        System.out.println("查，city:1，应该没有");
        System.out.println(testJavaService.queryByCity(1));
        System.out.println("查，id:3，应该没有");
        System.out.println(testJavaService.queryById(3));
        System.out.println("----------------------------！");

        System.out.println("查，city:2，从缓存中出!");
        System.out.println(testJavaService.queryByCity(2));
        System.out.println("----------------------------！");
    }
}


