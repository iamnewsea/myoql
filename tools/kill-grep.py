#!/usr/bin/env python3
#coding=utf-8

import os, sys, getopt


if len(sys.argv) <= 1:
    print("传递查询进程的参数，当只有一个进程时，程序执行 kill -9 ")
    sys.exit(1)

def pcode(code):
    if( code >> 8 ):
        print("系统出现错误!")
        sys.exit(code)

if __name__=='__main__':
    args = sys.argv[1:]
    greps = map(lambda x: """grep "{}" """.format(x) ,   args)

    cmd = """ps -ef | {} | tr -s ' '| grep -v ' grep ' | grep -v '{}' | cut -d' ' -f2,8-""".format(" | ".join(greps), __file__)

    print(cmd)
    print("----------------------------")
    results = os.popen(cmd).readlines()
    if len(results) > 1:
        print("找到 {} 个进程".format(len(results)))
        print( "\n".join(results))
        sys.exit(1)
    elif  len(results) == 0 :
        print("找不到进程：{}".format(" , ".join(args)))
        sys.exit()
    else:
        print("找到 1 个进程" )
        print(results[0])

    pid = results[0].split(' ')[0]

    pcode(os.system("kill -9 " + pid))

    print("killed {}: {}".format(pid," , ".join(args)))
    print("----------------------------")