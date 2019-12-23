package nbcp.db.mongo

/**
 * https://docs.mongodb.com/manual/reference/operator/query-modifier/
 */
enum class QueryModifierEnum {
    comment,
    explain,
    hint,
    max,
    maxTimeMS,
    min,
    orderby,
    query,
    returnKey,
    showDiskLoc,
    natural
}