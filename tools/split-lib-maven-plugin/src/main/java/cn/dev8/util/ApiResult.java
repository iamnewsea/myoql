package cn.dev8.util;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.var;

/**
 * @author Yuxh
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ApiResult<T> extends JsonResult {
    private T data;

    public static <T> ApiResult of(T data) {
        var ret = new ApiResult<T>();
        ret.setData(data);
        return ret;
    }

    public static ApiResult error(String msg) {
        var code = 0;
        if (msg != null && !msg.isEmpty()) {
            code = -1;
        }
        return error(msg, code);
    }

    public static ApiResult error(String msg, Integer code) {
        var ret = new ApiResult();
        ret.setMsg(msg);
        ret.setCode(code);
        return ret;
    }
}
