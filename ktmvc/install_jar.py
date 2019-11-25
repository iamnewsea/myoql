#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import sys
import shutil
import  xml.dom.minidom

dom = xml.dom.minidom.parse('pom.xml')
root = dom.documentElement
groupId = root.getElementsByTagName('groupId')[0].childNodes[0].data;
artifactId = root.getElementsByTagName('artifactId')[0].childNodes[0].data;
version = root.getElementsByTagName('version')[0].childNodes[0].data;

cleanCMD = "mvn clean package -Dmaven.test.skip=true"
jarFile = "target/%s-%s.jar" % (artifactId,version)
installCMD="mvn install:install-file -Dfile=%s -DgroupId=%s -DartifactId=%s -Dversion=%s -Dpackaging=jar" %(jarFile.replace("/",os.sep),groupId,artifactId,version)



def err(message):
    print("%s 发生了错误！！！" %(message))
    sys.exit(1)

def replaceOsSep(src):
    return src.replace("/",os.sep).replace("\\",os.sep)

def setWorkPath():
    base_path =  os.path.abspath( os.path.join( __file__ ,"../" ) )
    os.chdir( base_path)

if __name__=='__main__':
    print("-------------------------------------------------------------------------------")
    print("")
    print("正在打包并安装实体 %s.%s ..."%(groupId,artifactId))
    print("")
    print(os.linesep)

    setWorkPath();

    returnCode = os.system(cleanCMD)
    if(returnCode !=0 ):
        err("clean")

    print(os.linesep)

    returnCode = os.system(installCMD)
    if (returnCode != 0):
        err("install")

    # shutil.copy(jarFile,"../corp/lib/")
    # shutil.copy(jarFile,"../admin/lib/")
    # shutil.copy(jarFile,"../c/lib/")

    print("")
    print("安装 %s.%s 完成！"%(groupId,artifactId))
    print("-------------------------------------------------------------------------------")

