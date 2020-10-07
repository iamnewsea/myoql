# sql操作
> 以mysql为例

## 分库分表
myoql 通过定义不同的数据源来分库。 
> 以 product 为例,在配置文件中定义多个数据源，如 ds1,ds2,ds3 :


    db.sql.bindTableName2Database("product",ds1);
    
    
    
    
