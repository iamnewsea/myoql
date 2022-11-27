# kotlin extension

使用
```
<!-- https://mvnrepository.com/artifact/cn.dev8/ktbase -->
<dependency>
    <groupId>cn.dev8</groupId>
    <artifactId>ktbase</artifactId>
    <version>0.0.2</version>
</dependency>

```


## mysql or mariadb

### mysql
```
driver-class-name: com.mysql.cj.jdbc.Driver
url: jdbc:mysql://
```

# mariadb
```
driver-class-name: org.mariadb.jdbc.Driver
url: jdbc:mariadb://
```


## 通过Json快速生成 Es 实体
```js
var p = {};

Object.keys(p).map(it=>{ 
var ret = [];
var value = p[it];
if( value.Type == "string") value = '""';
else if ( value.Type == "array") {
    ret.push('@Define("""{"type":"text","index":"true","boost":"1","analyzer":"ik_max_word","search_analyzer":"ik_max_word"}""")');
    value = 'arrayOf<String>()';
}

ret.push( "var " + it + "=" + value) ;
return ret.join("\n");

}).join("\n")
```

## 缓存规则：

```
Redis禁用keys命令，使用 scan 命令代替。
Redis缓存键规则：类似Http协议的网址格式(不能使用#,因为#是SPEL表达式的标志)
sc：查询主表/连接表/排序/？查询主表隔离键=查询主表隔离键值@md5

整个key去除空格
特殊字符：  :(冒号) / ? = @ , 对每个值中出现的特殊字符，使用全角字符替换。
如果Sql长度小于32位，直接使用Sql，不使用md5
如下，sql部分是示意，结果按 md5 算：
Key1:  sc:user/city/corp/?id=1@select user.*, corp.name as 公司名称 , city.name as 城市名称 from user join corp on(user.corp_id = corp.id) join city on ( user.city_id = city.id) order by user.age
Key2:  sc:corp/city/user/?id=1@select corp.*, city.name as 城市名称, user.name as 法人姓名 from corp join city on (corp.city_id = city.id) join user on (corp.faren_id = user.id) order by corp.name

分析：Key1,key2的区别是 查询表顺序，主表固定在前，连接表按顺序在主表后。

Key3:  sc:corp/city/user/?city_id=1@md5
Key4:  sc:corp/city/user/?city_id=2@md5
Key5:  sc:corp/city/user/?city_id=3@md5
Key6:  sc:corp/city/user/?city_id=3@md5
Key7:  sc:corp/city@md5
Key8:  sc:corp/city?id=1@md5

分析：Key3-6按城市做隔离，每个Sql的查询条件不同，即md5不同， 如下Sql的破坏性不同：
对city任意 update,insert,delete操作,即不具有隔离键的where条件，会破坏如下key(正则表达)： 
	sc:city/* ：清除所有对 city 做主表查询的缓存项
*/city/* ：清除所有Join city表的缓存项。(key1-6)
	update corp where city_id=3 会破坏：
		sc:corp/*\\?city_id=3@* : 清除所有对corp做主表查询，且隔离键是 city_id = 3的项。
		sc:corp/*[^?]*  : 清除没有隔离键的项
		sc:corp/*\\? [^非city_id]=* : 清除其它 对 corp 做主表查询，且隔离键不是 city_id 的项。
		*/corp/* : 清除所有join corp 表的缓存项。

注意Redis对非字符串的正则不支持断言，如 非city_id=010, 需要使用： *\\?[^0][^1][^0]=* 格式

```