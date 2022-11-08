package nbcp.myoql.db.es.enums

enum class EsPutRefreshEnum private constructor(val value: String) {
    True("true"),
    False("false"),
    WaitFor("wait_for")
}