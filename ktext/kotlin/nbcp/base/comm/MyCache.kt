//package nbcp.base.comm
//
//import nbcp.base.extend.HasValue
//import nbcp.base.extend.IsIn
//import nbcp.base.extend.Lock
//import nbcp.base.extend.RemoveRange
//import nbcp.db.mongo.*
//import java.util.concurrent.CopyOnWriteArrayList
//import java.util.concurrent.locks.ReentrantLock
//
///**
// * Created by udi on 17-6-10.
// */
//
//
////缓存管理
//object MyCache {
//    data class MyCacheDataModel(
//            var entityName: String,
//            var cacheKey: String,
//            var value: String,
//            var cacheSecond: Int)
//
//
//    private val cacheData = mutableListOf<MyCacheDataModel>()
//    private val toRemoveCacheKeys = mutableListOf<String>()
//
//    private var working = false
//
//
//    private fun toWorking() {
//        if (working) return;
//        working = true;
//
//        //真正的逻辑
//        var doIt: () -> Unit = {
//            //先标记，再把 key 打包。
//            var maxCacheIndex = cacheData.size - 1;
//            var maxRemoveCacheIndex = toRemoveCacheKeys.size - 1;
//
//
//            var emptyGroupCacheEntityName = toRemoveCacheKeys
//                    .distinct()
//                    .toList()
//
//            //移除所有的 emptyGroupCacheEntityName
//
//            var cacheData2 = mutableListOf<MyCacheDataModel>()
//            var toRemoveCacheKeys2 = mutableListOf<String>()
//
//            toRemoveCacheKeys.forEach {
//                var key = it
//                if (emptyGroupCacheEntityName.any { it == key } == false) {
//                    return@forEach
//                }
//
//                if (toRemoveCacheKeys2.any { it == key  } == false) {
//                    return@forEach
//                }
//
//                toRemoveCacheKeys2.add(key)
//            }
//
//            cacheData.forEach {
//                var key = it
//                if (emptyGroupCacheEntityName.any { it == key.entityName } == false) {
//                    return@forEach
//                }
//
//                if (toRemoveCacheKeys2.any { it == key.entityName} == false) {
//                    return@forEach
//                }
//
//                cacheData2.add(key)
//            }
//
////            var index = maxRemoveCacheIndex;
////            while (true) {
////                if (index < 0) {
////                    break;
////                }
////
////                var key = toRemoveCacheKeys[index];
////                if (key.entityName.IsIn(emptyGroupCacheEntityName)) {
////                    toRemoveCacheKeys.removeAt(index);
////                }
////                index--;
////            }
////
////            //先把 cacheData在 remoev中的去掉。
////            index = maxCacheIndex;
////            while (true) {
////                if (index < 0) {
////                    break;
////                }
////
////                var key = cacheData[index];
////                if (key.entityName.IsIn(emptyGroupCacheEntityName)) {
////                    cacheData.removeAt(index);
////                } else if (toRemoveCacheKeys.any { it.entityName == key.entityName && it.group == key.group }) {
////                    cacheData.removeAt(index);
////                }
////                index--;
////            }
//
//            //准备好了。
//            if (this.addFunc != null) {
//                cacheData2.forEach { this.addFunc!!(it) }
//            }
//
//            if (this.removeFunc != null) {
//                toRemoveCacheKeys2.forEach { this.removeFunc!!(it) }
//            }
//
//            if (maxRemoveCacheIndex >= 0) {
//                toRemoveCacheKeys.RemoveRange(0, maxRemoveCacheIndex);
//            }
//
//            if (maxCacheIndex >= 0) {
//                cacheData.RemoveRange(0, maxCacheIndex);
//            }
//        }
//
//        var _doItWrapper: () -> Unit = {};
//        var doItWrapper: () -> Unit = {
//            working = true;
//            AsyncTask.execute {
//                Thread.sleep(500);
//                doIt();
//                _doItWrapper();
//            }
//        }
//        _doItWrapper = doItWrapper;
//
//        doItWrapper();
//    }
//
//    //添加一项
//    fun add(entityName: String,   cacheKey: String, value: String, cacheSecond: Int) {
//        if(enabled == false) return
//        cacheData.add(MyCacheDataModel(entityName,  cacheKey, value, cacheSecond));
//
//        toWorking()
//    }
//
//    fun remove(entityName: String ) {
//        if(enabled == false) return
//
//        toRemoveCacheKeys.add( entityName )
//        toWorking()
//    }
//
//    /**
//     */
//    fun find(entityName: String, cacheKey: String): String {
//        if(enabled == false) return "";
//
//        var value = cacheData.firstOrNull { it.entityName == entityName && it.cacheKey == cacheKey }?.value
//        if (value != null) {
//            return value;
//        }
//
//        if (findFunc != null) {
//            return findFunc!!(entityName,  cacheKey)
//        }
//        return ""
//    }
//
//
//    private var addFunc: ((MyCacheDataModel) -> Unit)? = null
//    private var removeFunc: ((String) -> Unit)? = null
//    private var findFunc: ((String, String) -> String)? = null
//    private var groupDict: HashMap<String, String> = linkedMapOf()
//
//    var enabled = false;
//
//    fun init(groupDict: HashMap<String, String>,
//             addFunc: ((MyCacheDataModel) -> Unit)? = null,
//             removeFunc: ((String) -> Unit)? = null,
//             findFunc: ((String, String) -> String)? = null) {
//        this.groupDict = groupDict
//        this.addFunc = addFunc
//        this.removeFunc = removeFunc
//        this.findFunc = findFunc
//    }
//}