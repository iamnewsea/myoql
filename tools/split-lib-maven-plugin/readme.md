# split-lib 使用方式：

# 手动安装

```
mvn install:install-file -DgroupId=cn.dev8 -DartifactId=split-lib-maven-plugin -Dversion=1.1.7-SNAPSHOT  -Dpackaging=pom -Dfile=pom.xml

mvn install:install-file -DgroupId=cn.dev8 -DartifactId=split-lib-maven-plugin -Dversion=1.1.7-SNAPSHOT  -Dpackaging=maven-plugin -Dfile=target/split-lib-maven-plugin-1.1.7-SNAPSHOT.jar
```

# 集成到程序里 （可忽略，直接使用命令行执行）

``` 
<plugin>
    <groupId>cn.dev8</groupId>
    <artifactId>split-lib-maven-plugin</artifactId>
    <version>1.1.7-SNAPSHOT</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>split-lib</goal>
            </goals>
            <configuration>
                <keepGroupIds>cn.dev8,com.nancal</keepGroupIds>
            </configuration>
        </execution>
    </executions>
</plugin>
```


# 命令行执行：

```
先package.
cd module!
mvn cn.dev8:split-lib-maven-plugin:split-lib  -DkeepGroupIds=cn.dev8,com.nancal
```