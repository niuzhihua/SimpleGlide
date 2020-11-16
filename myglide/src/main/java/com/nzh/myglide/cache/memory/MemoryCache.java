package com.nzh.myglide.cache.memory;

import com.nzh.myglide.cache.Key;

/**
 * 内存缓存 ： 采用LrcCahce 缓存淘汰算法控制缓存。
 */
public interface MemoryCache /*extends Release */ {

    /**
     * 向内存缓存中添加
     *
     * @param key
     * @param value
     */
    ActiveResource put(Key key, ActiveResource value);

    /**
     * 从内存缓存中 删除
     *
     * @param key
     */
    ActiveResource remove2(Key key);

    /**
     * 当图片从内存缓存中移除时，触发监听。
     *
     * @param listener
     */
    void setResouceRemoveListener(ResouceRemoveListener listener);


    interface ResouceRemoveListener {
        /**
         * 从内存缓存中移除时 回调。
         *
         * @param resouce
         */
        void onRemoveFromLRUMemoryCache(ActiveResource resouce);
    }

}
