package nbcp.myoql.mongo

import nbcp.base.comm.JsonMap
import nbcp.myoql.TestBase
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.aggregate

class TestKtExt_Mongo : TestBase() {

    fun agg() {
        db.morBase.sysCity.aggregate()
            .rawGroup { JsonMap() }
    }
}