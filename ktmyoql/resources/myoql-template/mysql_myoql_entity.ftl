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
open class ${entity.getName()}(): ISqlDbEntity {
<#list entity.getColumns() as field>
<#if  field.getAutoInc()>
    @SqlAutoIncrementKey
<#elseif field.getAutoId()>
    @ConverterValueToDb(AutoIdConverter::class)
<#elseif field.getAutoNumber()>
    @ConverterValueToDb(AutoNumberConverter::class)
</#if>
    @Cn("${field.getComment()}")
    var ${field.getName()}: ${field.getKotlinType()} = ${field.getKotlinDefaultValue()}
</#list>
}
</#list>