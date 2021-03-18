# kotlin extension

扩展了基础的用法，包括以下对象：

表述：
- data:? 表示 data字段 是任意类型，可空。
- data: Any 表示 data字段 是任意类型，不可空。
- data: []  表示 data字段 是Array类型。
- data:<?> 表示 data 字段是 可空Any泛型
- data:String 表示 data 字段是 字符串
- data:String? 表示 data 字段是 可空字符串

## 结构
- JsonMap , key是String的Map
- JsonResult ，普通接口返回对象。 msg , cause
- ApiResult<> ： JsonResult, 普通接口返回对象。 添加 data:<?>
- ListResult<> ： JsonResult ，列表返回对象。添加 total:Int, data :[], value:?
- BufferTailReader 从末尾读取流
- SnowFlake 雪花算法
- SysConst : 系统常量， utf8
- IdName , CodeName ， IdCodeName ， KeyValueString ， IdUrl , IdNameUrl ,IdUrlMsg

## Util

- JarClassUtil ，解析Jar包内的Class
- CodeUtil , 雪花算法生成 code 
- HttpUtil , Ajax
- ImageUtil , 缩放图片
- JsUtil , Js编码，解码
- Md5Util
- MyUtil
- RecursionUtil 递归
- SpringUtil ， 动态获取 Bean
- SSLClient , https ajax
- VerifyCodeUtil 验证码

## 扩展
扩展方法尽量使用 大驼峰， 避免与主类方法冲突。

- Any 
    - IsIn
    - ToJson
    - FromJson
    - ConvertJson 通过 Json 快速转换对象。
    - CloneObject 克隆 对象。
    - ToSerializableByteArray
    - ToSerializableObject
    - 

- Class
    - AllFields 所有字段，包括父类的。
    - FindField 查找字段，包括父类的。
    - IsSimpleType 
    - IsBooleanType
    - IsListType
    - IsStringType
    - IsNumberType
    - GetEnumList
    - GetEnumNumberField
    - GetEnumStringField
- String
    - VbSame
    - NewString
    - Repeat 
    - HasValue
    - IfHasValue
    - IsNumberic
    - IsMatch
    - Tokenizer 分词器,按单词把字符串分开。
    - RemoveComment 去除注释
    - PatchHostUrl
    - Remove
    - Slice
    - ToTab
    - Xml2Json
    - MatchPattern
    - 

- Number
    - ToReadableAmountValue 转为可读性金额。
    - ToBitPowerValue 转为每个2个幂集合。
    - 
 
- Map
    - RenameKey
    - ToMap
- List/Array
    - RemoveRange
    - Swap
    - Slice
    - Filter
    - EqualArrayContent 数组去除，比较内容是否相同。忽略位置
    - Unwind
    - Skip 
    - ForEachExt
    - InsertAfter
    - IntersectIndeies
    - Intersect
    - Minus
    - SplitDiffData
    - ToMap
- File
    - ReadHeadLines
    - ReadTailLines
    - FilterLines
    -
- Stream
    - GetHtmlString
    -
## 注解
- JsonModel，表示Request参数是整个RequestBody的Json。
- Require ，必填字段
- OpenAction ，不需要权限的Action

## 其它
- usingScope ，在每个作用域里控制变量。



# 使用
```
<!-- https://mvnrepository.com/artifact/cn.dev8/ktext -->
<dependency>
    <groupId>cn.dev8</groupId>
    <artifactId>ktext</artifactId>
    <version>${project.version}</version>
</dependency>
```

