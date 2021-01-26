<template>
    <div class="list">
        <div class="header-info">
            <p>${title}</p>
            <el-button type="primary"  @click="add_click()">添加</el-button>
        </div>
        <my-list ref="list" url="${url}/list" @param="preload" @loaded="procData" :page-size="pageSize" :store="true"
            style="width: 100%;margin-top:20px;">
            <el-table-column type="index" clign="center" width="50"></el-table-column>
                        <!--<template slot-scope="scope"></template>-->
<#list fields as field>
<#if has("id")><#elseif has("name")>
            <el-table-column label="${cn(field)}" align="center">
                <template slot-scope="scope">
                    <div class="link" @click="edit_click(scope.row)">{{scope.row.name}}</div>
                </template>
            </el-table-column>
<#elseif is_res(field)>
            <el-table-column align="center" label="${cn(field)}" prop="${field.name}_res"></el-table-column>
<#elseif is_type(field,"IdName")>
            <el-table-column align="center" label="${cn(field)}" prop="${field.name}.name"></el-table-column>
<#elseif is_type(field,"IdUrl")>
            <el-table-column label="${cn(field)}" align="center">
                <template slot-scope="scope">
                    <img :src="scope.row.url" />
                </template>
            </el-table-column>
<#else>
            <el-table-column align="center" label="${cn(field)}" prop="${field.name}"></el-table-column>
</#if>
</#list>
            <el-table-column label="操作" align="center" width="180">
                <template slot-scope="scope">
                    <el-button @click="edit_click(scope.row)" size="small">
                        编辑
                    </el-button>
                    <el-button @click="set_click(scope.row)" size="small">
                        设置状态
                    </el-button>
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
                pageSize:10,
<#list fields as field>
<#if field.type.isEnum || is_list(field,"Enum")>
                ${type}: jv.enum.${type}.getData(),
</#if>
</#list>
            };
        },
        mounted() {
            this.loadData();
        },
        methods: {
            //处理请求参数
            preload(param){
                //添加查询参数，修改分页参数等。
                //param.type = this.$route.params.type
            },
            //处理列表的数据
            procData(res) {
                res.data.data.forEach(it => {
                    //如果是组合实体，设置 it.id=...
<#list fields as field>
${fif:#enum1}
                    jv.enum.${type}.fillRes(it,"${field.name}");
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
            delete_click(row) {
                var rowId = row.id;
                var rowName = row.name;
                jv.confirm('确认删除 ' + rowName + ' 吗？').then(res => {
                    this.$http.post("${url}/delete/" + rowId).then(res => {
                        this.loadData();
                    })
                });
            },
            set_click(row){
                //设置事件
                var rowId = row.id;
            }
        }
    }
</script>