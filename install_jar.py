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
    type = root.getElementsByTagName('packaging')[0].childNodes[0].data;


    jarFile = "target/%s-%s.jar" % (artifactId,version)
    install="mvn install:install-file -Dfile={} -DgroupId=%s -DartifactId=%s -Dversion=%s -Dpackaging=%s {}" %(groupId,artifactId,version,type)

    if type == "pom":
        return "",install.format("pom.xml",""),"",""


    return "mvn clean package -Dmaven.test.skip=true", \
           install.format(jarFile.replace("/",os.sep),""), \
           install.format(jarFile.replace("/",os.sep),"-Dclassifier=javadoc"), \
           install.format(jarFile.replace("/",os.sep),"-Dclassifier=sources")


if __name__=='__main__':

    file = getArgument()

    setWorkPath();
    if file :
        os.chdir(file);

    cleanCMD,installCMD,installJavaDoc,installSource = getJavaFiles()

    print("-------------------------------------------------------------------------------")
    print("")
    print("正在打包 %s 并安装实体jar ..."%(file))
    print("")

    if cleanCMD :
        print(os.linesep)
        print(cleanCMD)
        returnCode = os.system(cleanCMD)
        if(returnCode !=0 ):
            err("clean")

    if installCMD:
        print(os.linesep)
        print(installCMD)
        returnCode = os.system(installCMD)
        if (returnCode != 0):
            err("install")

    if installJavaDoc:
        print(os.linesep)
        print(installJavaDoc)
        returnCode = os.system(installJavaDoc)
        if (returnCode != 0):
            err("install")

    if installSource:
        print(os.linesep)
        print(installSource)
        returnCode = os.system(installSource)
        if (returnCode != 0):
            err("install")

    print("")
    print("%s 安装完成！"%(file))
    print("-------------------------------------------------------------------------------")

