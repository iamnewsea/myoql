<template>
    <my-ref
        v-if="table"
        url="${url}/list"
        v-model="table"
        @loaded="procData"
        :multi="multi"
        :page-size="10"
        ref="list"
        name="${title}"
        :store="true"
        v-bind="[$attrs]"
        @input="v=>$emit('input',v)"
        :readOnly="readOnly"
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
        <template #query="scope">
<#if hasField(entity,"name")>
        <kv label="名称">
            <el-input v-model="scope.query.name"></el-input>
        </kv>
</#if>
        </template>
        <template #button>
            <el-button>选择${title}</el-button>
        </template>
    </my-ref>
</template>
<script>
/**
 * Created by CodeGenerator at ${now}
 */
export default {
    name: "ref-${kb(entity)}",
    props: {
        readOnly: {type: Boolean, default: false},
        multi: {type: Boolean, default: false}, //多选
        value: {
            type: [Object, Array], default: function () {
                return []
            }
        }
    },
    data() {
      return {
<#list enumTypes as type>
          ${type.getSimpleName()}: jv.enum.${type}.getData(),
</#list>
      }
    },
    computed:{
      table: {
          get() {
              return this.value;
          },
          set(folders){
              var v = Object.assign({}, v);
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