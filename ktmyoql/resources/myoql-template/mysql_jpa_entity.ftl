package ${package}

import ${package_base}.*;
import lombok.*;
import javax.persistence.*;
import javax.persistence.*;
import java.time.*;
import java.util.*;

/**
* Created by CodeGenerator at ${now}
*/
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Entity(name = "${entity.getName()}")
@Cn("${entity.getComment()}")
public class ${W(entity.getName())} {
<#list entity.getColumns() as field>
    /** ${field.getComment()} */
    @Cn("${field.getComment()}")
    @Column(name = "${field.getName()}")
    public ${field.getJavaType()} ${field.getName()} = ${field.getJavaDefaultValue()};
</#list>
}
