<template>
    <input-table style="width:100%"
                 :readOnly="readOnly"
                 v-model="table" @add="v=>{}">
        <div slot="head" v-if="title" class="title">
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
        <el-table-column align="center" label="${fieldCn(field)}">
            <ref-${kb(field.getName())} v-model="scope.row.${field.getName()}.id"></ref-${kb(field.getName())}>
        </el-table-column>
    <#elseif isType(field,"IdUrl")>
        <el-table-column label="${fieldCn(field)}" align="center">
            <template v-slot="scope">
                <upload
                        :maxCount="1"
                        v-model="scope.row.${field.getName()}.id"
                        fileType="img"
                        scales="16:9"
                        :maxWidth="1024"
                        maxSize="5M"
                ></upload>
            </template>
        </el-table-column>
    <#elseif isType(field,"boolean")>
        <el-table-column align="center" label="${fieldCn(field)}">
            <template v-slot="scope">
                <selector v-model="scope.row.${field.getName()}" :data="{true:'是',false:'否','':'全部'}" />
            </template>
        </el-table-column>
    <#elseif isType(field,"LocalDate")>
        <el-table-column align="center" label="${fieldCn(field)}">
            <template v-slot="scope">
                <el-date-picker v-model="scope.row.${field.getName()}" placeholder="选择日期" />
            </template>
        </el-table-column>
    <#elseif isType(field,"LocalDateTime")>
        <el-table-column align="center" label="${fieldCn(field)}">
            <template v-slot="scope">
                <el-date-picker v-model="scope.row.${field.getName()}" placeholder="选择日期时间"  type="datetime" />
            </template>
        </el-table-column>
    <#elseif isType(field,"LocalTime")>
        <el-table-column align="center" label="${fieldCn(field)}">
            <template v-slot="scope">
                <el-time-select v-model="scope.row.${field.getName()}" placeholder="选择时间" />
            </template>
        </el-table-column>
    <#else>
        <el-table-column align="center" label="${fieldCn(field)}">
            <template v-slot="scope">
                <el-input v-model="scope.row.${field.getName()}"></el-input>
            </template>
        </el-table-column>
    </#if>
</#list>
    </input-table>

</template>
<style scoped lang="scss">
    .title{
        margin-top:10px;
        &:after{
            content: "：";
            display: inline-block;
        }
    }
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
