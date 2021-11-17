package nbcp.db.es.component

import nbcp.comm.JsonMap
import nbcp.db.es.WhereData

/**
 * 先满足基本的查询
 */
class EsQueryData {
    /*
"query": {
    "bool": {
        "should": [{
                "term": {
                    "title":"产品"
                }
            }
        ]
    }
}
     */


    class BooleanData {
        var should: MutableList<JsonMap>? = null
        var must: MutableList<JsonMap>? = null

        fun hasValue(): Boolean {
            if (should.isNullOrEmpty() == null) return true;
            if (must.isNullOrEmpty() == null) return true;


            return false;
        }

        fun addShould(vararg where: WhereData){
            if( this.should == null){
                this.should = mutableListOf();
            }

            this.should!!.addAll(where);
        }

        fun addMust(vararg where: WhereData){
            if( this.must == null){
                this.must = mutableListOf();
            }

            this.must!!.addAll(where);
        }
    }

    var bool = BooleanData()



    fun hasValue(): Boolean {
        if (bool.hasValue()) return true;

        return false;
    }
}