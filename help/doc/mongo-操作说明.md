# 分库分表

优先级：

    上下文DataSource > 自定义拦截器 >  读写分离配置的默认DataSource


## 导入 sysCity.json

1. scp sysCity.json 到服务器 /var/lib/docker/volumes/mongo_data/_data
2. 使 docker 能访问到 sysCity.json ,并得到 docker 内的路径
3. 在宿主机执行： docker exec mongo mongo-import.sh db user password /data/   sysCity.json
4. 完成。
