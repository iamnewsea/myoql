
import os
import sys
import datetime

host="y1"
port=8117
db="test"
user="test"

password = sys.argv[1]
date =sys.argv[2]

def setWorkPath():
    base_path =  os.path.abspath( os.path.join( __file__ ,"../" ) )
    os.chdir( base_path)

def work(collection):
    cmd = "mongoimport -h %s --port %s -d %s -u %s -p %s --collection %s --file %s/%s.json" % (host,port,db,user,password,collection,date, collection)
    print (cmd)
    if( os.system(cmd) != 0):
        sys.exit()



if __name__=='__main__':
    setWorkPath()

    for file in os.listdir( date ):
        if not file.endswith(".json"):
            break
        collection = file[:-5]
        print(collection)
        work(collection)

    # 用法:
    # 先修改这个文件的服务器配置，文件没有保存密码，指定文件夹以及密码。
    # python export-mongo.py  密码 导出的日期文件夹
