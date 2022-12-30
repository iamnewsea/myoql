# split-lib 使用方式：

# 手动安装
 
```
mvn dependency:get -DremoteRepositories=https://oss.sonatype.org/content/repositories/snapshots -DgroupId=cn.dev8  -DartifactId=split-lib-maven-plugin -Dversion=1.1.7-SNAPSHOT
```


或者安装指定文件：
```
mvn install:install-file -DgroupId=cn.dev8 -DartifactId=split-lib-maven-plugin -Dversion=1.1.7-SNAPSHOT  -Dpackaging=pom -Dfile=pom.xml

mvn install:install-file -DgroupId=cn.dev8 -DartifactId=split-lib-maven-plugin -Dversion=1.1.7-SNAPSHOT  -Dpackaging=maven-plugin -Dfile=target/split-lib-maven-plugin-1.1.7-SNAPSHOT.jar
```

#### windows:
c:\window\split-lib.cmd
```
mvn cn.dev8:split-lib-maven-plugin:1.1.7-SNAPSHOT:split-lib  -DkeepGroupIds=%*
```

#### linux:
/usr/local/bin/split-lib.sh
```
mvn cn.dev8:split-lib-maven-plugin:1.1.7-SNAPSHOT:split-lib  -DkeepGroupIds=$*
```

# 命令行执行：

```
先package.
cd module!
mvn cn.dev8:split-lib-maven-plugin:1.1.7-SNAPSHOT:split-lib  -DkeepGroupIds=cn.dev8,com.nancal
```

# 集成到程序里 （可忽略，直接使用命令行执行更方便！）

``` 
<plugin>
    <groupId>cn.dev8</groupId>
    <artifactId>split-lib-maven-plugin</artifactId>
    <version>1.1.7-SNAPSHOT</version>
    <executions>
        <execution> 
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
