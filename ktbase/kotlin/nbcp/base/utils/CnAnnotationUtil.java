package nbcp.base.utils;


import nbcp.base.db.annotation.Cn;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;

public class CnAnnotationUtil {
    public static String getComment(Class entType) {
        return getComment(entType, "");
    }

    /**
     * 获取表的中文注释及Cn注解
     */

    public static String getComment(Class entType, String remark) {
        Cn cnAnnotation = (Cn) entType.getAnnotation(Cn.class);
        String cn = "";
        if (cnAnnotation != null) {
            cn = cnAnnotation.value();
        }

        if (!StringUtils.hasText(cn) && !StringUtils.hasText(remark)) return "";

        return "/**\n" +
                " * " + cn + remark + "\n" +
                " */";
    }


    public static String getComment(Field field) {
        Cn cnAnnotation = (Cn) field.getAnnotation(Cn.class);
        String cn = "";
        if (cnAnnotation != null) {
            cn = cnAnnotation.value();
        }

        if (!StringUtils.hasText(cn)) return "";
        
        return "/**\n" +
                " * " + cn + "\n" +
                " */";
    }


    public static String getCnValue(Class entType) {
        Cn cnAnnotation = (Cn) entType.getAnnotation(Cn.class);
        String cn = "";
        if (cnAnnotation != null) {
            cn = cnAnnotation.value();
        }

        if (!StringUtils.hasText(cn)) return "";

        return cn;
    }


    /**
     * 从字段上反射 Cn 注解
     *
     * @param field
     * @return
     */
    public static String getCnValue(Field field) {
        Cn cnAnnotation = (Cn) field.getAnnotation(Cn.class);
        String cn = "";
        if (cnAnnotation != null) {
            cn = cnAnnotation.value();
        }

        if (!StringUtils.hasText(cn)) return "";
        return cn;
    }

}
