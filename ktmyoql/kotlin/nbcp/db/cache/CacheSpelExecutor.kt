package nbcp.db.cache

import nbcp.comm.JsonMap
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext


class CacheSpelExecutor(val variableMap: JsonMap) {
    var spelExpressionParser = SpelExpressionParser();
    var context = StandardEvaluationContext()

    init {
        //添加 app 的系统配置项 及 上下文,系统配置项以 app 开头
//        var items = SpringUtil.context.environment.getProperty("app.cache.variables").AsString("app").split(",")
//
//        items.forEach { variablekey ->
//            var appMap = SpringUtil.binder.bindOrCreate(variablekey, JsonMap::class.java)
//            variableMap.put(variablekey, appMap);
//        }


        variableMap.forEach {
            context.setVariable(it.key, it.value);
        }
    }

    fun getVariableValue(it: String): String {
        var retString = it;
        if (it.contains("#")) {
            retString = spelExpressionParser.parseExpression(it).getValue(context, String::class.java)
        }

        //转为全角
        return retString
            .replace(":", "：")
            .replace("|", "｜")
            .replace("@", "＠")
            .replace(" ", "")
            .replace("　", "")
            .replace("\t", "")
            .replace("\r", "")
            .replace("\n", "")
    }
}