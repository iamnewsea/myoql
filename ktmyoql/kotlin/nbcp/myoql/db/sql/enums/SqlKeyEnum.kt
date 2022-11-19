package nbcp.myoql.db.sql.enums


enum class SqlKeyEnum {
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    WITH,
    CALL,
    INTO,

    FROM,
    JOIN,
    WHERE,
    GROUP_BY,
    ORDER_BY,
    HAVING,
    LIMIT,
    OFFSET,

    Union,
    Values,
    Set,
    Other,
}

enum class SqlLockType{
    ShareMode,
    Update
}