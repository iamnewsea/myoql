
# 导入K8s Mongo数据库

>kubectl cp sysCity.json default/mongo-xxx:/opt

>kubectl exec mongo-57d5f47895-jxd2x -it -- mongoimport -h 127.0.0.1 --port 27017 -d lowcode --collection sysCity -u dev -p 123  --file /opt/sysCity.json

# 导入 sysCity.json

1. scp sysCity.json 到服务器 /var/lib/docker/volumes/mongo_data/_data
2. 使 docker 能访问到 sysCity.json ,并得到 docker 内的路径
3. 在宿主机执行： docker exec mongo mongo-import.sh db user password /data/ sysCity.json
4. 完成。