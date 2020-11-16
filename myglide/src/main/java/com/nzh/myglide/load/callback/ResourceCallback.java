package com.nzh.myglide.load.callback;

import com.nzh.myglide.cache.memory.ActiveResource;

/**
 * 用来将找到的图片 回调给 Request 组件。
 */
public interface ResourceCallback {
    void onResourceCallback(ActiveResource resouce);
}
