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
                    <kv label="${fieldCn(field)}">
                        <selector v-model="info.${field.getName()}" enum="${field.getType().getSimpleName()}" chk="*"/>
                    </kv>
    <#elseif fieldIsEnumList(field)>
                    <kv label="${fieldCn(field)}">
                        <selector multi value-is-object v-model="info.${field.getName()}" enum="${fieldListType(field)}" />
                    </kv>
    <#elseif isType(field,"IdUrl")>
                    <kv>
                        <label slot="k">${fieldCn(field)}</label>
                        <upload
                                :maxCount="1"
                                v-model="info.${field.getName()}"
                                fileType="img"
                                scales="16:9"
                                :maxWidth="1024"
                                maxSize="5M"
                        ></upload>
                    </kv>
    <#elseif fieldIsList(field,"IdUrl")>
                    <kv>
                        <label slot="k">${fieldCn(field)}</label>
                        <upload
                                :maxCount="99"
                                v-model="info.${field.getName()}"
                                fileType="img"
                                scales="16:9"
                                :maxWidth="1024"
                                maxSize="5M"
                        ></upload>
                    </kv>
    <#elseif isType(field,"IdName")>
                    <kv label="${fieldCn(field)}">
                        <ref-${kb(field.getName())} v-model="info.${field.getName()}"></ref-${kb(field.getName())}>
                    </kv>
    <#elseif isType(field,"boolean")>
                    <kv label="${fieldCn(field)}">
                        <selector v-model="info.${field.getName()}" :data="{true:'是',false:'否','':'全部'}" />
                    </kv>
    <#elseif isType(field,"LocalDate")>
                    <kv label="${fieldCn(field)}">
                        <el-date-picker v-model="info.${field.getName()}" placeholder="选择日期" />
                    </kv>
    <#elseif isType(field,"LocalDateTime")>
                    <kv label="${fieldCn(field)}">
                        <el-date-picker v-model="info.${field.getName()}" placeholder="选择日期时间"  type="datetime" />
                    </kv>
    <#elseif isType(field,"LocalTime")>
                    <kv label="${fieldCn(field)}">
                        <el-time-select v-model="info.${field.getName()}" placeholder="选择时间" />
                    </kv>
    <#elseif field.getName() == "name">
                    <kv label="${fieldCn(field)}">
                        <el-input v-model="info.${field.getName()}" chk="*" />
                    </kv>
    <#elseif field.getName() == "remark">
        <kv label="${fieldCn(field)}">
            <el-input v-model="info.${field.getName()}" type="textarea"/>
        </kv>
    <#else>
                    <kv label="${fieldCn(field)}">
                        <el-input v-model="info.${field.getName()}" />
                    </kv>
</#if>
</#list>
                </el-card>
            </el-col>

        </el-row>
    </div>
</template>

<script>
/**
 * Created by CodeGenerator  at ${now}
 */
<#list fields as field>
<#if field.getName() == "creator" || field.getName() == "createBy">
<#elseif field.getName() == "updater" || field.getName() == "updateBy">
<#elseif isType(field,"IdName")>
import Ref${bc(field.getName())}
    from
        "@/component/empty-ref"

</#if>
</#list>
export default {
    components: {<#list fields as field><#if field.getName() == "creator" || field.getName() == "createBy" || field.getName() == "updater" || field.getName() == "updateBy"><#elseif isType(field,"IdName")>
        "ref-${kb(field.getName())}": Ref${bc(field.getName())},
</#if></#list>},
    data() {
        return {
            info: {
<#list fields as field>
<#if fieldIsList(field)>
                ${field.getName()}: [],
<#elseif isObject(field)>
                ${field.getName()}: {},
</#if>
</#list>
            }, //子对象需要声明。
<#list enumTypes as type>
            ${type.getSimpleName()}: jv.enum.${type.getSimpleName()}.getData(),
</#list>
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

<style scoped lang="scss">
</style>