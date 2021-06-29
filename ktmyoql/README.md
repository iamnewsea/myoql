# kotlin extension

使用
```
<!-- https://mvnrepository.com/artifact/cn.dev8/ktext -->
<dependency>
    <groupId>cn.dev8</groupId>
    <artifactId>ktext</artifactId>
    <version>0.0.2</version>
</dependency>

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

