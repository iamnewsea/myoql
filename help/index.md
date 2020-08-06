# ktmyoql 使用手册

## 配置使用的数据库

@SpringBootApplication 注解中指定 exclude 为以下对象:

- MongoAutoConfiguration::class , 排除 Mongo数据库配置
- DataSourceAutoConfiguration::class , 排除 MySql数据库配置
- RedisAutoConfiguration::class , 排除 Redis数据库配置

