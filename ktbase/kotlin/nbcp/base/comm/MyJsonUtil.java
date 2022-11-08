package nbcp.base.comm;

import nbcp.base.comm.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;


/**
 * 仅为了Java规范
 */
public class MyJsonUtil {
    public static <T> String toJson(T value) {
        return MyJson.ToJson(value, null, new JsonStyleScopeEnum[0]);
    }

    public static <T> String toJson(T value, JsonSceneScopeEnum style, JsonStyleScopeEnum... jsonScopes) {
        return MyJson.ToJson(value, style, jsonScopes);
    }


    public static <T> T fromJson(String value, Class<T> clazz) {
        return MyJson.FromJson(value, clazz);
    }
}
