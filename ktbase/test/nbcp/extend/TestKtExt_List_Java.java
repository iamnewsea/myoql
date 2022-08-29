package nbcp.extend;

import nbcp.TestBase;
import nbcp.comm.*;
import nbcp.utils.*;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

class TestKtExt_List_Java extends TestBase {


    @Test
    public void test_cn() {
        LinkedList list = new LinkedList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        System.out.println(MyHelper.joinToString(list, "-"));


        String[] x = MyHelper.toTypedArray(list, String.class);
        System.out.println(MyJsonUtil.toJson(x));
    }

    @Test
    public void abc() {
        BatchReader<String> reader = BatchReader.init(5,(skip, take) -> {
            LinkedList list = new LinkedList<String>();
            if( skip == 0){
                list.add("a1");
                list.add("b2");
                list.add("c3");
                list.add("c4");
                list.add("c5");
                return list;
            }
            else {
                list.add("a6");
                list.add("b7");
                list.add("c8");
            }
            return list;
        });

        for (String it : reader) {
            System.out.println(it);
        }

    }

}