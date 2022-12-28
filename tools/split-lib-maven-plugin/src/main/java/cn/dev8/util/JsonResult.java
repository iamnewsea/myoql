package cn.dev8.util;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Yuxh
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JsonResult {
    /**
     * 成功返回0
     */
    private int code = 0;
    /**
     * 成功返回空字符串
     */
    private String msg = "";


    public boolean hasError() {
        return this.code != 0;
    }
}
