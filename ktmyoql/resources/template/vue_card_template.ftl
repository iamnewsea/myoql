<template>
    <div>
        <div class="header-info">
            <p>{{action_name}}-${title}</p>
            <div>
                <el-button size="mini" @click="$router.push('${url}/add')" v-if="action=='edit'">新建</el-button>
                <el-button size="mini" @click="save_click" type="primary"> 保存</el-button>
            </div>
        </div>
        <el-row :gutter="12" type="flex">
            <el-col>
                <el-card shadow="always">
                    <div slot="header">
                        <span>基本信息</span>
                    </div>
<#list fields as field>
<#if field.name == 'id'>
    <#elseif field.name == 'createAt'>
    <#elseif field.type.isEnum>
        <kv label="${cn(field)}">
            <selector type="radio" v-model="info.${field.name}" enum="${field.type.simpleName}" />
        </kv>
    <#elseif is_enum_list(field)>
        <kv label="${cn(field)}">
            <selector type="check" v-model="info.${field.name}" enum="${field.type.simpleName}" />
        </kv>
    <#elseif it.type.name == "IdUrl">
        <kv>
            <label slot="k">${cn(field)}</label>
            <upload
                    :maxCount="1"
                    v-model="info.${field.name}"
                    fileType="img"
                    scales="16:9"
                    :maxWidth="1024"
                    maxSize="5M"
            ></upload>
        </kv>
    <#elseif is_list(it,"IdUrl")>
        <kv>
            <label slot="k">${cn(field)}</label>
            <upload
                    :maxCount="99"
                    v-model="info.${field.name}"
                    fileType="img"
                    scales="16:9"
                    :maxWidth="1024"
                    maxSize="5M"
            ></upload>
        </kv>
    <#elseif it.type.name == "IdName">
        <kv label="${cn(field)}">
            <ref-${k(name)} v-model="info.${field.name}"></ref-${k(field.name)}>
        </kv>
    <#elseif it.type.name == "boolean">
        <kv label="${cn(field)}">
            <selector type="radio" v-model="info.${field.name}" :data="{true:'是',false:'否','':'全部'}" />
        </kv>
    <#elseif it.type.name == 'LocalDate'>
        <kv label="${cn(field)}">
            <el-date-picker v-model="info.${field.name}" placeholder="选择日期" />
        </kv>
    <#elseif it.type.name == "LocalDateTime">
        <kv label="${cn(field)}">
            <el-date-picker v-model="info.${field.name}" placeholder="选择日期时间"  type="datetime" />
        </kv>
    <#elseif it.type.name == "LocalTime">
        <kv label="${cn(field)}">
            <el-time-select v-model="info.${field.name}" placeholder="选择时间" />
        </kv>
    <#elseif it.name == "name">
        <kv label="${cn(field)}"><el-input v-model="info.${field.name}" chk="*"/></kv>
    <#else>
        <kv label="${cn(field)}"><el-input v-model="info.${field.name}" /></kv>
</#if>
</#list>
                </el-card>
            </el-col>
            <el-col>
                <el-card shadow="always">
                    <div slot="header">
                        <span>扩展信息</span>
                    </div>
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
<#if is_type(field,"IdName")>
        import Ref${W(field.name)} from "../home/empty-ref"
</#if>
</#list>
    export default {
        components: {
<#list fields as field>
<#if is_type(field,"IdName")>
        "ref-${k(field.name)}": Ref${W(field.name)},
</#if>
</#list>
        },
        data() {
            return {
<#list fields as field>
<#if is_type(field,"IdName")>
                ${field.type.name}: jv.enum.${field.type.name}.getData(),
</#if>
</#list>
                info: {}, //子对象需要声明。
            }
        },
        props: {
            id: {type: String, default: ""}
        },
        computed:{
            action() {
                return this.id ? "edit" : "add";
            },
            action_name(){
                return {add: "添加", edit: "修改"}[this.action]
            }
        },
        mounted() {
            this.loadData()
        },
        methods: {
            loadData() {
                if (!this.id) return;
                this.$http.post("${url}/detail/" + this.id).then(res => {
                    this.info = res.data.data;
                });
            },
            save_click() {
                //校验
                if (jv.main.chk() == false) {
                    return;
                }

                this.$http.post("${url}/save", this.info).then(res => {
                    jv.info(this.action_name + " 成功");
                    if (this.action == "add") {
                        this.$router.push("${url}/edit/" + res.data.data)
                    }
                    else if (this.action == "edit") {
                        this.$router.push("${url}/list")
                    }
                })
            },

        }
    }
</script>
