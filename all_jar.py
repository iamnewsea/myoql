#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import sys
import shutil
import xml.dom.minidom
import getopt


def setWorkPath():
    base_path = os.path.abspath(os.path.join(__file__, "../"))
    os.chdir(base_path)


def err(message):
    print("%s 发生了错误！！！" % (message))
    sys.exit(1)


def replaceOsSep(src):
    return src.replace("/", os.sep).replace("\\", os.sep)


def getArgument():
    profile = ""
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hP:", ["profile="])
    except getopt.GetoptError:
        print ('install_jar.py -f 目标项目pom.xml所在的文件夹 ')
        sys.exit(2)

    for opt, arg in opts:
        if opt == '-h':
            print ('all_jar.py -P profile')
            sys.exit()
        elif opt in ('-P', '--profile'):
            profile = arg
    return (profile)


if __name__ == '__main__':
    setWorkPath()

    (profile) = getArgument()
    profileValue = ""
    if profile:
        profileValue = " -P " + profile

    returnCode = os.system("python install_jar.py " + profileValue)

    print("")
    print("安装完成！")
    print("-------------------------------------------------------------------------------")
