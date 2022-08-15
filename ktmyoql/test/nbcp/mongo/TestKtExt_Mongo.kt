package nbcp.mongo

import nbcp.TestBase
import nbcp.comm.*
import nbcp.db.db
import nbcp.db.mongo.*

class TestKtExt_Mongo : TestBase() {

    fun agg() {
        db.morBase.sysCity.aggregate()
            .group { JsonMap() }
    }
}