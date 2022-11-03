package nbcp.db.sql


enum class SqlKeyEnum {
    Select,
    Insert,
    Update,
    Delete,
    With,
    Call,
    Into,

    From,
    Join,
    Where,
    GroupBy,
    OrderBy,
    Having,
    Limit,
    Offset,

    Union,
    Values,
    Set,
    Other,
}

enum class SqlLockType{
    ShareMode,
    Update
}