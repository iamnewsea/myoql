package nbcp.base.comm;

import nbcp.base.enums.JsonSceneScopeEnum;
import nbcp.base.enums.JsonStyleScopeEnum;
import nbcp.base.extend.MyJson;


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
