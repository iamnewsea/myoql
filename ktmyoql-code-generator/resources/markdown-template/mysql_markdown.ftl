# `${group}` 组

<#list entities as entity>
## `${entity.getName()}` : ${entity.getComment()}

| 序号 | 列名 | 类型 | 主键 | 备注 |
|---|---|---|---|---|
<#list entity.getColumns() as field>
| ${style(field,field.getIndex())} | ${style(field,field.getName())} | ${style(field,field.getSqlType())} | ${style(field,field.isPrimary())} | ${style(field,field.getRemark())} |
</#list>


<#if (entity.getUks()?size > 0)>
唯一索引：

<#list entity.getUks() as uk>
    ${uk_index}: ${uk}
</#list>
</#if>
</#list>