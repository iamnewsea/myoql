<template>
    <my-ref
        v-if="value2"
        url="${url}/list"
        v-model="value2"
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
        <el-table-column type="index" align="center" width="50"></el-table-column>
        <!--<template v-slot="scope"></template>-->
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
<#elseif isRes(field)>
        <el-table-column align="center" label="${fieldCn(field)}" prop="${field.getName()}_res"></el-table-column>
<#elseif isRes(field,"IdName")>
        <el-table-column align="center" label="${fieldCn(field)}" prop="${field.getName()}.name"></el-table-column>
<#elseif isRes(field,"IdUrl")>
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
    watch:{
        value:{
          deep: true,immediate: true, handler(v){
              this.value2 = v;
          }
        },
        value2:{
            deep: true,immediate: true, handler(v){
                this.$emit("input",v);
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