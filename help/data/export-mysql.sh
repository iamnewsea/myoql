#!/usr/bin/env bash

host="127.0.0.1"
user=root
password=1234.5678
db_name=iam
time=$(date "+%Y%m%d-%H%M")

mysqldump  -u$user -p$password -h$host -B $db_name|gzip >/data/mysql-bak-time/$db_name.sql.gz
