//package nbcp.comm;
//
///**
// * Created by udi on 17-3-30.
// */
//
//import java.lang.annotation.Documented;
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
//import org.springframework.core.annotation.AliasFor;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//@Target({ElementType.METHOD, ElementType.TYPE})
//@Retention(RetentionPolicy.RUNTIME)
//@Documented
//@RequestMapping(
//        method = {RequestMethod.POST}
//)
//public @interface JsonpMapping {
//    @AliasFor(
//            annotation = RequestMapping.class
//    )
//    String name() default "";
//
//    @AliasFor(
//            annotation = RequestMapping.class
//    )
//    String[] value() default {};
//
//    @AliasFor(
//            annotation = RequestMapping.class
//    )
//    String[] path() default {};
//
//    @AliasFor(
//            annotation = RequestMapping.class
//    )
//    String[] params() default {};
//
//    @AliasFor(
//            annotation = RequestMapping.class
//    )
//    String[] headers() default {};
//
//    @AliasFor(
//            annotation = RequestMapping.class
//    )
//    String[] consumes() default {};
//
//    @AliasFor(
//            annotation = RequestMapping.class
//    )
//    String[] produces() default {};
//}
//
