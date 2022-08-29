package nbcp.extend;

import nbcp.TestBase;
import nbcp.comm.*;
import nbcp.utils.*;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.regex.Pattern;

class TestKtExt_List_Java extends TestBase {


    @Test
    public void test_cn() {
        LinkedList list = new LinkedList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        System.out.println(MyHelper.joinToString(list,"-"));


        String[] x = MyHelper.toTypedArray(list,String.class);
        System.out.println(MyJsonUtil.toJson(x));
    }

}