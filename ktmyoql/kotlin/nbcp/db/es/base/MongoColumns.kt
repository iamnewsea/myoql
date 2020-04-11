package nbcp.db.es

import nbcp.base.extend.MyString

/**
 * Es 多列
 */
class EsColumns(vararg value: EsColumnName ) : ArrayList<EsColumnName>() {
    init{
        this.addAll(value)
    }
    infix fun and (other: EsColumnName) :EsColumns{
        this.add(other);
        return this;
    }
}