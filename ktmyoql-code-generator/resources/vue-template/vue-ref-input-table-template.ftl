<template>
    <input-table style="width:100%"
                 :readOnly="readOnly"
                 v-model="table" @add="v=>{}">
        <div slot="head" v-if="title">
            {{title}}
        </div>
<#list fields as field>
    <#if field.getName() == "id">
    <#elseif field.getName() == "creator" || field.getName() == "createBy">
    <#elseif field.getName() == "updater" || field.getName() == "updateBy">
    <#elseif field.getName() == "createAt">
    <#elseif field.getName() == "updateAt">
    <#elseif field.getName() == "name">
        <el-table-column label="${fieldCn(field)}" align="center">
            <template v-slot="scope">
                <el-input v-model="scope.row.${field.getName()}"></el-input>
            </template>
        </el-table-column>
    <#elseif field.getType().isEnum()>
        <el-table-column align="center" label="${fieldCn(field)}" >
            <template v-slot="scope">
                <selector  v-model="scope.row.${field.getName()}" enum="${fieldListType(field)}" />
            </template>
        </el-table-column>
    <#elseif fieldIsEnumList(field)>
        <el-table-column align="center" label="${fieldCn(field)}" >
            <template v-slot="scope">
                <selector multi  v-model="scope.row.${field.getName()}" enum="${fieldListType(field)}" />
            </template>
        </el-table-column>
    <#elseif isType(field,"IdName")>
        <el-table-column align="center" label="${fieldCn(field)}" prop="${field.getName()}.name">
            <ref-${kb(field.getName())} v-model="scope.row.${field.getName()}"></ref-${kb(field.getName())}>
        </el-table-column>
    <#elseif isType(field,"IdUrl")>
        <el-table-column label="${fieldCn(field)}" align="center">
            <template v-slot="scope">
                <upload
                        :maxCount="1"
                        v-model="scope.row.${field.getName()}"
                        fileType="img"
                        scales="16:9"
                        :maxWidth="1024"
                        maxSize="5M"
                ></upload>
            </template>
        </el-table-column>
    <#else>
        <el-table-column align="center" label="${fieldCn(field)}" prop="${field.getName()}">
            <template v-slot="scope">
                <el-input v-model="scope.row.${field.getName()}"></el-input>
            </template>
        </el-table-column>
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
        title: {type: String , default: ""},
        readOnly: {type: Boolean, default: false},
        value: {
            type: Array, default: function () {
                return []
            }
        }
    },
    data() {
      return {
<#list enumTypes as type>
    ${type.getSimpleName()}: jv.enum.${type.getSimpleName()}.getData(),
</#list>
          value2: null
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
