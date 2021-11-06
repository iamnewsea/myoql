#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import sys
import shutil
import  xml.dom.minidom
import  getopt


def err(message):
    print("%s 发生了错误！！！" %(message))
    sys.exit(1)

def replaceOsSep(src):
    return src.replace("/",os.sep).replace("\\",os.sep)

def setWorkPath():
    base_path =  os.path.abspath( os.path.join( __file__ ,"../" ) )
    os.chdir( base_path)

def getArgument():
    file = ""
    profile = ""
    try:
        opts, args = getopt.getopt(sys.argv[1:],"hf:P:",["file=","profile="])
    except getopt.GetoptError:
        print ('install_jar.py -f 目标项目pom.xml所在的文件夹 ')
        sys.exit(2)

    for opt, arg in opts:
        if opt == '-h':
            print ('install_jar.py -f 目标项目pom.xml所在的文件夹 -P profile')
            sys.exit()
        elif opt in ('-P','--profile') :
            profile = arg
        elif opt in ("-f", "--file"):
            file = arg
    return (file,profile)


if __name__=='__main__':

    setWorkPath()
    file,profile = getArgument()


    profileValue = ""
    if profile:
        profileValue =  " -P "+ profile


    if not file:
        file = "ktweb"

    install ="mvn clean install -Dmaven.test.skip=true -Dmaven.test.skip=true -e -U -pl %s -am "%(file)

    print("-------------------------------------------------------------------------------")
    print("")
    print("正在打包 %s 并安装实体jar ..."%(file))
    print("")

    print(install)
    returnCode = os.system(install)
    if(returnCode !=0 ):
        err("install")

    print("")
    print("%s 安装完成！"%(file))
    print("-------------------------------------------------------------------------------")

