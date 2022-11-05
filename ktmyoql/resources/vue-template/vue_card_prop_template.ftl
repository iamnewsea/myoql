<template>
    <div class="card-page">
        <el-row :gutter="12" type="flex">
            <el-col>
                <el-card shadow="always">
                    <div slot="header" v-if="title">
                        <span>{{title}}</span>
                    </div>
<#list fields as field>
    <#if field.getName() == "id">
    <#elseif field.getName() == "creator" || field.getName() == "createBy">
    <#elseif field.getName() == "updater" || field.getName() == "updateBy">
    <#elseif field.getName() == "createAt">
    <#elseif field.getName() == "updateAt">
    <#elseif field.getType().isEnum()>
                    <kv label="${field_cn(field)}">
                        <selector v-model="info.${field.getName()}" enum="${field.getType().getSimpleName()}" chk="*"/>
                    </kv>
    <#elseif field_is_enum_list(field)>
                    <kv label="${field_cn(field)}">
                        <selector multi value-is-object v-model="info.${field.getName()}" enum="${field_list_type(field)}" />
                    </kv>
    <#elseif is_type(field,"IdUrl")>
                    <kv>
                        <label slot="k">${field_cn(field)}</label>
                        <upload
                                :maxCount="1"
                                v-model="info.${field.getName()}"
                                fileType="img"
                                scales="16:9"
                                :maxWidth="1024"
                                maxSize="5M"
                        ></upload>
                    </kv>
    <#elseif field_is_list(field,"IdUrl")>
                    <kv>
                        <label slot="k">${field_cn(field)}</label>
                        <upload
                                :maxCount="99"
                                v-model="info.${field.getName()}"
                                fileType="img"
                                scales="16:9"
                                :maxWidth="1024"
                                maxSize="5M"
                        ></upload>
                    </kv>
    <#elseif is_type(field,"IdName")>
                    <kv label="${field_cn(field)}">
                        <ref-${k(field.getName())} v-model="info.${field.getName()}"></ref-${k(field.getName())}>
                    </kv>
    <#elseif is_type(field,"boolean")>
                    <kv label="${field_cn(field)}">
                        <selector v-model="info.${field.getName()}" :data="{true:'是',false:'否','':'全部'}" />
                    </kv>
    <#elseif is_type(field,"LocalDate")>
                    <kv label="${field_cn(field)}">
                        <el-date-picker v-model="info.${field.getName()}" placeholder="选择日期" />
                    </kv>
    <#elseif is_type(field,"LocalDateTime")>
                    <kv label="${field_cn(field)}">
                        <el-date-picker v-model="info.${field.getName()}" placeholder="选择日期时间"  type="datetime" />
                    </kv>
    <#elseif is_type(field,"LocalTime")>
                    <kv label="${field_cn(field)}">
                        <el-time-select v-model="info.${field.getName()}" placeholder="选择时间" />
                    </kv>
    <#elseif field.getName() == "name">
                    <kv label="${field_cn(field)}">
                        <el-input v-model="info.${field.getName()}" chk="*" />
                    </kv>
    <#elseif field.getName() == "remark">
        <kv label="${field_cn(field)}">
            <el-input v-model="info.${field.getName()}" type="textarea"/>
        </kv>
    <#else>
                    <kv label="${field_cn(field)}">
                        <el-input v-model="info.${field.getName()}" />
                    </kv>
</#if>
</#list>
                </el-card>
            </el-col>

        </el-row>
    </div>
</template>
<style scoped>
</style>
<script>
/**
 * Created by CodeGenerator  at ${now}
 */
<#list fields as field>
<#if field.getName() == "creator" || field.getName() == "createBy">
<#elseif field.getName() == "updater" || field.getName() == "updateBy">
<#elseif is_type(field,"IdName")>
import Ref${W(field.getName())} from "@/component/empty-ref"
</#if>
</#list>
export default {
    components: {<#list fields as field><#if field.getName() == "creator" || field.getName() == "createBy" || field.getName() == "updater" || field.getName() == "updateBy"><#elseif is_type(field,"IdName")>
        "ref-${k(field.getName())}": Ref${W(field.getName())},
</#if></#list>},
    data() {
        return {
<#list fields as field>
<#if field.getType().isEnum()>
            ${field.getType().getSimpleName()}: jv.enum.${field.getType().getSimpleName()}.getData(),
<#elseif field_is_enum_list(field)>
            ${field_list_type(field)}: jv.enum.${field_list_type(field)}.getData(),
</#if>
</#list>
            info: {
<#list fields as field>
<#if field_is_list(field,"Object")>
                ${field.getName()}: [],
<#elseif is_object(field)>
                ${field.getName()}: {},
</#if>
</#list>
            }, //子对象需要声明。
        }
    },
    props: {
        value: {type: Object, default:()=>  { return {} }},
        title: {type:String, default: ""}
    },
    watch:{
        value: {
            immediate: true, handler(v) {
                if( this.info == v) return;
                this.info = v;
            }
        },
        info:{
            immediate: true, handler(v) {
                if( this.info == v) return;
                this.$emit("input",v)
            }
        }
    }
}
</script>
