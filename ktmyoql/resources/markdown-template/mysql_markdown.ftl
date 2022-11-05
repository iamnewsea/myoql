<#list entitys as entity>
### 表: ${entity.getName()}
${entity.getComment()}
列名 | 类型 | 备注 | 主键 | 默认值
---|---|---|---|---
<#list entity.getColumns() as field>
${field.getName()} | ${field.getSqlType()} | ${field.getComment()} | | <#if  field.getAutoInc()>自增<#elseif field.getAutoId()>系统生成字符串雪花Id<#elseif field.getAutoNumber()>系统生成数值型雪花Id</#if>
</#list>

唯一索引：
<#list entity.getUks() as uk>
    ${uk_index}. ${uk}
</#list>
</#list>