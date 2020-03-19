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


def resetVersionOnly(version):
    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement

    vv_data = root.getElementsByTagName('version')[0].childNodes[0]
    vv_data.data = version

    with open('pom.xml', 'w', encoding='UTF-8') as fh:
        fh.write(dom.toxml())
        fh.flush()

    print("重新设置了版本号 %s" % (version))


def getVersionData():
    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement
    groupId = root.getElementsByTagName('groupId')[0].childNodes[0].data
    artifactId = root.getElementsByTagName('artifactId')[0].childNodes[0].data
    version = root.getElementsByTagName('version')[0].childNodes[0].data

    mns = filter(lambda x: x.nodeType == 1, root.getElementsByTagName('modules')[0].childNodes)
    modules = list(map(lambda x: x.childNodes[0].data, mns))

    return groupId, artifactId, version, modules


def resetVersion(module, file, groupId, artifactId, version):
    if module:
        os.chdir(module)

    dom = xml.dom.minidom.parse(file)
    root = dom.documentElement

    vv_data = root.getElementsByTagName('version')[0].childNodes[0]
    mns = list(filter(lambda x: x.nodeType == 1, root.getElementsByTagName('parent')[0].childNodes))
    t_groupId = list(filter(lambda x: x.tagName == "groupId", mns))[0].childNodes[0].data
    t_artifactId = list(filter(lambda x: x.tagName == "artifactId", mns))[0].childNodes[0].data
    t_version_data = list(filter(lambda x: x.tagName == "version", mns))[0].childNodes[0]

    if t_groupId != groupId:
        return

    if t_artifactId != artifactId:
        return

    vv_data.data = version
    t_version_data.data = version

    with open(file, 'w', encoding='UTF-8') as fh:
        fh.write(dom.toxml())
        fh.flush()

    print("重新设置了: %s" % (module))


if __name__ == '__main__':
    setWorkPath()

    if len(newVersion) > 0:
        resetVersionOnly(newVersion)

    groupId, artifactId, version, modules = getVersionData()

    print("-------------------------------------------------------------------------------")
    print(os.linesep)

    resetVersion("", "pom_component.xml", groupId, artifactId, version)

    for module in modules:
        resetVersion(module, "pom.xml", groupId, "myoql_component", version)
        os.chdir("../")

    print("")
    print("新版本 %s 设置完成！" % (version))
    print("-------------------------------------------------------------------------------")
