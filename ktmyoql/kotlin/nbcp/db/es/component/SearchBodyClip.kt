package nbcp.db.es

import nbcp.comm.*

//class SearchQueryClip {
//
//}

//class SearchSortClip: JsonMap() {
//
//}

//class SearchFilterClip :JsonMap(){
//
//
//}

class SearchBodyClip {
    var from = 0L;
    var size = -1;
    /**
    "query": {
    "bool": {
    "should": [
    {
    "term": {
    "title": "中国"
    }
    },
    {
    "term": {
    "content": "中国"
    }
    }
    ]
    }
    },
     */
    var query: JsonMap? = null
    /*
"aggs":{
  "avg_fees":{"avg":{"field":"fees"}}
}
     */
    var aggs: JsonMap? = null
    val fields = mutableListOf<String>()

    /**
    "sort": [
    {
    "publish_date": {
    "order": "asc"
    }
    },
    "_score"
    ],
     */
    val sort = mutableListOf<JsonMap>()


    /**
    "filter": {
    "range": {
    "publish_date": {
    "from": "2010/07/01",
    "to": "2010/07/21",
    "include_lower": true,
    "include_upper": false
    }
    }
    }
     */
    val filter = JsonMap()

    /*
 "highlight": {
    "pre_tags": [
      "<tag1>",
      "<tag2>"
    ],
    "post_tags": [
      "</tag1>",
      "</tag2>"
    ],
    "fields": {
      "title": {},
      "content": {}
    }
  },
     */
    val highlight = JsonMap()

    /*
"facets": {
    "cate": {
      "terms": {
        "field": "category"
      }
    }
  }
     */
    val facets = JsonMap();


    override fun toString(): String {
        return this.ToJson()
    }

    fun isEmpty(): Boolean {
        return false;
    }
}