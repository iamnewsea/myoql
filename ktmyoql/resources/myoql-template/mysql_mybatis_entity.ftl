package ${package};

import ${package_base}.*;
import lombok.*;
import java.time.*;
import java.util.*;
import nbcp.db.*;
import java.lang.*;

/**
* Created by CodeGenerator at ${now}
*/
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode()
@DbName("${entity.getName()}")
@Cn("${entity.getComment()}")
public class ${W(entity.getName())} {
<#list entity.getColumns() as field>
    /** ${field.getComment()} */
    @Cn("${field.getComment()}")
    @DbName("${field.getName()}")
    public ${field.getJavaType()} ${field_name(field.getName())};
</#list>
}
