# ktweb

* 使上传视频文件时，可以解析到第一帧的画面,引入下包:

```
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv</artifactId>
    <version>1.5.3</version>
</dependency>

<!--windows ffmpeg-->
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>ffmpeg</artifactId>
    <version>4.2.2-1.5.3</version>
    <classifier>linux-x86_64</classifier>
</dependency>
```

两个组件版本需要匹配。

`classifier` 根据情况，可以为： `linux-x86_64` 和 `windows-x86_64`


* 验证码生成图片，引入下包：
```
    <dependency>
        <groupId>com.github.whvcse</groupId>
        <artifactId>easy-captcha</artifactId>
        <version>1.6.2</version>
    </dependency>
```


* 

