# sql操作
> 以mysql为例

## 分库分表
优先级：(?? 合理的优先级 ??)

    上下文DataSource > 自定义拦截器 >  读写分离配置的默认DataSource

### 灵活的上下文模式

    var ds_main = DataSourceBuilder.create().build() as HikariDataSource;
    ds_main.driverClassName = "";
    ds_main.jdbcUrl = "jdbc://";
    ds_main.username = "";
    ds_main.password = "";

    var ds_read = DataSourceBuilder.create().build() as HikariDataSource;
    ds_read.driverClassName = "";
    ds_read.jdbcUrl = "jdbc://";
    ds_read.username = "";
    ds_read.password = "";
    
    
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
            var ds_read = DataSourceBuilder.create().build() as HikariDataSource;
            ds_read.driverClassName = "";
            ds_read.jdbcUrl = "jdbc://";
            ds_read.username = "";
            ds_read.password = "";

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

### 默认 DataSource

主库是 hikari 数据源默认写法 ：spring.datasource.hikari
从库: spring.datasource.slave.hikari
    

    