package com.nzh.myglide.lifecycle.request;

/**
 * 加载图片时的 占位图配置
 */
public class RequestOption {

    private int placeHolderId; // 请求加载图片前的 占位图id
    private int errorId;// 请求加载图片失败后的  占位图id

    private int overrideHeight; // 加载图片的固定高配置
    private int overrideWidth;// 加载图片的固定宽配置

    public int getPlaceHolderId() {
        return placeHolderId;
    }

    public void setPlaceHolderId(int placeHolderId) {
        this.placeHolderId = placeHolderId;
    }

    public int getErrorId() {
        return errorId;
    }

    public void setErrorId(int errorId) {
        this.errorId = errorId;
    }

    public int getOverrideHeight() {
        return overrideHeight;
    }

    public void setOverrideHeight(int overrideHeight) {
        this.overrideHeight = overrideHeight;
    }

    public int getOverrideWidth() {
        return overrideWidth;
    }

    public void setOverrideWidth(int overrideWidth) {
        this.overrideWidth = overrideWidth;
    }
}
