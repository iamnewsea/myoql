# run.sh 
在服务器上，开启新任务运行Jar包，用法：

    (./run.sh Jar包 &)
    
# 常用重构正则

## 替换

    \.orderBy\s*\{\s*(\w+)\.(\w+)\.asc\s*\}
    =>
    .orderByAsc { $1.$2 }