<template>
    <div class="card-page">
        <div class="toolbar">
            <el-button size="mini" @click="$router.push('${url}/add')" v-if="action=='edit'">新建</el-button>
            <el-button size="mini" @click="save_click" type="primary"> 保存</el-button>
        </div>
        <el-row :gutter="12" type="flex">
            <el-col>
                <el-card shadow="always">
                    <div slot="header">
                        <span>基本信息</span>
                    </div>
<#list fields as field>
    <#if field.getName() == "id">
    <#elseif field.getName() == "creator" || field.getName() == "createBy">
    <#elseif field.getName() == "updater" || field.getName() == "updateBy">
    <#elseif field.getName() == "createAt">
    <#elseif field.getName() == "updateAt">
    <#elseif field.getType().isEnum()>
                    <kv label="${cn(field)}">
                        <selector type="radio" v-model="info.${field.getName()}" enum="${field.getType().getSimpleName()}" chk="*"/>
                    </kv>
    <#elseif is_enum_list(field)>
                    <kv label="${cn(field)}">
                        <selector type="check" v-model="info.${field.getName()}" enum="${list_type(field)}" />
                    </kv>
    <#elseif is_type(field,"IdUrl")>
                    <kv>
                        <label slot="k">${cn(field)}</label>
                        <upload
                                :maxCount="1"
                                v-model="info.${field.getName()}"
                                fileType="img"
                                scales="16:9"
                                :maxWidth="1024"
                                maxSize="5M"
                        ></upload>
                    </kv>
    <#elseif is_list(field,"IdUrl")>
                    <kv>
                        <label slot="k">${cn(field)}</label>
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
                    <kv label="${cn(field)}">
                        <ref-${k(field.getName())} v-model="info.${field.getName()}"></ref-${k(field.getName())}>
                    </kv>
    <#elseif is_type(field,"Boolean")>
                    <kv label="${cn(field)}">
                        <selector type="radio" v-model="info.${field.getName()}" :data="{true:'是',false:'否','':'全部'}" />
                    </kv>
    <#elseif is_type(field,"LocalDate")>
                    <kv label="${cn(field)}">
                        <el-date-picker v-model="info.${field.getName()}" placeholder="选择日期" />
                    </kv>
    <#elseif is_type(field,"LocalDateTime")>
                    <kv label="${cn(field)}">
                        <el-date-picker v-model="info.${field.getName()}" placeholder="选择日期时间"  type="datetime" />
                    </kv>
    <#elseif is_type(field,"LocalTime")>
                    <kv label="${cn(field)}">
                        <el-time-select v-model="info.${field.getName()}" placeholder="选择时间" />
                    </kv>
    <#elseif field.getName() == "name">
                    <kv label="${cn(field)}">
                        <el-input v-model="info.${field.getName()}" chk="*" />
                    </kv>
    <#else>
                    <kv label="${cn(field)}">
                        <el-input v-model="info.${field.getName()}" />
                    </kv>
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
<#if field.getName() == "creator" || field.getName() == "createBy">
<#elseif field.getName() == "updater" || field.getName() == "updateBy">
<#elseif is_type(field,"IdName")>
    import Ref${W(field.getName())} from "../home/empty-ref"
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
<#elseif is_enum_list(field)>
            ${list_type(field)}: jv.enum.${list_type(field)}.getData(),
</#if>
</#list>
            info: {
<#list fields as field>
<#if is_list(field,"Object")>
                ${field.getName()}: [],
<#elseif is_object(field)>
                ${field.getName()}: {},
</#if>
</#list>
            }, //子对象需要声明。
        }
    },
    props: {
        id: {type: String, default: ""}
    },
    computed: {
        action() {
            return this.id ? "edit" : "add";
        },
        action_name() {
            return {add: "添加", edit: "修改"}[this.action]
        }
    },
    mounted() {
        this.$addNav(this.action_name + "${title}");
        this.loadData();
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
                //[axios拦截器中已处理了异常]。
                jv.info(this.action_name + " 成功");
                if (this.action == "add") {
                    this.$popNav();
                    this.$router.push("${url}/edit/" + res.data.data)
                } else if (this.action == "edit") {
                    this.$router.push("${url}/list")
                }
            })
        }
    }
}
</script>
