<template>
    <div class="list-page">
        <tool-bar nav="" title="${title}">
            <el-button @click="add_click()">添加</el-button>
        </tool-bar>
        <my-list
            ref="list"
            url="${url}/list"
            @param="preload"
            @loaded="procData"
            :page-size="10"
            :store="true"
        >
            <el-table-column type="index" align="center" width="50"></el-table-column>
                        <!--<template v-scope="scope"></template>-->
<#list fields as field>
<#if field.getName() == "id">
<#elseif field.getName() == "creator" || field.getName() == "createBy">
<#elseif field.getName() == "updater" || field.getName() == "updateBy">
<#elseif field.getName() == "createAt">
<#elseif field.getName() == "updateAt">
<#elseif field.getName() == "name">
            <el-table-column label="${field_cn(field)}" align="center">
                <template v-scope="scope">
                    <div class="link" @click="edit_click(scope.row)">{{ scope.row.name }}</div>
                </template>
            </el-table-column>
<#elseif is_res(field)>
            <el-table-column align="center" label="${field_cn(field)}" prop="${field.getName()}_res"></el-table-column>
<#elseif is_type(field,"IdName")>
            <el-table-column align="center" label="${field_cn(field)}" prop="${field.getName()}.name"></el-table-column>
<#elseif is_type(field,"IdUrl")>
            <el-table-column label="${field_cn(field)}" align="center">
                <template v-scope="scope">
                    <img :src="scope.row.url" />
                </template>
            </el-table-column>
<#else>
            <el-table-column align="center" label="${field_cn(field)}" prop="${field.getName()}"></el-table-column>
</#if>
</#list>
            <el-table-column label="操作" align="center">
                <template v-scope="scope">
                    <#if has("status")>
                    <el-button @click="set_click(scope.row)" size="small">
                        设置状态
                    </el-button>
                    </#if>
                    <el-button @click="delete_click(scope.row)" size="small">
                        删除
                    </el-button>
                </template>
            </el-table-column>
            <template #query="scope">
<#if has("name")>
                <kv label="名称">
                    <el-input v-model="scope.query.name"></el-input>
                </kv>
</#if>
            </template>
        </my-list>
    </div>
</template>
<style scoped>
</style>
<script>
/**
 * Created by CodeGenerator at ${now}
 */
export default {
    components: {},
    data() {
        return {
<#list fields as field>
<#if field.getType().isEnum()>
            ${field.getType().getSimpleName()}: jv.enum.${field.getType().getSimpleName()}.getData(),
<#elseif field_is_enum_list(field)>
            ${field_list_type(field)}: jv.enum.${field_list_type(field)}.getData(),
</#if>
</#list>
        };
    },
    mounted() {
    },
    methods: {
        //处理请求参数
        preload(param) {
            //添加查询参数，修改分页参数等。
            //param.type = this.$route.params.type
        },
        //处理列表的数据
        procData(res) {
            res.data.data.forEach(it => {
                //如果是组合实体，设置 it.id=...
<#list fields as field>
<#if field.getType().isEnum()>
                jv.enum.${field.getType().getSimpleName()}.fillRes(it, "${field.getName()}");
<#elseif field_is_enum_list(field)>
                jv.enum.${field_list_type(field)}.fillRes(it, "${field.getName()}");
</#if>
</#list>
            });
        },
        //查询
        loadData(pageNumber) {
            this.$refs.list.loadData(pageNumber);
        },
        add_click() {
            this.$router.push("${url}/add")
        },
        edit_click(row) {
            //记录上次点击行
            var rowId = row.id;
            this.$refs.list.setLastRow(row);
            this.$router.push("${url}/edit/" + rowId)
        },
        async delete_click(row) {
            var rowId = row.id,rowName = row.name;
            await jv.confirm('确认删除 ' + rowName + ' 吗？');

            var res = await this.$http.post("${url}/delete/" + rowId)
            this.loadData();
        }<#if has("status")>,
        set_click(row) {
            //设置事件
            var rowId = row.id;
        }</#if>
    }
}
</script>