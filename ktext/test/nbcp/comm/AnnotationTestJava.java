package nbcp.comm;

import nbcp.TestBase;
import nbcp.db.DbDefine;
import nbcp.db.DbDefines;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;
import java.util.Arrays;

public class AnnotationTestJava extends TestBase {
    @Repeatable(value = Roles.class)
    public  @interface Role {
        String name() default "doctor";
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Roles {
        Role[] value();
    }

    @Role(name = "doctor")
    @Role(name = "who")
    public class RepeatAnn{
    }

    @Test
    void ddd(){
        Arrays.stream(RepeatAnn.class.getAnnotations()).forEach(System.out::println);
    }
}
