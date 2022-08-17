package nbcp.comm;

import nbcp.scope.JsonSceneEnumScope;
import nbcp.scope.JsonStyleEnumScope;


/**
 * 仅为了Java规范
 */
public class MyJsonUtil {
    public <T> String toJson(T value) {
        return MyJson.ToJson(value, null, new JsonStyleEnumScope[0]);
    }

    public <T> String toJson(T value, JsonSceneEnumScope style, JsonStyleEnumScope... jsonScopes) {
        return MyJson.ToJson(value, style, jsonScopes);
    }


    public <T> T fromJson(String value, Class<T> clazz) {
        return MyJson.FromJson(value, clazz);
    }
}
