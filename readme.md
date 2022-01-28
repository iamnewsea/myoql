
# 发布

- 修改 /pom.xml version
- python sync_version.py
- 本地安装： python install_jar.py  
- 安装到远程仓库：python publish ${version}
- 本发安装单个Jar包： python install_jar.py -f ktbase


    > 设置版本： mvn versions:set -DnewVersion=1.0.6-SNAPSHOT
    > 发布到私服：mvn clean deploy -Dmaven.test.skip=true -e           -U -am -P nancal-dev -pl ktweb 
    > 发布到私服：mvn clean deploy -Dmaven.test.skip=true -e -U -am -P nancal-snapshots -pl ktweb 

    > mvn clean package -Dmaven.test.skip=true  -P release
    > mvn clean install -Dmaven.test.skip=true  -P release
    > mvn clean deploy -Dmaven.test.skip=true -P release

## 发布到私服
maven的 setting.xml 增加

servers
```
<server>
   <id>nancal-dev</id>
   <username>admin</username>
   <password>xxx</password>
</server>

<server>
   <id>nancal-snapshots</id>
   <username>admin</username>
   <password>xxx</password>
</server>
```



pom.xml 文件设置：(以后想办法把这一段移到 setting.xml 中)
```
<profile>
    <id>nancal-dev</id>
    <distributionManagement>
        <repository>
            <id>nancal-dev</id>
            <url>http://nexus.nancal.com/repository/maven-releases/</url>
        </repository>
    </distributionManagement>
</profile>
```

本地打包：python install_jar.py
发布到私服：mvn clean deploy -Dmaven.test.skip=true -P nancal-dev

## 版本

Major.Minor.Fix
稳定版本=Major.Minor.Max


# 使用包需要配置的变量

- app.upload.host 表示上传文件的文件服务 Host
- app.filter.allow-origins 表示跨域允许的域名。
- app.filter.ignore-log-urls 表示哪些URL在请求过程中添加 UsingScope.NoInfo 作用域。
- app.filter.headers 表示跨域允许的 headers
- app.filter.html-path 表示可以访问的静态内容，不做任何模板解析。
- app.dataCenter-id 表示服务所在的数据中心Id，生成雪花算法时使用。
- app.machine-id 表示数据中心下属的服务器Id，生成雪花算法时使用。

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



