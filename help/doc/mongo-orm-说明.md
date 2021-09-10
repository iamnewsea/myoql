# mongo orm 说明
    
## 引用实体
由于Mongo不能进行关联，使用空间换时间，所以在设计初期，需要对实体进行规划，做必要的冗余。
卡片也可以称为是 引用实体，用 data class 定义,可序列化,不能被继承.

比如:订单表使用产品信息,企业信息,购买者的信息.
    
    * 大部分情况,可以使用 IdName 表示最简单的卡片信息，页面显示name的时候，不用进行二次查询。
    * 购买者要引用更多的字段, 可以建立 引用单独的数据引用实体 WxUserInfoModel . 引用实体做为轻量级卡片,被其它实体引用. ShopUser 对外提借供 toWxUserInfo 方法.
        注意: 不能让 ShopUser 继承 WxUserInfoModel, 因为如果这样, order.createBy:WxUserInfoModel = ShopUser()的时候, createBy会是 ShopUser 的信息.
    * 
    
    
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
     
     var n = mor.组件.实体名.batchInsert()
        .add( 实体1 )
        .add( 实体2 )
        .exec()
其中：

    添加完成后， 可以通过 entity.id 获取添加后的id值。
    
##注意事项:
    - 尽量不要使用 save 方法，save方法有两个问题：
        1. 需要先查后保存。 
        2. 并发问题。保存时别人可能已经修改了数据。

## 聚合

```
db.mor_base.sysAnnex.aggregate()
    .addPipeLineRawString(PipeLineEnum.match, """ { "group" : "lowcode"} """.replace("##", "$"))
    .addPipeLineRawString(
        PipeLineEnum.group, """
{
    _id: { 扩展名: "##ext" },
    总数: { ##sum : 1 },
    最小: { ##min: "##size" } ,
    最大: { ##max: "##size" }
}
    """.replace("##", "$")
    )
    .addPipeLineRawString(PipeLineEnum.sort, """ { "_id.扩展名":1 } """)
    .toMapList()
```

生成的结果：
```
[{"总数":1,"最小":26821,"最大":26821,"id":{"扩展名":"abc"}},{"总数":5,"最小":5229,"最大":170276,"id":{"扩展名":"png"}}]
```

```
使用 db.runCommand( 生成的mongo语句 ) 执行
```
