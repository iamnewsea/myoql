package nbcp.myoql.es;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Skills.class)
public @interface Skill {

    String[] value() default {};
}