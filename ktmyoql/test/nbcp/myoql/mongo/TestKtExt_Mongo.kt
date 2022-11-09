package nbcp.myoql.mongo

import nbcp.myoql.TestBase
import nbcp.base.comm.*
import nbcp.myoql.db.db

import nbcp.myoql.db.mongo.*

class TestKtExt_Mongo : TestBase() {

    fun agg() {
        db.morBase.sysCity.aggregate()
            .group { JsonMap() }
    }
}