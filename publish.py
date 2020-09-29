#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import sys
import shutil
import  xml.dom.minidom
import  getopt

if sys.version_info < (3, 0):
    print("必须使用 python3版本")
    sys.exit(1)

if len(sys.argv)<2:
    print("缺少 version 参数")
    sys.exit(1)

# 第一个参数
version = sys.argv[1]

def err(message):
    print("%s 发生了错误！！！" %(message))
    sys.exit(1)

def replaceOsSep(src):
    return src.replace("/",os.sep).replace("\\",os.sep)

def setWorkPath():
    base_path =  os.path.abspath( os.path.join( __file__ ,"../" ) )
    os.chdir( base_path)

if __name__=='__main__':

    setWorkPath()

    returnCode = os.system("python sync_version.py {}".format(version))
    if(returnCode !=0 ):
        err("sync_version")

    returnCode = os.system("python install_jar.py -P release")
    if(returnCode !=0 ):
        err("build")

    print(os.linesep)

    returnCode = os.system("mvn clean deploy -Dmaven.test.skip=true -P release")
    if (returnCode != 0):
        err("deploy")

    print("")
    print("安装完成！")
    print("-------------------------------------------------------------------------------")

