//package nbcp.db.sql.component
//
//import nbcp.comm.*
//import nbcp.base.extend.AsString
//import nbcp.db.mysql.*
//import java.util.concurrent.Executors
//import java.util.concurrent.Future
//import nbcp.db.sql.*
///**
// * Created by yuxh on 2019/4/16
// */
//
//class SqlParallelQuery<M : SqlBaseTable<out T>, T : IBaseDbEntity>(var mainEntity: M, var baseQuery: SqlQueryClip<M, T>, var parallelQueryParam: Map<String, Int> = mapOf()) : SqlBaseQueryClip(mainEntity) {
//    override fun toSql(): SingleSqlData {
//        return baseQuery.toSql()
//    }
//
//    override fun toMap(): JsonMap? {
//        return baseQuery.toMap();
//    }
//
//    override fun exists(): Boolean {
//        val executor = Executors.newFixedThreadPool(parallelQueryParam.size)
//        var resultList = mutableListOf<Future<Boolean>>();
//        for (request in parallelQueryParam) {
//            resultList.add(executor.submit<Boolean> {
//                baseQuery.datasourceName = request.key;
//                return@submit baseQuery.exists()
//            })
//        }
//
//        var ret = false;
//        var done = false;
//        for (result in resultList) {
//            if (done) {
//                result.cancel(true);
//                continue;
//            }
//
//            ret = result.get()
//            if (ret == true) {
//                done = true;
//            }
//        }
//
//        return ret;
//    }
//
//
//    fun count(countQuery: ((SqlQueryClip<M, T>) -> Unit)? = null): Int {
//        val executor = Executors.newFixedThreadPool(parallelQueryParam.size)
//        var resultList = mutableListOf<Future<Int>>();
//        for (request in parallelQueryParam) {
//            resultList.add(executor.submit<Int> {
//                baseQuery.datasourceName = request.key;
//                return@submit baseQuery.count(countQuery)
//            })
//        }
//
//        var ret = 0;
//        for (result in resultList) {
//            ret += result.get()
//        }
//
//        return ret;
//    }
//
//
//    fun <R : Any> toListResult(entityClass: Class<R>, idValue: ((R) -> String), sort: Comparator<R>, countQuery: ((SqlQueryClip<M, T>) -> Unit)? = null): ListResult<R> {
//        var skipSum = parallelQueryParam.values.sum();
//
//        val executor = Executors.newFixedThreadPool(parallelQueryParam.size)
//        var resultList = mutableListOf<Future<MutableList<R>>>();
//        for (request in parallelQueryParam) {
//            resultList.add(executor.submit<MutableList<R>> {
//                baseQuery.datasourceName = request.key;
//                baseQuery.skip(request.value);
//                return@submit baseQuery.toList(entityClass)
//            })
//        }
//
//
//        var meta = linkedMapOf<String, String>();  //key:id , value:datasource
//        var allData = mutableListOf<R>();
//
//        var index = -1;
//        for (result in resultList) {
//            index++;
//
//            var r = result.get()
//
//            r.forEach {
//                meta.put(idValue(it), parallelQueryParam.keys.elementAt(index));
//            }
//
//            allData.addAll(r);
//        }
//
//        //仅支持简单的排序. 不是很影响结果.但影响输出的排序. , 使用自定义排序.
//        allData.sortWith(sort)
//
//        var takeData = allData.take(take);
//        var ret = ListResult<R>();
//        ret.data = takeData.toMutableList();
//
//
//        var parallelQueryResult = parallelQueryParam.values.toMutableList();
//        takeData.forEach {
//            var id = idValue(it);
//            var datasource =meta.get(id).AsString()
//            var dataSourceIndex = parallelQueryParam.keys.indexOf(datasource)
//            parallelQueryResult[dataSourceIndex] += 1;
//        }
//
//        if (skipSum == 0) {
//            if (ret.data.size < this.take) {
//                ret.total = ret.data.size;
//            } else {
//                ret.total = count(countQuery)
//            }
//        }
//
//        ret.value = parallelQueryResult;
//        return ret;
//    }
//
//}