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

def getFile( ):
    file = ""
    try:
        opts, args = getopt.getopt(sys.argv[1:],"hf:",["file="])
    except getopt.GetoptError:
        print ('install_jar.py -f 目标项目pom.xml所在的文件夹')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print ('install_jar.py -f 目标项目pom.xml所在的文件夹')
            sys.exit()
        elif opt in ("-f", "--file"):
            file = arg
    return file

def getJavaFiles():
    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement
    groupId = root.getElementsByTagName('groupId')[0].childNodes[0].data;
    artifactId = root.getElementsByTagName('artifactId')[0].childNodes[0].data;
    version = root.getElementsByTagName('version')[0].childNodes[0].data;

    cleanCMD = "mvn clean package -Dmaven.test.skip=true"
    jarFile = "target/%s-%s.jar" % (artifactId,version)
    installCMD="mvn install:install-file -Dfile=%s -DgroupId=%s -DartifactId=%s -Dversion=%s -Dpackaging=jar" %(jarFile.replace("/",os.sep),groupId,artifactId,version)

    return cleanCMD,jarFile,installCMD

if __name__=='__main__':

    file = getFile()

    setWorkPath();
    os.chdir(file);

    cleanCMD,jarFile,installCMD = getJavaFiles()

    print("-------------------------------------------------------------------------------")
    print("")
    print("正在打包 %s 并安装实体jar ..."%(file))
    print("")
    print(os.linesep)


    returnCode = os.system(cleanCMD)
    if(returnCode !=0 ):
        err("clean")

    print(os.linesep)

    returnCode = os.system(installCMD)
    if (returnCode != 0):
        err("install")

    print("")
    print("%s 安装完成！"%(file))
    print("-------------------------------------------------------------------------------")

