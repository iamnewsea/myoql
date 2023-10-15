<template>
    <input-table style="width:100%"
                 :readOnly="readOnly"
                 v-model="table" @add="v=>{}">
<#list fields as field>
    <#if field.getName() == "id">
    <#elseif field.getName() == "creator" || field.getName() == "createBy">
    <#elseif field.getName() == "updater" || field.getName() == "updateBy">
    <#elseif field.getName() == "createAt">
    <#elseif field.getName() == "updateAt">
    <#elseif field.getName() == "name">
        <el-table-column label="${fieldCn(field)}" align="center">
            <template v-slot="scope">
                {{ scope.row.name }}
            </template>
        </el-table-column>
    <#elseif isType(field)>
        <el-table-column align="center" label="${fieldCn(field)}" prop="${field.getName()}_res"></el-table-column>
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
    </input-table>

</template>
<style scoped>
</style>
<script>
/**
 * Created by CodeGenerator at ${now}
 */
export default {
    name: "table-${kb(entity)}",
    props: {
        readOnly: {type: Boolean, default: false},
        value: {
            type: Array, default: function () {
                return []
            }
        }
    },
    data() {
      return {
          value2: null,
<#list fields as field>
    <#if field.getType().isEnum()>
    ${field.getType().getSimpleName()}: jv.enum.${field.getType().getSimpleName()}.getData(),
<#elseif fieldIsEnumList(field)>
${fieldListType(field)}: jv.enum.${fieldListType(field)}.getData(),
</#if>
    </#list>
      }
    },
    computed:{
      table: {
          get() {
              return this.value;
          },
          set(folders){
              var v = Object.assign([], v);
              this.$emit("input", v);
          }
      }
    },
    methods: {
      procData(res, op) {
        let json = res.data.data;
        json.forEach(it => {
            //如果是组合实体，设置 it.id=...
<#list fields as field>
    <#if field.getType().isEnum()>
                    jv.enum.${field.getType().getSimpleName()}.fillRes(it, "${field.getName()}");
<#elseif fieldIsEnumList(field)>
                    jv.enum.${fieldListType(field)}.fillRes(it, "${field.getName()}");
</#if>
    </#list>
        });
      }
    }
  }
</script>
