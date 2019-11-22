# kotlin extension

> mvn clean package -Dmaven.test.skip=true  -P release

> mvn install:install-file -Dfile=target\ktmyoql-0.0.2.jar -DgroupId=cn.dev8 -DartifactId=ktmyoql -Dversion=0.0.2 -Dpackaging=jar

> mvn clean deploy -P release


使用
```
<!-- https://mvnrepository.com/artifact/cn.dev8/ktext -->
<dependency>
    <groupId>cn.dev8</groupId>
    <artifactId>ktext</artifactId>
    <version>0.0.2</version>
</dependency>

```