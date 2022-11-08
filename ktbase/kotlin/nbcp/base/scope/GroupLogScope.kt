package nbcp.base.comm

import nbcp.base.aop.GroupLog
import nbcp.base.scope.IScopeData


data class GroupLogScope @JvmOverloads constructor(var value: String = "") : IScopeData {
    companion object {
        fun of(groupLog: GroupLog): GroupLogScope {
            return GroupLogScope(groupLog.value)
        }
    }
}