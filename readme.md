
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



# 项目说明
    entity 项目是实体层，包含：
    * 枚举定义
    * mongodb 实体。
    * mysql 实体。
    * redis 实体。
    * mq 实体。

从实际使用来说， 一个系统要分为多个模块，模块下包含实体及接口。 

    * 一级项目是一个系统，一个微服务
    * 一个系统下面分为多个模块。
    * 一个模块下有多个实体。
    
在该系统下，会对实体按模块进行分组。 

## 项目依赖

* ktext 
* ktmyoql
* ktmvc

地址：https://gitee.com/imnewsea/myoql
使用最新版的方法：

    * 获取代码
    * 执行 python all_jar.py 部署到本地


##使用方式
   会有部分 python3 脚本辅助。 需要安装以下组件：

    * JDK 1.8+
    * Maven
    * Python3
    
## 实体规范
- 金额的单位是分， 数据类型是Int
- 时间：数据类型是LocalDate,LocalDateTime,LocalTime
- id：主键，ObjectId（MySql用自定义短Id，为了分布式）
- name：名称
- code：编码
- createAt： 创建时间
- remark：备注
- createBy: 创建人

    
# mongo orm 说明

## 定义实体及生成实体

    1. 在 nbcp.db.mongo.entity 包下添加 Mongo实体。要求文档使用 @Document注解，必须继承 IMongoDocument (关键是有 id 字段)
例子如下：其中 DbEntityGroup 定义了该实体所属的组（即模块）。
```
@Document
@DbEntityGroup("system")
data class SysCity(
        var code: Int = 0,
        var name: String = "",
        var fullName: String = "",
        var level: Int = 0,
        var lng: Float = 0F, //经度
        var lat: Float = 0F, //纬度
        var pinyin: String = "",
        var telCode: String = "",
        var postCode: String = "",
        var pcode: Int = 0
) : IMongoDocument()
```
    2. 执行 test : dev.tool.gen_mor，系统生成实体元数据到 shop-orm 项目： nbcp.db.mongo.mor_tables.kt
    3. 执行生成脚本： python build_base.py ,本地部署 shop-entity,shop-orm,shop-web-base
    6. 签入代码。在admin项目就可以使用实体了。
    
## Mongo 查询：

    var list = mor.组名.实体名.query()
       .where{ it.列名  match 值 }
       .where{ it.列名  match_like 值 }
       .where{ it.列名  match_ne 值 }
       .where{ it.列名  match_gte 值 }
       .select{ it.列名 }
       .select{ it.列名 }
       .limit(跳过行数,获取行数)
       .orderByDesc{ it.列名 }
       .toList()
    
其中：查询实体的 where 条件，运算符有以下：
 
    match 表示相等 
    match_like 表示使用正则表达式的方式进行模糊匹配
    match_not_equal 表示不相等 
    match_pattern 表示正则
    match_type 表示判断类型
    match_gte 表示大于等于
    match_lte 表示小于等于
    match_exists 表示判断存在
    match_in 表示包含
    match_notin 表示不包含
        
其中：执行返回结果可以是以下：

    toList 返回实体列表
    toList(类型) 返回指定的实体列表。
    toEntity 返回类型，单条
    toEntity(类型) 返回指定的实体类型。
    exists 判断是否存在满足条件的集合。
    count 返回满足条件的行数
    toListResult 返回列表数据，如果是第1页会带总页数。
    
