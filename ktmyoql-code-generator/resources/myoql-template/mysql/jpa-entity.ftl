package @pkg@

import nbcp.myoql.db.*
import nbcp.db.sql.*
import nbcp.db.mysql.*
import java.time.*

/**
* Created by ${user} at ${now}
*/
@DbEntityGroup("${entity.group}")
@Cn("${entity.getComment()}")
@DbUks(${entity.getUks()?join(",")})
@DbName("${entity.getName()}")
open class ${entity.getClassName()}(): ISqlDbEntity {
<#list entity.getColumns() as field>
<#if  field.getAutoInc()>
    @SqlAutoIncrementKey
<#elseif field.getAutoId()>
    @ConverterValueToDb(AutoIdConverter::class)
<#elseif field.getAutoNumber()>
    @ConverterValueToDb(AutoNumberConverter::class)
</#if>
    @Cn("${field.getComment()}")
    @DbName("${field.getName()}")
    var ${field.getFieldName()}: ${field.getKotlinType()} = ${field.getKotlinDefaultValue()}
</#list>
}