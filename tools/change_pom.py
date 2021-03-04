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

def printHelp():
    print('''
替换 xml 文件中的值。
python reset_pom_value.py -f 根pom.xml所在路径（默认当前） -t xpath表达式 -v 新的值
''')

def getArgs():
    groupId = ""
    file = ""
    artifactId = ""

    try:
        opts, args = getopt.getopt(sys.argv[1:], "g:a:f:h", ["groupId=","artifactId=","file="])
    except getopt.GetoptError:
        printHelp()
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            printHelp()
            sys.exit()
        elif opt in ("-f", "--file"):
            file = arg
        elif opt in ("-g", "--groupId"):
            groupId = arg
        elif opt in ("-a", "--artifactId"):
            artifactId = arg

    return file,groupId,artifactId


def findNodes(es,paths):
    ps = paths.split("/")
    items = es
    for x in ps :
        items = list( filter( lambda  n:n.nodeType == 1 and n.nodeName == x, items) )
        if (any(items) == False):
            return ""
        items2 = [];
        for i in items:
            items2.extend(i.childNodes)
        items = items2
    return items

def findText(es,paths):
    return findNodes(es,paths)[0].data

def getModulesData():

    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement
    groupId = findText(root.childNodes,"groupId")

    if(len(groupId) == 0):
        groupId = findText(root.childNodes,"parent/groupId")

    artifactId = findText(root.childNodes,"artifactId")


    mns = findNodes(root.childNodes,"modules")
    modules = list(map( lambda x: x.childNodes[0].data, filter(lambda x:  x.nodeType == 1, mns )))


    return groupId,artifactId,modules

def resetGroupId(newGroupId,newArtifactId):

    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement

    groupId = findNodes(root.childNodes,"groupId")
    artifactId = findNodes(root.childNodes,"artifactId")


    if  newGroupId:
        groupId[0].data = newGroupId

    if  newArtifactId:
        artifactId[0].data = newArtifactId

    with open('pom.xml','w',encoding='UTF-8') as fh:
        fh.write(dom.toxml())
        fh.flush()


    print("重新设置了根 pom.xml")

def resetSubGroupId(module,groupId,artifactId,newGroupId,newArtifactId):
    if module:
        os.chdir(module);

    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement

    groupId = findNodes(root.childNodes,"groupId")
    artifactId = findNodes(root.childNodes,"artifactId")


    p_groupId = findNodes(root.childNodes,"parent/groupId")
    p_artifactId = findNodes(root.childNodes,"parent/artifactId")


    if p_groupId[0].data == groupId and newGroupId:
        p_groupId[0].data = newGroupId
        groupId[0].data = newGroupId

    if p_artifactId[0].data == artifactId and newArtifactId:
        p_artifactId[0].data = newArtifactId
        artifactId[0].data = newArtifactId

    with open('pom.xml','w',encoding='UTF-8') as fh:
        fh.write(dom.toxml())
        fh.flush()


    print("重新设置了: %s"%(module))

if __name__=='__main__':

    file,newGroupId,newArtifactId = getArgs()

    setWorkPath();

    if file:
        os.chdir(file)

    groupId,artifactId,modules = getModulesData()

    print("-------------------------------------------------------------------------------")
    print(os.linesep)


    for module in modules:
        resetSubGroupId(module,groupId,artifactId,newGroupId,newArtifactId)
        os.chdir("../")


    resetGroupId(newGroupId,newArtifactId)

    print("")
    print("设置 %s 完成！"%(newGroupId))
    print("-------------------------------------------------------------------------------")

