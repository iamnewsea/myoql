# mysql orm 说明

mysql 的使用方式和 mongo 差不多。
只解决简单的 CRUD , 对于复杂的查询, 尽量使用存储过程。


## 定义实体及生成实体

    1. 在 nbcp.db.mysql.entity 包下添加 mysql 实体。要求文档使用 @DbEntityGroup 注解，必须继承 IBaseDbEntity (接口无内容)
例子如下：其中 DbEntityGroup 定义了该实体所属的组（即模块）。DbUks 定义了该表的维一键。
如果多个列组成一个唯一键，字符串内使用 "," 分隔。s_user 定义了两个唯一键。
```
@DbEntityGroup("system")
@DbUks("id", "loginName")
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
       .where{ it.列名.character_length()  match 值 }
       .where{ it.列名  match 值 }
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
    var n = dbr.组名.实体名.update()
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
     
     var n = mor.组件.实体名.batchInsert()
        .add( 实体1 )
        .add( 实体2 )
        .exec()
其中：

    添加完成后， 可以通过 entity.id 获取添加后的id值。也可以通过 db.lastAutoId 获取最后插入数据库的Id值。

# 动态数据源

```
usingScope( DataSourceScope(DataSourceBuilder
    .create()
    .xxx()
    .build() ) ){
    
    var sql = RawQuerySqlClip("""
create table abc ( 
    `id` varchar(20) NOT NULL COMMENT '主键id',
    name varchar(30) null comment 'name', 
     PRIMARY KEY (`id`) USING BTREE ) 
) COMMENT='应用管理基本信息'
ENGINE=InnoDB
""" );

    sql.exec();
}
```