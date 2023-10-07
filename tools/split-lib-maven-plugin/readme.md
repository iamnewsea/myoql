# split-lib 使用方式：

## 调试
1. 在Idea项目打开 `Project Structure`  --> `Libraries` , 手动添加 jar 包
2. 在Idea项目的 `External Libraries`, 打开文件， 设置断点。
3. 在Idea项目 `Mvaven`  要启动的模块 --> `Plugins` --> `split-lib-maven-plugin` 右键调试！

## 手动安装

```
mvn dependency:get -DremoteRepositories=https://oss.sonatype.org/content/repositories/snapshots -DgroupId=cn.dev8  -DartifactId=split-lib-maven-plugin -Dversion=1.1.9-SNAPSHOT
```

或者安装指定文件：

```
mvn install:install-file -DgroupId=cn.dev8 -DartifactId=split-lib-maven-plugin -Dversion=1.1.9-SNAPSHOT  -Dpackaging=pom -Dfile=pom.xml

mvn install:install-file -DgroupId=cn.dev8 -DartifactId=split-lib-maven-plugin -Dversion=1.1.9-SNAPSHOT  -Dpackaging=maven-plugin -Dfile=target/split-lib-maven-plugin-1.1.9-SNAPSHOT.jar
```

#### windows:

c:\window\split-lib.cmd

```
mvn cn.dev8:split-lib-maven-plugin:1.1.9-SNAPSHOT:split-lib  -DkeepGroupIds=%*
```

#### linux:

/usr/local/bin/split-lib.sh

```
mvn cn.dev8:split-lib-maven-plugin:1.1.9-SNAPSHOT:split-lib  -DkeepGroupIds=$*
```

## 命令行执行：

```
先package.
cd module!
mvn cn.dev8:split-lib-maven-plugin:1.1.9-SNAPSHOT:split-lib  -DkeepGroupIds=cn.dev8,com.nancal
```

## 集成到程序里 （可忽略，直接使用命令行执行更方便！）

``` 
<profile>
    <id>split-lib</id>
    <properties>
        <env>split-lib</env>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>cn.dev8</groupId>
                <artifactId>split-lib-maven-plugin</artifactId>
                <version>1.1.11-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>split-lib</goal>
                        </goals>
                        <configuration>
                            <keepGroupIds>cn.dev8</keepGroupIds>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</profile>
```

不可以手动执行！因为手动执行拿不到变量!

