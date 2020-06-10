package nbcp.db;

import kotlin.jvm.internal.Intrinsics;
import nbcp.comm.MyHelper;
import nbcp.comm.SysConstKt;
import nbcp.comm.config;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * 表示Mongo数据库里 Id，Url 的附件实体引用。
 * 保存到Mongo数据库的时候，使用 field Json，无Host。
 * 返回到Mvc前端的时候，使用 get method Json，带Host。
 */
public class IdUrl implements Serializable {
    private String id = "";
    private String url = "";

    public IdUrl() {

    }

    public IdUrl(@NotNull String id, @NotNull String url) {
        this();
        Intrinsics.checkParameterIsNotNull(id, "<set-?>");
        Intrinsics.checkParameterIsNotNull(url, "<set-?>");
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        Intrinsics.checkParameterIsNotNull(id, "<set-?>");
        this.id = id;
    }

    public String getUrl() {
        return MyHelper.PatchHostUrl(this.url, config.INSTANCE.getUploadHost());
    }

    public void setUrl(@NotNull String url) {
        Intrinsics.checkParameterIsNotNull(url, "<set-?>");
        this.url = url;
    }

    public String getUrlFieldValue() {
        return url;
    }

}
