package nbcp.db.cache

import nbcp.comm.StringMap

data class UrkInfo(val rks: StringMap, val uks: StringMap, val rksValid: Boolean, val uksValid: Boolean)

