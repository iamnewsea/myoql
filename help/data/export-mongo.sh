#!/usr/bin/env bash
host="127.0.0.1"
database=iam
user=root
password=1234.5678
time=$(date "+%Y%m%d-%H%M")
filter="."

mkdir -p /data/mongo-bak/$time
mongo --host $host --quiet $database -u $user -p $password --authenticationDatabase "admin" --eval "db.getCollectionNames()" \
  | awk '{gsub(/[",]/,"");print substr($0,2)}' \
  | grep $filter \
  > mongo-tables.txt

for table in `cat mongo-tables.txt` ; do
  mongoexport --host $host -d $database -u $user -p $password  --authenticationDatabase "admin" --collection $table  -o /data/mongo-bak/$time/$table.json && echo "[$table done!]"
done

echo "mongo数据导出完成！"
