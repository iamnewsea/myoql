package nbcp.comm;

import kotlin.collections.CollectionsKt;
import nbcp.TestBase;
import nbcp.db.IdName;
import nbcp.scope.JsonSceneEnumScope;
import nbcp.utils.RecursionUtil;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
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