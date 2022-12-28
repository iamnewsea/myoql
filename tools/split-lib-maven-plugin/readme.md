# split-lib 使用方式：

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

或者，命令行执行：

先package.

```
cd module!
mvn cn.dev8:split-lib-maven-plugin:split-lib  -DkeepGroupIds=cn.dev8,com.nancal
```