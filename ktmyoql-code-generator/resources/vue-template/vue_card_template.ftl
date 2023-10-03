<template>
    <div class="card-page">
        <tool-bar nav="" :title="action_name + '${title}'">
            <el-button size="mini" @click="$router.push('${url}/add')" v-if="action=='edit'">新建</el-button>
            <el-button size="mini" @click="save_click" type="primary">保存</el-button>
        </tool-bar>
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
                    <kv label="${fieldCn(field)}">
                        <selector v-model="info.${field.getName()}" enum="${field.getType().getSimpleName()}" chk="*"/>
                    </kv>
    <#elseif fieldIsEnumList(field)>
                    <kv label="${fieldCn(field)}">
                        <selector multi value-is-object v-model="info.${field.getName()}" enum="${fieldListType(field)}" />
                    </kv>
    <#elseif isRes(field,"IdUrl")>
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
    <#elseif isRes(field,"IdName")>
                    <kv label="${fieldCn(field)}">
                        <ref-${kb(field.getName())} v-model="info.${field.getName()}"></ref-${kb(field.getName())}>
                    </kv>
    <#elseif isRes(field,"boolean")>
                    <kv label="${fieldCn(field)}">
                        <selector v-model="info.${field.getName()}" :data="{true:'是',false:'否','':'全部'}" />
                    </kv>
    <#elseif isRes(field,"LocalDate")>
                    <kv label="${fieldCn(field)}">
                        <el-date-picker v-model="info.${field.getName()}" placeholder="选择日期" />
                    </kv>
    <#elseif isRes(field,"LocalDateTime")>
                    <kv label="${fieldCn(field)}">
                        <el-date-picker v-model="info.${field.getName()}" placeholder="选择日期时间"  type="datetime" />
                    </kv>
    <#elseif isRes(field,"LocalTime")>
                    <kv label="${fieldCn(field)}">
                        <el-time-select v-model="info.${field.getName()}" placeholder="选择时间" />
                    </kv>
    <#elseif isRes(field,"byte","int","long")>
                    <kv label="${fieldCn(field)}">
                        <el-input v-model="info.${field.getName()}" chk="?int" />
                    </kv>
    <#elseif isRes(field,"float","double")>
        <kv label="${fieldCn(field)}">
            <el-input v-model="info.${field.getName()}" chk="?float" />
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
<#elseif isRes(field,"IdName")>
import Ref${bc(field.getName())}
    from
        "@/component/empty-ref"

</#if>
</#list>
export default {
    components: {<#list fields as field><#if field.getName() == "creator" || field.getName() == "createBy" || field.getName() == "updater" || field.getName() == "updateBy"><#elseif isRes(field,"IdName")>
        "ref-${kb(field.getName())}": Ref${bc(field.getName())},
</#if></#list>},
    data() {
        return {
<#list fields as field>
<#if field.getType().isEnum()>
            ${field.getType().getSimpleName()}: jv.enum.${field.getType().getSimpleName()}.getData(),
<#elseif fieldIsEnumList(field)>
            ${fieldListType(field)}: jv.enum.${fieldListType(field)}.getData(),
</#if>
</#list>
            info: {
<#list fields as field>
<#if fieldIsList(field,"Object")>
                ${field.getName()}: [],
<#elseif isObject(field)>
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
        this.loadData();
    },
    methods: {
        async loadData() {
            if (!this.id) return;
            let res = await this.$http.post("${url}/detail/" + this.id)
            this.info = res.data.data;
        },
        async save_click() {
            //校验
            if (this.chk() == false) {
                return;
            }

            let res = await this.$http.post("${url}/save", this.info)
            //[axios拦截器中已处理了异常]。
            jv.info(this.action_name + " 成功");
            let id = res.data.data || this.info.id;
            if (!id) {
                return jv.error("找不到 id");
            }
            this.$router.push("/dev/app-deploy-setting/edit/" + id);
        }
    }
}
</script>
