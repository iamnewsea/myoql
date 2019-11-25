
# 发布

- 修改 /pom.xml version
- python sync_version.py
- mvn clean package -Dmaven.test.skip=true
- mvn clean deploy -P release

# 使用包需要配置的变量

- server.upload.host 表示上传文件的文件服务 Host
- server.upload.logoSize 表示上传图片自动压缩的最大大小。
- server.filter.allowOrigins 表示跨域允许的域名。
- server.filter.ignore-log-urls 表示哪些URL在请求过程中添加 LogScope.NoInfo 作用域。
- server.dataCenterId 表示服务所在的数据中心Id，生成雪花算法时使用。
- server.machineId 表示数据中心下属的服务器Id，生成雪花算法时使用。



