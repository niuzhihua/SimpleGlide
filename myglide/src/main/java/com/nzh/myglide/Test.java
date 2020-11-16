package com.nzh.myglide;

import com.nzh.myglide.cache.memory.ActiveResouceCache;
import com.nzh.myglide.cache.memory.ActiveResource;
import com.nzh.myglide.cache.memory.MemoryCache;
import com.nzh.myglide.cache.Key;
import com.nzh.myglide.cache.memory.LruMemoryCache;


public class Test implements ActiveResource.OnResourceReleaseListener, MemoryCache.ResouceRemoveListener {

    /**
     * 活动缓存 和 内存缓存的使用逻辑。
     */
    public ActiveResource testActiveCache_MemoryCache(Key key) {

        // 内存缓存
        LruMemoryCache memoryCache = new LruMemoryCache(20);
        memoryCache.setResouceRemoveListener(this);

        // 活动缓存
        ActiveResouceCache activeCache = new ActiveResouceCache(this);

        // 第一步： 从活动缓存中找 是否有图片可用。

        ActiveResource resouce = activeCache.get(key);

        if (resouce != null) {
            // 从活动缓存找到则返回
            // 引用计数 +1
            resouce.acquired();
            return resouce;
        }

        //第二步：从内存缓存中找
        resouce = memoryCache.get(key);
        if (resouce != null) {
            // 内存缓存找到逻辑：  从内存缓存移除,添加到活动缓存 ,计数+1
            memoryCache.remove(key);
            resouce.acquired();
            activeCache.put(key, resouce);
            return resouce;
        }

        return null;

    }


    @Override
    public void onRemoveFromLRUMemoryCache(ActiveResource resouce) {

        // 从内存缓存中移除
        // 并添加到活动缓存 或者 添加到复用池
    }

    @Override
    public void onRemoveFromActiveCache(Key key, ActiveResource resouce) {
        // 从活动缓存中移除
        // 并添加到内存缓存
    }
}
