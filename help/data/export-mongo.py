import os
import sys
import datetime
import json
import getopt

host="dev8.cn"
port=27017
db="album"
user="dev"

password=""
tables= []
folder= datetime.date.today()
command= False


def help():
    #print(sys.version)
    print (''' Mongo备份工具
        -p 密码
        -c 仅打印命令不执行
        -t 集合列表 逗写分隔。-t 是最后一个参数
        -f 文件夹名称
        -h --help 帮助
    ''' )

    sys.exit()

def init():
    global password,tables,folder,command
    base_path =  os.path.abspath( os.path.join( __file__ ,"../" ) )
    os.chdir( base_path)

    opts, args = getopt.getopt(sys.argv[1:], "p:t:f:ch" ,["password=","table=", "folder","command","help"] )

    prev=""
    for c, v in opts:
        prev= c
        if c in ("-p", "--password"):
            password = v
        elif c in ("-t", "--table"):
            tables.append(v)
        elif c in ("-f", "--folder"):
            folder = v
        elif c in ("-c", "--command"):
            command = True
        elif c in ( "-h", "--help" ):
            help()


    if (len(args)>0 and (prev == "-t" or prev == "--table")) :
            tables = tables + args


def work(collection):
    cmd = "mongoexport -h %s --port %s -d %s -u %s -p %s --collection %s  -o %s/%s.json" % (
        host,
        port,
        db,
        user,
        password,
        collection,
        folder,
        collection)

    print (cmd)
    if ( command ):
        return

    if( os.system(cmd) != 0):
        sys.exit()

def getCollectionsFromDb():
    cmd = """mongo --quiet  %s:%s/%s -u %s -p %s --eval "db.getCollectionNames()" """ %(
        host,
        port,
        db,
        user,
        password
    )
    print(cmd)
    results = os.popen(cmd).readlines()

    return json.loads( "".join( results) .replace("\t","").replace("\n",""));


if __name__=='__main__':
    init()

    if len(tables) == 0 :
        tables = getCollectionsFromDb()

    for collection in tables:
        work(collection)


# 用法:
# 先修改这个文件的服务器配置,文件没有保存密码 ,执行完成后，会保存到《当前日期》的文件夹中。
# python export-mongo.py -p 密码 -t集合列表文件，每个一行。
# pwd=yxh123
# docker exec mongo mongo --quiet  album -u root -p $pwd --eval "db.getCollectionNames()" | mongoexport -d album -u root -p $pwd --collection %s  -o %s/%s.json