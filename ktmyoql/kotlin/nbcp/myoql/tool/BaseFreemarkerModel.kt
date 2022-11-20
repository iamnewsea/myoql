package nbcp.myoql.tool;



import lombok.Data;
import nbcp.base.extend.MyHelper;
import nbcp.base.utils.SpringUtil;
import nbcp.myoql.tool.freemarker.*;

import java.time.LocalDateTime;

@Data
public class BaseFreemarkerModel {
    private String user = SpringUtil.getContext().getEnvironment().getProperty("user.name");

    private String now = MyHelper.AsString(LocalDateTime.now());

    private FreemarkerHasValue hasValue = new FreemarkerHasValue();

    private FreemarkerKebabCase kb = new FreemarkerKebabCase();
    private FreemarkerBigCamelCase bc = new FreemarkerBigCamelCase();
    private FreemarkerSmallCamelCase sc = new FreemarkerSmallCamelCase();

    private FreemarkerIsRes isRes = new FreemarkerIsRes();
    private FreemarkerIsIn isIn = new FreemarkerIsIn();
    private FreemarkerIsObject isObject = new FreemarkerIsObject();
    private FreemarkerIsType isType = new FreemarkerIsType();

    private FreemarkerAllField allFields = new FreemarkerAllField();
    private FreemarkerFieldIsEnumList fieldIsEnumList = new FreemarkerFieldIsEnumList();
    private FreemarkerFieldCn fieldCn = new FreemarkerFieldCn();
    private FreemarkerFieldValue fieldValue = new FreemarkerFieldValue();
    private FreemarkerIsList fieldIsList = new FreemarkerIsList();
    private FreemarkerFieldListType fieldListType = new FreemarkerFieldListType();


}
