package nbcp.myoql.es;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;


@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface Skills {

    Skill[] value();
}