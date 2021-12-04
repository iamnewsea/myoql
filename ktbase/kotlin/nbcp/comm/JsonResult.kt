package nbcp.comm

import java.io.Serializable
import java.lang.RuntimeException


/**
 * Created by jin on 2017/3/16.
 */

/**
 * 普通的返回对象。
 */
open class JsonResult() : Serializable {
    var code: Int = 0;
    var msg: String = ""
        get() {
            return field
        }
        set(value) {
            if (value.isEmpty()) code = 0
            else if (value.HasValue && code == 0) code = -1
            field = value;
        }

    var cause: String? = null

    companion object {
        @JvmStatic
        fun error(msg: String, code: Int = 0): JsonResult {
            var ret = JsonResult();
            ret.code = code;
            ret.msg = msg;
            return ret;
        }
    }
}

open class ApiResult<T>() : JsonResult() {
    var data: T? = null
    var value: Any? = null

    companion object {
        @JvmStatic
        fun <T> error(msg: String, code: Int = 0): ApiResult<T> {
            var ret = ApiResult<T>();
            ret.code = code;
            ret.msg = msg;
            return ret;
        }

        @JvmStatic
        fun <T> of(data: T?): ApiResult<T> {
            var ret = ApiResult<T>();
            ret.data = data;
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param value value的值
     */
    fun setValue(value: Any): ApiResult<T> {
        this.value = value;
        return this;
    }
}


class ParameterInvalidException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("参数非法"))

class NoDataException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("找不到数据"))

class ExecuteDbException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("操作数据库失败"))

class ServerException @JvmOverloads constructor(msg: String = "") :
    RuntimeException(msg.AsString("服务器异常"))

/**
 * 查询对象
 */

open class SortQueryItemModel {
    var field: String = "";
    var asc: Boolean = false
}

open class ListQueryModel {
    var skip: Int = 0;
    var take: Int = -1;
    var sorts = mutableListOf<SortQueryItemModel>()
}

/**
 * 列表返回对象
 */
open class ListResult<T>(
    //@Transient val clazz: Class<T>
) : JsonResult() {

    var total: Int = -1
    var data: List<T> = listOf()
    var value: Any? = null

    companion object {
        @JvmStatic
        @JvmOverloads
        inline fun <reified T> error(msg: String, code: Int = 0): ListResult<T> {
            var ret = ListResult<T>();
            ret.code = code;
            ret.msg = msg;
            return ret;
        }

        @JvmStatic
        @JvmOverloads
        fun <T> of(data: Collection<T>, total: Int = -1): ListResult<T> {
            var ret = ListResult<T>();
            ret.data = data.toList();
            if (total < 0) {
                ret.total = data.size;
            } else {
                ret.total = total;
            }
            return ret;
        }
    }

    /** 设置额外value的值。
     * @param value value的值
     */
    fun setValue(value: Serializable): ListResult<T> {
        this.value = value;
        return this;
    }
}


/**
 * 为了接收数据方便
 */
class TypedMapResult<T>() : JsonMap() {
    var msg: String
        get() {
            return this.get("msg").AsString();
        }
        set(value) {
            this.set("msg", value);
        }

    var code: Int
        get() {
            return this.get("code").AsInt();
        }
        set(value) {
            this.set("code", value);
        }

    var data: T?
        get() {
            val _data = this.get("data");
            if (_data == null) return null;
            return _data as T
        }
        set(value) {
            this.set("data", value);
        }
}