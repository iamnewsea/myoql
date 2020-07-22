//package nbcp
//
//import nbcp.comm.ToJson
//import org.apache.commons.cli.DefaultParser
//import org.apache.commons.cli.Options
//
//fun main(vararg args: String) {
//    var ops = Options();
//    ops.addOption("h", "help", false, "help")
//    ops.addOption("m", "merge", true, "合并yml文件")
//
//    var par = DefaultParser().parse(ops,args);
//    if( par.hasOption("h")){
//        println("""
//java -jar ktext.jar -m file1.yml -m file2.yml
//""")
//        return;
//    }
//
//    var files = par.getOptionValues("m");
//
//
//}