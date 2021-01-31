# sql操作
> 以mysql为例

## 读写分离

### 配置

* 主数据库的配置和默认一样
* 从库：把主库 spring.datasource 改为 spring.datasource-slave 即可。hikari 连接池同样生效。
* 两个 bean :  dataSource（主） , slave 

### 优先级：(?? 合理的优先级 ??)
    上下文DataSource > 自定义拦截器 >  读写分离配置的默认DataSource

### 灵活的上下文模式

    var ds_main = SpringUtil.getBean<DataSource>()
    var ds_read = SpringUtil.getBean<DataSource>("slave") 
    
    
    usingScope(ds_read){
        dbr.system.product.queryById(1).toEntity();
    }

    usingScope(ds_main){
        dbr.system.product.updateById(1).set{ it.name to "abc"}.exec();
    }

### 定义的拦截器

    @Component
    public class MysqlDynamicDataSource:ISqlDataSource{
        override run(tableName:String,isRead:Boolean):DataSource?{
            var ds_main = SpringUtil.getBean<DataSource>()
            var ds_read = SpringUtil.getBean<DataSource>("slave") 

            if(tableName.IsIn("product","order") ){
                if(isRead == false){
                    return ds_read;
                }
                else{
                    return ds_main;
                }
            }
            return null;
        }
    }

# 分表


    