//package nbcp.db
//
//import nbcp.myoql.db.mongo.MongoBaseQueryClip
//import org.bson.Document
//
//
//class EventChain (val query:MongoBaseQueryClip){
//    var oriData:List<Document>? = null;
//
//    fun loadOriDbData(){
//        if( oriData != null){
//            return ;
//        }
//
//        oriData = query.toMapList()
//    }
//}