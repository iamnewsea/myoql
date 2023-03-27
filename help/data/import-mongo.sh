#!/usr/bin/env bash
host="127.0.0.1"
database=iam
user=root
password=1234.5678
filter="."


for file in `ls -1 -F *.json`; do
  table=${file:0,-5}
  mongoimport --host  $host --port 27017 -d $database -u $user -p $password --collection $table --file $file
done

echo "mongo数据导入完成！"