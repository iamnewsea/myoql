<#list entitys as entity>
### 表: ${entity.getName()}
${entity.getComment()}
列名 | 类型 | 备注 | 主键 | 默认值
---|---|---|---|---
<#list entity.getColumns() as field>
${field.getName()} | ${field.getSql_type()} | ${field.getComment()} | | <#if  field.getAuto_inc()>自增<#elseif field.getAuto_id()>系统生成字符串雪花Id<#elseif field.getAuto_number()>系统生成数值型雪花Id</#if>
</#list>
</#list>