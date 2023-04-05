# `${group}` 组

<#list entities as entity>
## `${entity.getName()}` : ${entity.getComment()}

| 序号 | 列名 | 类型 | 备注 | 索引 |
|---|---|---|---|---|
<#list entity.getColumns() as field>
| ${style(field,"index",field.getIndex())} | ${style(field,"name",field.getName())} | ${style(field,"sqlType",field.getSqlType())} | ${style(field,"remark",field.getRemark())} | ${style(field,"index",field.isPrimary())} |
</#list>


<#if (entity.getAuks()?size > 0)>
联合唯一索引：

<#list entity.getAuks() as uk>
    ${uk_index}: ${uk}
</#list>
</#if>
</#list>