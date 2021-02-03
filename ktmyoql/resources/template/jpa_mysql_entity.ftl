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
public class ${W(entity.getName())} extends BaseEntity {
<#list entity.getColumns() as field>
<#if is_in(field.getName(),"id","create_at","update_at","create_user_id","create_user_name","update_user_id","update_user_name","del_flag")>
<#else>
    /** ${field.getComment()} */
    @Cn("${field.getComment()}")
    @Column(name = "${field.getName()}")
    private ${field.getJava_type()} ${field.getName()};
</#if>
</#list>
}
