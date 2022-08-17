package nbcp.utils;

import nbcp.TestBase;
import nbcp.comm.*;
import nbcp.db.IdName;
import org.junit.jupiter.api.Test;

public class MyUtilJavaTest extends    TestBase  {
    @Test
    public void test1() {
         MyUtil.getRandomWithLength(4);
    }


}