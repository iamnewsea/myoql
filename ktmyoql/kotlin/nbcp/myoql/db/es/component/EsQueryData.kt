package nbcp.myoql.db.es.component

import nbcp.base.comm.JsonMap

/**
 * 先满足基本的查询
 */
class EsQueryData : JsonMap() {
    /*
    查询方式一：
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
查询方式二：https://www.cnblogs.com/yjf512/p/4897294.html
{
  "query": {
    "multi_match": {
        "query" : "我的宝马多少马力",
        "fields" : ["title", "content"]
    }
  }
}

     */


    class BooleanData {
        var should: MutableList<JsonMap>? = null
        var must: MutableList<JsonMap>? = null
        var minimum_should_match: Int? = null

        fun hasValue(): Boolean {
            if (!should.isNullOrEmpty()) return true;
            if (!must.isNullOrEmpty()) return true;


            return false;
        }

        fun addShould(vararg where: WhereData) {
            if (this.should == null) {
                this.should = mutableListOf();
                this.minimum_should_match = 1;
            }

            this.should!!.addAll(where);
        }

        fun addMust(vararg where: WhereData) {
            if (this.must == null) {
                this.must = mutableListOf();
            }

            this.must!!.addAll(where);
        }
    }

    companion object {
        private fun <T> MutableMap<String,Any?>.touchMap(fieldName: String, callback: () -> T): T {
            var b = this.get(fieldName);
            if (b != null) {
                return b as T;
            }

            this.set(fieldName, callback())
            return this.get(fieldName) as T;
        }
    }

    fun addShould(vararg where: WhereData) {
        var b = this.touchMap("bool", { JsonMap() });
        var should = b.touchMap("should", { mutableListOf<JsonMap>() });

        b.set("minimum_should_match", 1);

        should.addAll(where);
    }

    fun addMust(vararg where: WhereData) {
        var b = this.touchMap("bool", { JsonMap() });
        var must = b.touchMap("must", { mutableListOf<JsonMap>() });


        must.addAll(where);
    }

    fun addMustNot(vararg where: WhereData) {
        var b = this.touchMap("bool", { JsonMap() });
        var must = b.touchMap("must_not", { mutableListOf<JsonMap>() });

        must.addAll(where);
    }
    fun hasValue(): Boolean {
        if (this.keys.size == 1 && this.containsKey("bool")) {
            return (this.get("bool") as JsonMap).isNotEmpty()
        }

        return isNotEmpty();
    }
}