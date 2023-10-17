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
            :query="query"
        >
<#list fields as field>
<#if field.getName() == "id">
<#elseif field.getName() == "creator" || field.getName() == "createBy">
<#elseif field.getName() == "updater" || field.getName() == "updateBy">
<#elseif field.getName() == "createAt">
<#elseif field.getName() == "updateAt">
<#elseif field.getName() == "name">
            <el-table-column label="${fieldCn(field)}" align="center">
                <template v-slot="scope">
                    <div class="link" @click="edit_click(scope.row)">{{ scope.row.name }}</div>
                </template>
            </el-table-column>
<#elseif field.getType().isEnum()>
            <el-table-column align="center" label="${fieldCn(field)}" >
                <template v-slot="scope">
                    <el-tag>{{scope.row.${field.getName()}_res}}</el-tag>
                </template>
            </el-table-column>
<#elseif fieldIsEnumList(field)>
    <el-table-column align="center" label="${fieldCn(field)}" >
        <template v-slot="scope">
            <el-tag v-for="item in scope.row.${field.getName()}_res" :key="item">{{item}}</el-tag>
        </template>
    </el-table-column>
<#elseif isType(field,"IdName")>
            <el-table-column align="center" label="${fieldCn(field)}" prop="${field.getName()}.name"></el-table-column>
<#elseif isType(field,"IdUrl")>
            <el-table-column label="${fieldCn(field)}" align="center">
                <template v-slot="scope">
                    <img :src="scope.row.url" />
                </template>
            </el-table-column>
<#else>
            <el-table-column align="center" label="${fieldCn(field)}" prop="${field.getName()}"></el-table-column>
</#if>
</#list>
            <el-table-column label="操作" align="center">
                <template v-slot="scope">
                    <#if hasField(entity,"status")>
                    <el-button round @click="set_click(scope.row)" size="small">
                        设置状态
                    </el-button>
                    </#if>
                    <el-button type="warning" plain round @click="delete_click(scope.row)" size="small">
                        删除
                    </el-button>
                </template>
            </el-table-column>
            <template #query="scope">
<#if hasField(entity,"name")>
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
            //默认查询条件,默认存储
            query:{},
<#list enumTypes as type>
            ${type.getSimpleName()}: jv.enum.${type.getSimpleName()}.getData(),
</#list>
        };
    },
    mounted() {
        //加载存储的查询条件
    },
    methods: {
        //处理请求参数
        preload(param) {
            //保存查询条件

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
<#elseif fieldIsEnumList(field)>
                jv.enum.${fieldListType(field)}.fillRes(it, "${field.getName()}");
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
            let rowId = row.id;
            this.$refs.list.setLastRow(row);
            this.$router.push("${url}/edit/" + rowId)
        },
        async delete_click(row) {
            let rowId = row.id,rowName = row.name;
            await jv.confirm('确认删除 ' + rowName + ' 吗？');

            let res = await this.$http.post("${url}/delete/" + rowId)
            this.loadData();
        }<#if hasField(entity,"status")>,
        set_click(row) {
            //设置事件
            let rowId = row.id;
        }</#if>
    }
}
</script>