## Mongo 更新
    var n = mor.组名.实体名.update()
           .where{ it.列名 math 值 }
           .set{ it.列名 to 值 }
           .unset{ it.列名 }
           .push{ it.数组的列名 to 添加到数组的值 }
           .pull({ it.数组的列名 , 删除数组的条件 , 删除数组的条件2 }
           .exec();
    
其中:

        unset 表示删除该字段
        push是向数组添加一项.
        pull是从数组中移除一项.
    
   
## Mongo 删除:

    var n = mor.组件.实体名.deleteById( Id字符串 或 ObjectId类型 ).exec()
   
或者:

    var n = mor.组件.实体名.delete()
        .where{ it.列名  match 值 }
        .exec()
    
## Mongo 添加:

     var n = mor.组件.实体名.doInsert( 实体 ) 
     
     var n = mor.组件.实体名.insert()
        .insert( 实体1 )
        .insert( 实体2 )
        .exec()
其中：

    添加完成后， 可以通过 entity.id 获取添加后的id值。
    
##注意事项:
    - 尽量不要使用 save 方法，save方法有两个问题：
        1. 需要先查后保存。 
        2. 并发问题。保存时别人可能已经修改了数据。

# mysql orm 说明

mysql 的使用方式和 mongo 差不多。
只解决简单的 CRUD , 对于复杂的查询, 尽量使用存储过程。


## 定义实体及生成实体

    1. 在 nbcp.db.mysql.entity 包下添加 mysql 实体。要求文档使用 @DbEntityGroup 注解，必须继承 IBaseDbEntity (接口无内容)
例子如下：其中 DbEntityGroup 定义了该实体所属的组（即模块）。SqlUks 定义了该表的维一键。
如果多个列组成一个唯一键，字符串内使用 "," 分隔。s_user 定义了两个唯一键。
```
@DbEntityGroup("system")
@SqlUks("id", "loginName")
//用户
data class s_user(
        @SqlAutoIncrementKey
        var id: Int = 0,
        //姓名
        var name: String = "",
        //用户名
        var loginName: String = "",
        //员工号
        var code: String = "",
        //手机号
        var mobile: String = "",

        //是否是盘点员
        var isChecker: Boolean = false,

        //是否是管理员
        var isAdmin: Boolean = false,

        var isDisabled: Boolean = false,

        //用于Api交互
        var token: String = "",

        //最后更新时间
        var updateAt: LocalDateTime = LocalDateTime.now(),
        var createAt: LocalDateTime = LocalDateTime.now()
) : IBaseDbEntity()
```
    2. 执行 test : dev.tool.gen_dbr，系统生成实体元数据到 shop-orm 项目： nbcp.db.mysql.dbr_tables.kt
    3. 执行生成脚本： python build_base.py ,本地部署 shop-entity,shop-orm,shop-web-base
    6. 签入代码。在admin项目就可以使用实体了。

  
## Sql 查询：

    var list = dbr.组名.实体名.query()
       .where{ it.列名  match 值 }
       .where{ it.列名  like 值 }
       .where{ it.列名  match_ne 值 }
       .where{ it.列名  match_gte 值 } 
       .select{ it.列名 }
       .select{ it.列名 }
       .limit(跳过行数,获取行数)
       .orderByDesc{ it.列名 }
       .toList()
    
其中：查询实体的 where 条件，运算符有以下：
 
    match 表示相等 
    like 表示使用正则表达式的方式进行模糊匹配
    match_not_equal 表示不相等 
    match_gte 表示大于等于
    match_lte 表示小于等于
    match_between 表示 大于等于，并且 小于
    match_in 表示包含
    match_notin 表示不包含
        
其中：执行返回结果可以是以下：

    toList 返回实体列表
    toList(类型) 返回指定的实体列表。
    toEntity 返回类型，单条
    toEntity(类型) 返回指定的实体类型。
    exists 判断是否存在满足条件的集合。
    count 返回满足条件的行数
    toListResult 返回列表数据，如果是第1页会带总页数。
    
## Sql 更新
    var n = mor.组名.实体名.update()
           .where{ it.列名 math 值 }
           .set{ it.列名 to 值 }
           .exec(); 
   
## Sql 删除:

    var n = mor.组件.实体名.deleteById( Id字符串 或 ObjectId类型 ).exec()
   
或者:

    var n = mor.组件.实体名.delete()
        .where{ it.列名  match 值 }
        .exec()
    
## Sql 添加:

     var n = mor.组件.实体名.doInsert(实体) 
     
     var n = mor.组件.实体名.insert()
        .insert( 实体1 )
        .insert( 实体2 )
        .exec()
其中：

    添加完成后， 可以通过 entity.id 获取添加后的id值。也可以通过 db.lastAutoId 获取最后插入数据库的Id值。
    

    


