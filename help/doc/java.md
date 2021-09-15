# 集成

`SpringBoot` 启动类添加 `@ComponentScan("nbcp.**")`。如果项目也是nbcp包名，可以忽略。




获取方法名：

```
//不能封装，否则只能得到封装的方法名了。
Thread.currentThread().getStackTrace()[1].getMethodName()
```

# 日志配置
1. 使用SpringBoot默认的 logback
2. yaml文件配置配置文件：

```
logging:
    config: classpath:logback-spring.xml
```

配置消息格式：
```
[%d{yyyy-MM-dd HH:mm:ss}] %level %X{group} %X{request_id} [%class:%line]:%n%m%n
```

其中有两个自定义字段,group,request_id。
    
    设置方式：MDC.put("request_id", request_id)
    设置时机：在Filter 或 HandlerInterceptor中, 或 Aop 拦截器里。
