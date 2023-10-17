#nbcp-entity项目介绍
    是品致信产品的实体层。包含以下实体：
        MongoDB
        Redis
        

##依赖
    nbcp 和 dev8 两个Java项目都依赖 pzx_entity
    
#使用方式
   
##linux
    在当前目录下执行： install_jar.sh
   
##windows
    在当前目录下执行：
    python install_jar.py

#Mongo实体添加流程
    1. 在 nbcp.db.mongo.entity 包下添加 Mongo实体。要求文档使用 @Document注解，必须继承 MongoBaseEntity(关键是 id 字段)
    2. 把新实体放到  nbcp.db.mongo.mor.kt 文件中的相应组下。 类型使用 ProxyRepository
    3. 执行 nbcp.PzxEntityMain.kt
    4. 把 nbcp.db.mongo.mor.src 的内容，Copy到 nbcp.db.mongo.mor.kt 中。
    5. 执行 install_jar.sh
    6. 签入代码。
    7. 打开 nbcp 或 dev8  签入项目的lib下的jar包。 就可以使用 pzx_entity的新实体了。

##实体规范
- 金额的单位是分， 数据类型是Int
- 时间：数据类型是Date (Long是备选，目前不用)
- id：主键，ObjectId（MySql用自定义短Id，为了分布式）
- name：名称
- code：编码
- createAt： 创建时间
- remark：备注
- createBy: 创建人
    
## Mongo实体
    所有Mongo实体，都有 @Document 及 @Id var id:ObjectId = ObjectId() 属性。
    内嵌实体没有 @Document 注解.

#mor使用语法

##Mongo实体查询语法：
    var list = mor.组名.实体名.Query()
       .where(mor.组名.实体名.列名  mongoEquals 值 )
       .where(mor.组名.实体名.列名  mongoLike 值 )
       .where(mor.组名.实体名.列名  mongoNotEquals 值 )
       .where(mor.组名.实体名.列名  mongoGreeterThanEquals 值 )
       .select(mor.组名.实体名.列名)
       .select(mor.组名.实体名.列名)
       .limit(跳过行数,获取行数)
       .orderBy(是否正序，mor.组名.实体名.列名)
       .toList(返回的实体类型)
    
    其中：
        查询实体的where条件，使用 and 进行连接。 
        mongoEquals 表示相等 
        mongolike 表示使用正则表达式的方式进行模糊匹配
        mongoNotEquals 表示不相等 
        mongoGreeterThanEquals 表示大于等于
        mongoLessThanEquals 表示小于等于
        
    或者：
    var entity = mor.组件.实体名.findById( Id字符串 或 ObjectId类型)
    或者:
    var entity = mor.组件.实体名.FindFirst( where条件 )
    
##Mongo更新
    var n = mor.组名.实体名.Update()
           .where(  where条件 ) 
           .set(mor.组名.实体名.列名 , 值 )
           .unset(mor.组名.实体名.列名 )
           .push(mor.组名.实体名.数组的列名 , 添加到数组的值 )
           .pull(mor.组名.实体名.数组的列名 , 删除数组的条件 )
           .exec();
    
    其中:
        unset 表示把该列置为 null
        push是向数组添加一项.
        pull是从数组中移除一项.
    
        也可以使用 mor.affectRowCount 表示影响的行数,来替代变量n
    或者:
    var n = mor.组件.实体名.updateById( Id字符串 或 ObjectId类型 )
   
##Mongo删除
    var n = mor.组件.实体名.deleteById( Id字符串 或 ObjectId类型 )
    
    
    或者:
    var n = mor.组件.实体名.delete( where条件 , where条件 )
    
##Mongo添加:
     mor.组件.实体名.doInsert( 实体 )
     mor.组件.实体名.doInsertAll( 实体集合 )

##注意事项:
    - 不要使用 save 方法，save方法有两个问题：
        1. 需要先查后保存。 
        2.并发问题。保存时别人可能已经修改了数据。

# mysql orm 说明

只解决简单的 CRUD , 对于复杂的查询, 请使用 Sql

# 缓存测试用例 

* s_user 表， 隔离键 corp_id

        insert id=1 corp=1   name=1a
        insert id=10 corp=1  name=1b
        insert id=11 corp=1  name=1c
        insert id=2 corp=2   name=2a
        insert id=20 corp=2  name=2b
        insert id=21 corp=2  name=2c
 
* 查

        select where id=1  => id缓存
        select where id=2  => id缓存
        select where corp=1 => corp缓存
        select where corp=2 => corp缓存
        select where id=1 and corp=1 => urk缓存
        select where id=2 and corp=2 => urk缓存
        select where name like '%a' => sql缓存
    
    
* update

        update where id=1  => 
            破坏 uk: id=1 ; rk: s_user; urk: id=1
        update where id=1 and corp=1 => 
            破坏 uk: id=1 ; rk: corp=1 ; urk: id=1
        update s_user where name likt '%a' =>
            破坏 s_user all cache
* insert

        insert id=12 corp=1 => 
            破坏 rk: corp=1;
        insert s_user select => 
            破坏 rk: s_user
* delete

        delete id=11 corp=1 => 
            破坏 uk:id=11; rk:corp=1; urk:id=11
        delete id=11 => 
            破坏 uk: id=11; rk: corp=*; urk:id=11
        delete corp=1 => 
            破坏 uk: id=*; rk:corp=1; urk:corp=1
        delete s_user where name like '%a'
            破坏 s_user all cache
            

