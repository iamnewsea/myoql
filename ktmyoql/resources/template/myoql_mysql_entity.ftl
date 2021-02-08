package nbcp.db.mysql.entity

import nbcp.db.*
import nbcp.db.sql.*
import nbcp.db.mysql.*
import java.time.*

/**
* Created by CodeGenerator at ${now}
*/

<#list entitys as entity>
@DbEntityGroup("${entity.group}")
@Cn("${entity.getComment()}")
@DbUks(${entity.getUks()?join(",")})
data class ${entity.getName()}(): ISqlDbEntity {
<#list entity.getColumns() as field>
<#if  field.getAuto_inc()>
    @SqlAutoIncrementKey
<#elseif field.getAuto_id()>
    @ConverterValueToDb(AutoIdConverter::class)
<#elseif field.getAuto_number()>
    @ConverterValueToDb(AutoNumberConverter::class)
</#if>
    @Cn("${field.getComment()}")
    var ${field.getName()}: ${field.getKotlin_type()} = ${field.getKotlin_default_value()}
</#list>
}
</#list>