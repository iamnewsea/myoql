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

def getModulesData():

    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement
    groupId = root.getElementsByTagName('groupId')[0].childNodes[0].data;
    artifactId = root.getElementsByTagName('artifactId')[0].childNodes[0].data;


    mns = filter(lambda x: x.nodeType == 1, root.getElementsByTagName('modules')[0].childNodes);
    modules = list( map(lambda x: x.childNodes[0].data ,mns) )


    return groupId,artifactId,modules

def resetGroupId(newGroupId,newArtifactId):

    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement

    groupIdDom = root.getElementsByTagName('groupId')[0].childNodes[0];
    artifactIdDom = root.getElementsByTagName('artifactId')[0].childNodes[0];


    if  newGroupId:
        groupIdDom.data = newGroupId

    if  newArtifactId:
        artifactIdDom.data = newArtifactId

    with open('pom.xml','w',encoding='UTF-8') as fh:
        fh.write(dom.toxml())
        fh.flush()


    print("重新设置了根 pom.xml")

def resetSubGroupId(module,groupId,artifactId,newGroupId,newArtifactId):
    if module:
        os.chdir(module);

    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement

    groupIdDom = root.getElementsByTagName('groupId')[0].childNodes[0];
    artifactIdDom = root.getElementsByTagName('artifactId')[0].childNodes[0];


    mns = list(filter(lambda x: x.nodeType == 1, root.getElementsByTagName('parent')[0].childNodes));
    t_groupIdDom= list(filter( lambda x: x.tagName == "groupId",mns ))[0].childNodes[0]
    t_artifactIdDom= list(filter( lambda x: x.tagName == "artifactId",mns))[0].childNodes[0]


    if t_groupIdDom.data == groupId and newGroupId:
        t_groupIdDom.data = newGroupId
        groupIdDom.data = newGroupId

    if t_artifactIdDom.data == artifactId and newArtifactId:
        artifactIdDom.data = newArtifactId
        t_artifactIdDom.data = newArtifactId

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

