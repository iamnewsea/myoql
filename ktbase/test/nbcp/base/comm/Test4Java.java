package nbcp.base.comm;

import kotlin.collections.CollectionsKt;
import nbcp.base.TestBaseJava;
import nbcp.base.extend.MyHelper;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

class Test4Java extends TestBaseJava {


    @Test
    public void test_get_json() {
        List<List<String>> list = new LinkedList<>();
        list.add(CollectionsKt.listOf("a", "b", "c"));
        list.add(CollectionsKt.listOf("x", "y", "z"));

        List<String> ret = MyHelper.Unwind(list);
        System.out.println(MyJsonUtil.toJson(ret));
    }
}