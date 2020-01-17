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


if __name__=='__main__':
    returnCode = os.system("python install_jar.py")
    if(returnCode !=0 ):
        err("install_jar")

    print(os.linesep)

    returnCode = os.system("python install_jar.py -f ktext")
    if(returnCode !=0 ):
        err("install_jar ktext")

    print(os.linesep)

    returnCode = os.system("python install_jar.py -f ktmyoql")
    if(returnCode !=0 ):
        err("install_jar ktmyoql")

    returnCode = os.system("python install_jar.py -f ktmvc")
    if(returnCode !=0 ):
        err("install_jar ktmvc")

    print("")
    print("安装完成！")
    print("-------------------------------------------------------------------------------")

