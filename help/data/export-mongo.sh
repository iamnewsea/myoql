#!/usr/bin/env bash
database=album
user=dev
password=$1
time=$(date "+%Y%m%d-%H%M")

mkdir -p /data/mongo/$time
mongo --quiet $database -u $user -p $password --eval "db.getCollectionNames()" \
  | awk '{print substr($0,2,length()-2)}' \
  | { read c; ary=(${c//,/});  for i in ${ary[@]};  do echo $i;  done  ; } \
  |  awk '{print substr($0,2,length()-2)}' \
  | { readarray ary ; for i in ${ary[@]};  do echo "mongoexport -d $database -u $user -p $password --collection $i  -o /data/mongo/$time/$i.json && echo '['$i' done！]'";  done  ; }
  | bash


