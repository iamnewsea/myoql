package nbcp.base.extend

import nbcp.base.utils.MyUtil
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
//import javax.servlet.ServletRequest
//import javax.servlet.http.HttpServletRequest
//import javax.servlet.http.HttpServletResponse
//
///**
// * Created by udi on 17-3-19.
// *
// * 该扩展保存用户登录之后的 Session项。
// */
//
//val HttpServletResponse.IsOctetContent: Boolean
//    get() {
//        if (this.contentType == null) {
//            return false
//        }
////http://tool.oschina.net/commons
//        for (c in arrayOf<String>("stream", "flash", "drawing", "image", "audio", "video", "pdf", "msword", "excel", "powerpoint")) {
//            if (this.contentType.indexOf(c, 0, true) >= 0) return true;
//        }
//        return false;
//    }


val lockMaps: HashMap<String, ReentrantLock> = hashMapOf()

/**
 * 使用方法：请在方法的类型上使用Lock
 * return this::function::class.java.Lock {
 *      doSomething();
 *      returnValue;
 * };
 * 在	Kotlin	中有一个约定,如果函数的最后一个参数是一个函数,并且你传递一个	lambda	表达式作为相应的参数,你可以在圆括号之外指定它:
 */
inline fun <R> Class<out Any>.Lock(body: () -> R): R {
    var key = this.name;
    if (lockMaps.containsKey(key) == false) {
        lockMaps.put(key, ReentrantLock())
    }

    var lock = lockMaps.get(key)!!;
    lock.lock();
    try {
        return body();
    } finally {
        lock.unlock();
    }
}

//inline fun <R> Lock.Lock(body: () -> R): R {
//    this.lock();
//    try {
//        return body();
//    } finally {
//        this.unlock();
//    }
//}


//var RequestId = 0;
//val ServletRequest.MyId: Int
//    get() {
//        this::MyId::class.java.Lock {
//            var ret = this.getAttribute("MyId");
//            if (ret == null) {
//                RequestId = (++RequestId) % 10000;
//                this.setAttribute("MyId", RequestId.toString());
//            }
//        }
//
//        return this.getAttribute("MyId").toString().toInt()
//    }
//
//val HttpServletRequest.ClientIp: String
//    get() {
//        var value = this.getAttribute("_ClientIp").AsString().trim()
//        if (value.isEmpty()) {
//            value = IpUtil.getClientIp(this);
//
//            this.setAttribute("_ClientIp", value);
//        }
//        return value;
//    }
//
