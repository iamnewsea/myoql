#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import sys
import shutil
import getopt
from lxml import etree


def err(message):
    print("%s 发生了错误！！！" % (message))
    sys.exit(1)


def replaceOsSep(src):
    return src.replace("/", os.sep).replace("\\", os.sep)


def setWorkPath():
    base_path = os.path.abspath(os.path.join(__file__, "../"))
    os.chdir(base_path)

def printHelp():
    print('''
替换 xml 文件中的值。
python reset_pom_value.py -f 根pom.xml所在路径（默认当前） -t xpath表达式 -v 新的值
''')

def getArgs():
    tag = ""
    value = ""
    file = ""
    try:
        opts, args = getopt.getopt(sys.argv[1:], "t:v:fh", ["tag=", "value=", "file="])
    except getopt.GetoptError:
        printHelp()
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            printHelp()
            sys.exit()
        elif opt in ("-f", "--file"):
            file = arg
        elif opt in ("-t", "--tag"):
            tag = arg
        elif opt in ("-v", "--value"):
            value = arg

    return tag, value, file


def check_same(module, groupId, artifactId):
    os.chdir(module)
    dom = etree.parse("pom.xml")
    nsmap = dom.getroot().nsmap[None]

    t_groupId = dom.find("{%s}parent/{%s}groupId" % (nsmap, nsmap)).text
    t_artifactId = dom.find("{%s}parent/{%s}artifactId" % (nsmap, nsmap)).text

    if t_groupId != groupId:
        return False

    if t_artifactId != artifactId:
        return False

    return True


def getAllPoms():
    dom = etree.parse("pom.xml")
    nsmap = dom.getroot().nsmap[None]

    groupId = dom.find("{%s}groupId" % (nsmap)).text
    artifactId = dom.find("{%s}artifactId" % (nsmap)).text
    modules = list(map(lambda x: x.text, dom.findall("{%s}modules/{%s}module" % (nsmap, nsmap))))

    # etree.tostring(dom,doctype="xml")

    # 检查子项目的 parent 是否一致
    for index in range(len(modules)):
        m = modules[index]
        if not check_same(m, groupId, artifactId):
            modules.remove(m)
        os.chdir("..")

    return modules


def resetData(module, tag, value):
    if module:
        os.chdir(module);

    dom = etree.parse("pom.xml")
    nsmap = dom.getroot().nsmap[None]

    path = "/".join(list(map(lambda x: "{%s}%s" % (nsmap, x), tag.split("/"))))
    # print (path);
    dom.find(path).text = value

    # xmlContent = etree.tostring(dom,pretty_print=True, encoding="UTF-8",xml_declaration = True) #doctype='<?xml version="1.0" encoding="UTF-8"?>')
    # print(xmlContent);

    with open('pom.xml', 'wb') as fh:
        dom.write(fh, encoding="utf-8", xml_declaration=True, pretty_print=True)
        fh.close()

    print("重新设置了: %s" % (module))


if __name__ == '__main__':

    setWorkPath();

    tag, value, file = getArgs()
    if file:
        os.chdir(file)


    print("-------------------------------------------------------------------------------")
    print(os.linesep)

    subPoms = getAllPoms()
    for s in subPoms:
        resetData(s, tag, value)
        os.chdir("..")


    resetData("",tag,value)
    print("")
    print("%s %s %s 设置完成！" % (file, tag, value))
    print("-------------------------------------------------------------------------------")
