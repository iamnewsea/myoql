#!/usr/bin/env bash

host="127.0.0.1"
user=root
password=1234.5678


for file in `ls -1 -F *.sql`; do
  mysql -u$user -p$password  < $file > log.txt

  tail 50 log.txt
done
