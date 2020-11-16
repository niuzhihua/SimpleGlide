package com.nzh.myglide.load.callback;

import com.nzh.myglide.cache.memory.ActiveResource;

/**
 * 负责 将解码的图片 回调给 EngineJob.
 */
public interface DecodeJobCallback {
    /**
     * 获取图片成功的回调
     * @param resouce
     */
    void onResourceReady(ActiveResource resouce);

    // 获取图片失败。
    void onLoadFailed(Exception e);
}
