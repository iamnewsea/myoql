package nbcp.comm

import org.slf4j.LoggerFactory
import nbcp.comm.*
import java.io.Serializable
import java.lang.RuntimeException
import java.lang.reflect.ParameterizedType
import java.util.ArrayList


/**
 * Created by jin on 2017/3/16.
 */

/**
 * 普通的返回对象。
 */
open class JsonResult(var msg: String = "", var cause: String = "") : Serializable {}

open class ApiResult<T>(msg: String = "", cause: String = "") : JsonResult(msg, cause) {
    var data: T? = null
    var value: Any? = null
    var valueRemark: String = ""

    companion object {
        @JvmStatic
        fun <T> of(data: T?): ApiResult<T> {
            var ret = ApiResult<T>();
            ret.data = data;
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param valueRemark value值的含义
     * @param value value的值
     */
    fun setValue(valueRemark: String, value: Any): ApiResult<T> {
        this.valueRemark = valueRemark;
        this.value = value;
        return this;
    }
}


class ParameterInvalidException(msg: String, cause: String) : RuntimeException(msg.AsString("参数非法"))

class NoDataException(msg: String, cause: String = "") : RuntimeException(msg.AsString("找不到数据"))

class ExecuteDbException(msg: String, cause: String = "") : RuntimeException(msg.AsString("操作数据库失败"))

class ServerException(msg: String, cause: String = "") : RuntimeException(msg.AsString("服务器异常"))

/**
 * 查询对象
 */

open class ListQueryModel {
    var skip: Int = 0;
    var take: Int = -1;
}

/**
 * 列表返回对象
 */
open class ListResult<T>(msg: String = "",
                         var total: Int = -1,
                         var data: List<T> = listOf()
) : JsonResult(msg) {
    var value: Any? = null
    var valueRemark: String = ""
    companion object {
        @JvmStatic
        fun <T> of(data: List<T>): ListResult<T> {
            var ret = ListResult<T>();
            ret.data = data;
            ret.total = data.size;
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param valueRemark value值的含义
     * @param value value的值
     */
//    fun <V> setValue(valueRemark: String, value: V): ListResult<T> {
//        var ret = ListResultWithValue<T, V>();
//        ret.set(this, value, valueRemark);
//        return ret;
//    }
    fun setValue(valueRemark: String, value: Serializable): ListResult<T> {
        this.valueRemark = valueRemark;
        this.value = value;
        return this;
    }
}

//open class ListResultWithValue<T, V>() : ListResult<T>() {
//    private var value: V? = null
//    private var valueRemark: String = "";
//
//    fun set(list: ListResult<T> = ListResult<T>(), value: V? = null, valueRemark: String = "") {
//        this.msg = list.msg;
//        this.total = list.total;
//        this.cause = list.cause;
//        this.data = list.data;
//
//        this.value = value;
//        this.valueRemark = valueRemark;
//    }
//}

//class AppListResult<T>(msg: String = "",
//                       var over: Boolean = false, //表示数据已结束。
//                       var data: MutableList<T> = mutableListOf(),
//                       var value: String = ""//返回除 data 额外的信息
//) : JsonResult(msg) {
//}