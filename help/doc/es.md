# elasticsearch

## 配置

spring.elasticsearch.rest.uris=http://ip:9200

组件使用低级Api，进行Orm封装。
程序需要引用
```
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-client</artifactId>
    <version>7.6.2</version>
</dependency>
```

