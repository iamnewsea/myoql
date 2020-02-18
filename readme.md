
# 发布

- 修改 /pom.xml version
- python sync_version.py
- 本地安装： python all_jar.py 
- 安装到远程仓库：python publish ${version}
- 本发安装单个Jar包： python install_jar.py -f ktext

# 使用包需要配置的变量

- server.upload.host 表示上传文件的文件服务 Host
- server.upload.logoSize 表示上传图片自动压缩的最大大小。
- server.filter.allowOrigins 表示跨域允许的域名。
- server.filter.ignore-log-urls 表示哪些URL在请求过程中添加 UsingScope.NoInfo 作用域。
- server.dataCenterId 表示服务所在的数据中心Id，生成雪花算法时使用。
- server.machineId 表示数据中心下属的服务器Id，生成雪花算法时使用。

# nginx 代理：

```
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
```



>开发环境

- JDK 1.8
- Intellij IDEA
- Kotlin
- Maven

>Intellij IDEA 插件
    
    Kotlin
    
>Intellij Idea环境配置：
    
    python build-base.py
    mvn-jar.cmd -f corp
    mvn-jar.cmd -f admin
 
## 发版：
    
    python scpshop.py
    或
    python scpadmin.py
 
# 项目特点

    可以 java + kotlin , 推荐 kotlin 更简单。
    mysql orm , 有实体生成器。支持枚举。
    mongo orm ,有实体生成器。支持枚举。
    redis 封装。
    mq 封装。
    mvc 封装，定义参数更灵活，action 参数分别从 url querystring, request body, header , cookie 中取。
    集成 swagger-ui
    集成日志
    封装Excel读取，导出
    封装文件上传
    封装 登录校验的请求 OpenAction
    MySql 主从数据库读取
    Mongo 多数据库数据源
    
# 教程

参见： doc 文件夹下相应部分。



