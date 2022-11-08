package nbcp.base.scope

/**
 * 在作用域中添加 String 类型的值。
 * usingScope(StringScopeData("key","value")){
 *  scopes.getLatestStringScope("key")
 * }
 */
data class StringScopeData(var key: String, var value: String): IScopeData

