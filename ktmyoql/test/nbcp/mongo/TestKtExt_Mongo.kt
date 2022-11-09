package nbcp.mongo

import nbcp.base.TestBase
import nbcp.base.comm.*
import nbcp.myoql.db.db

import nbcp.myoql.db.mongo.*

class TestKtExt_Mongo : TestBase() {

    fun agg() {
        db.morBase.sysCity.aggregate()
            .group { JsonMap() }
    }
}