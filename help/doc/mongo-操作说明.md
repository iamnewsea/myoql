# 分库分表

优先级：

    上下文DataSource > 自定义拦截器 >  读写分离配置的默认DataSource

#  连接超时

连接字符串添加参数： ?maxIdleTimeMS=3000

> https://docs.mongodb.com/drivers/node/current/fundamentals/connection/

