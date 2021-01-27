<template>
  <my-ref url="${url}/list"
          v-model="value" @loaded="procData" :multi="multi" :page-size="10"
          ref="list" name="${title}" :store="true"  v-bind="[$attrs]" @input="v=>$emit('input',v)"
          :readOnly="readOnly"
  >
    <el-table-column type="index" clign="center" width="50"></el-table-column>
        <!--<template slot-scope="scope"></template>-->
<#list fields as field>
<#if field.getName() == "id"><#elseif field.getName() == "name">
            <el-table-column label="${cn(field)}" align="center">
                <template slot-scope="scope">
                    <div class="link">{{scope.row.name}}</div>
                </template>
            </el-table-column>
<#elseif is_res(field)>
            <el-table-column align="center" label="${cn(field)}" prop="${field.getName()}_res"></el-table-column>
<#elseif is_type(field,"IdName")>
            <el-table-column align="center" label="${cn(field)}" prop="${field.getName()}.name"></el-table-column>
<#elseif is_type(field,"IdUrl")>
            <el-table-column label="${cn(field)}" align="center">
                <template slot-scope="scope">
                    <img :src="scope.row.url" />
                </template>
            </el-table-column>
<#else>
            <el-table-column align="center" label="${cn(field)}" prop="${field.getName()}"></el-table-column>
</#if>
</#list>
    <template #query="scope">
<#if has("name")>
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
    name: "ref-${entity_url}",
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
<#list fields as field>
<#if field.getType().isEnum() || is_list(field,"Enum")>
                ${field.getType().getSimpleName()}: jv.enum.${field.getType().getSimpleName()}.getData(),
</#if>
</#list>
      }
    },
    methods: {
      procData(res, op) {
        var json = res.data.data;
        json.forEach(it => {
            //如果是组合实体，设置 it.id=...
<#list fields as field>
<#if field.getType().isEnum()>
                    jv.enum.${field.getType().getSimpleName()}.fillRes(it,"${field.getType().getSimpleName()}");
</#if>
</#list>
        });
      }
    }
  }
</script>