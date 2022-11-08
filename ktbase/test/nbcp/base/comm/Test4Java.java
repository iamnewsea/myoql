package nbcp.base.comm;

import kotlin.collections.CollectionsKt;
import nbcp.base.TestBase;
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import org.junit.jupiter.api.Test;

import java.util.*;

class Test4Java extends TestBase {


    @Test
    public void test_get_json() {
        List<List<String>> list = new LinkedList<>();
        list.add(CollectionsKt.listOf("a", "b", "c"));
        list.add(CollectionsKt.listOf("x", "y", "z"));

        List<String> ret = MyHelper.Unwind(list);
        System.out.println(MyJsonUtil.toJson(ret));
    }
}