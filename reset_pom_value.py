#!/usr/bin/python
# -*- coding: utf-8 -*-

import os
import sys
import shutil
import xpath
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

def getArgs( ):
    tag = ""
    value = ""
    file = ""
    try:
        opts, args = getopt.getopt(sys.argv[1:],"t:v:fh",["tag=","value=","file="])
    except getopt.GetoptError:
        print ('reset_pom_value.py -f pom.xml所在路径（默认当前） -t xpath表达式 -v 新的值')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print ('reset_pom_value.py -f pom.xml所在路径（默认当前） -t xpath表达式 -v 新的值')
            sys.exit()
        elif opt in ("-f", "--file"):
            file = arg
        elif opt in ("-t", "--tag"):
            tag = arg
        elif opt in ("-v", "--value"):
            value = arg


    return file
def check_same(module,groupId,artifactId):
    os.chdir(module)
    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement

    vv_data = root.getElementsByTagName('version')[0].childNodes[0];
    mns = list(filter(lambda x: x.nodeType == 1, root.getElementsByTagName('parent')[0].childNodes));
    t_groupId= list(filter( lambda x: x.tagName == "groupId",mns ))[0].childNodes[0].data
    t_artifactId= list(filter( lambda x: x.tagName == "artifactId",mns))[0].childNodes[0].data

    if t_groupId != groupId:
        return False

    if t_artifactId != artifactId:
        return False

    return True

def getSubPoms():
    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement
    groupId = root.getElementsByTagName('groupId')[0].childNodes[0].data;
    artifactId = root.getElementsByTagName('artifactId')[0].childNodes[0].data;

    mns = filter(lambda x: x.nodeType == 1, root.getElementsByTagName('modules')[0].childNodes);
    modules = list( map(lambda x: x.childNodes[0].data ,mns) )

    # 检查子项目的 parent 是否一致
    for index in range(len(modules)):
        m = modues[index]
        if not check_same(m,groupId,artifactId):
            modules.remove(m)
        os.chdir("..")

    return modules

def resetData(module,tag,value):
    os.chdir(module);
    dom = xml.dom.minidom.parse('pom.xml')
    root = dom.documentElement
    elems = xpath.find(tag,root)
    for e in elems:
        e.data = value


    with open('pom.xml','w',encoding='UTF-8') as fh:
        fh.write(dom.toxml())
        fh.flush()


    print("重新设置了: %s"%(module))

if __name__=='__main__':
    print("-------------------------------------------------------------------------------")
    print(os.linesep)


    setWorkPath();

    tag,value,file = getArgs()
    if file:
        os.chdir(file)

    subPoms = getSubPoms()
    for s in subPoms:
        resetData(s,tag,value)

    print("")
    print("%s %s %s 设置完成！"%(file,tag,value))
    print("-------------------------------------------------------------------------------")

