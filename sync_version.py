#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import sys
import shutil
import xml.dom.minidom
import getopt

# 第一个参数
newVersion = ""
if len(sys.argv) > 1:
    # 第一个参数
    newVersion = sys.argv[1]


def err(message):
    print("%s 发生了错误！！！" % (message))
    sys.exit(1)


def replaceOsSep(src):
    return src.replace("/", os.sep).replace("\\", os.sep)


def setWorkPath():
    base_path = os.path.abspath(os.path.join(__file__, "../"))
    os.chdir(base_path)

def findNodes(es,paths):
    ps = paths.split("/")
    items = es
    for x in ps :
        items = list( filter( lambda  n:n.nodeType == 1 and n.nodeName == x, items) )
        if (any(items) == False):
            return []
        items2 = [];
        for i in items:
            items2.extend(i.childNodes)
        items = items2
    return items

def findText(es,paths):
    nodes = findNodes(es,paths)
    if( any(nodes) == False):
        return ""
    return nodes[0].data

def getVersionOnly():
    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement
    return findText(root.childNodes,"version")

def resetVersionOnly(version):
    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement

    vv_data = findNodes(root.childNodes,"version")
    ori_version = vv_data[0].data
    vv_data[0].data = version

    with open('pom.xml', 'w', encoding='UTF-8') as fh:
        fh.write(dom.toxml())
        fh.flush()

    print("对 pom.xml 重新设置了版本号 %s --> %s" % (ori_version, version))
    return ori_version


def getVersionData():
    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement
    groupId = findText(root.childNodes,"groupId")

    if(len(groupId) == 0):
        groupId = findText(root.childNodes,"parent/groupId")

    artifactId = findText(root.childNodes,"artifactId")
    version = findText(root.childNodes,"version")

    mns = findNodes(root.childNodes,"modules")
    modules = list(map( lambda x: x.childNodes[0].data, filter(lambda x:  x.nodeType == 1, mns ) ))

    return groupId, artifactId, version, modules


def resetVersion(module, file, groupId, artifactId, version):
    if module:
        os.chdir(module)

    dom = xml.dom.minidom.parse(file)
    root = dom.documentElement

    vv_data = findNodes(root.childNodes,"version")
    p_groupId = findNodes(root.childNodes,"parent/groupId")
    p_artifactId = findNodes(root.childNodes,"parent/artifactId")
    t_version_data = findNodes(root.childNodes,"parent/version")

    if p_groupId[0].data != groupId:
        return

    if p_artifactId[0].data != artifactId:
        return

    if len(vv_data) > 0:
        vv_data[0].data = version
    t_version_data[0].data = version

    with open(file, 'w', encoding='UTF-8') as fh:
        fh.write(dom.toxml())
        fh.flush()

    print("对 %s 重新设置了版本号: %s" % (module, version))


if __name__ == '__main__':
    setWorkPath()

    if len(newVersion) == 0:
        print("myoql现在的版本: " + getVersionOnly())
        sys.exit(1)
    elif newVersion == "+":
        version = getVersionOnly()
        tails = version.split("-")
        v_ary = tails[0].split(".")
        v_ary[-1] = str(int(v_ary[-1]) +1)
        tails[0] = ".".join(v_ary)
        newVersion = "-".join(tails)




    print("-----------------------------------------------")
    resetVersionOnly(newVersion)
    groupId, artifactId, version, modules = getVersionData()

    print("-----------------------------------------------")

    cwd = os.getcwd()
    for module in modules:
        resetVersion(module, "pom.xml", groupId, artifactId, version)
        os.chdir(cwd)

    print("-----------------------------------------------")
