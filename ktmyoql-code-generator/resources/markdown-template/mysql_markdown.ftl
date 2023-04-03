## `${group}` 组

<#list entities as entity>
### `${entity.getName()}` : ${entity.getComment()}

| 列名 | 类型 | 备注 | 主键 | 默认值 |
|---|---|---|---|---|
<#list entity.getColumns() as field>
| ${field.getName()} | ${field.getSqlType()} | ${field.getComment()} | ${field.isPrimary()} | <#if  field.getAutoInc()>自增<#elseif field.getAutoId()>雪花Id字符串<#elseif field.getAutoNumber()>雪花Id数值</#if> |
</#list>


<#if (entity.getUks()?size > 0)>
唯一索引：

<#list entity.getUks() as uk>
    ${uk_index}: ${uk}
</#list>
</#if>
</#list>