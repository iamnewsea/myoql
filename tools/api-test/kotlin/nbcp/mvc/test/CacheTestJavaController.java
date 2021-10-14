package nbcp.mvc.test;

import nbapp.service.cache.AppCacheTestJavaService;
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
    private AppCacheTestJavaService appCacheTestJavaService;

    @GetMapping("/cache")
    void cach_city1() throws InterruptedException {
        System.out.println("加载，city:1,2 ,id=3,4");
        System.out.println(appCacheTestJavaService.cache_select(1));
        System.out.println(appCacheTestJavaService.code_cache_select(1));
        System.out.println(appCacheTestJavaService.cache_select_id(3));
        System.out.println(appCacheTestJavaService.cache_select_id(4));
        System.out.println(appCacheTestJavaService.cache_select(2));
        System.out.println(appCacheTestJavaService.code_cache_select(2));
        System.out.println("加载完成----------------------------！");
        System.out.println("查从缓存中出!");
        System.out.println(appCacheTestJavaService.cache_select(1));
        System.out.println(appCacheTestJavaService.code_cache_select(1));
        System.out.println(appCacheTestJavaService.cache_select_id(3));
        System.out.println(appCacheTestJavaService.cache_select_id(4));
        System.out.println(appCacheTestJavaService.cache_select(2));
        System.out.println(appCacheTestJavaService.code_cache_select(2));
        System.out.println("查询完成----------------------------！");


        System.out.println("破 city:1");
        appCacheTestJavaService.cache_broke(1);
        System.out.println("查，city:1，应该没有");
        System.out.println(appCacheTestJavaService.cache_select(1));
        System.out.println("查，id:3，应该没有");
        System.out.println(appCacheTestJavaService.cache_select_id(3));
        System.out.println("----------------------------！");

        System.out.println("查，city:2，从缓存中出!");
        System.out.println(appCacheTestJavaService.cache_select(2));
        System.out.println("----------------------------！");
    }
}


