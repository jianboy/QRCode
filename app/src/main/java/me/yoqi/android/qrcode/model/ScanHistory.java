package me.yoqi.android.qrcode.model;

/**
 * history 类
 *
 * @author liuyuqi.gov@msn.cn
 * @date 2020-10-20
 */
public class ScanHistory {
    String id;  //自增字段
    String content; //内容
    String created; //创建时间
    int isUrl; //是否是链接

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getIsUrl() {
        return isUrl;
    }

    public void setIsUrl(int isUrl) {
        this.isUrl = isUrl;
    }
}