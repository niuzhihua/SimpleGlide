package com.nzh.myglide;

import android.app.ActivityManager;
import android.content.Context;
import android.util.DisplayMetrics;

import com.nzh.myglide.cache.disk.wrapper.DiskCache;
import com.nzh.myglide.cache.disk.wrapper.DiskLruCacheWrapper;
import com.nzh.myglide.cache.memory.LruMemoryCache;
import com.nzh.myglide.cache.memory.MemoryCache;
import com.nzh.myglide.cache.pool.BitmapPool;
import com.nzh.myglide.load.Engine;
import com.nzh.myglide.load.GlideExecutor;

import java.util.concurrent.ThreadPoolExecutor;

public class MyGlideBuilder {

    // 内存缓存工具
    public MemoryCache memoryCache;
    // 磁盘缓存
    public DiskCache diskCache;
    // 图片复用池
    public BitmapPool bitmapPool;
    // 图片加载引擎
    public Engine engine;

    public ThreadPoolExecutor executor;

    /**
     * 1、为内存缓存指定 内存大小。
     * 2、初始化 各种缓存工具 等。
     *
     * @param context
     * @return
     */
    public MyGlide build(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 返回系统为app分配的最大可用的内存大小，单位: M 兆字节
        int max = activityManager.getMemoryClass() * 1024 * 1024;

        // 以一张铺满屏幕的图片的大小为单位
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int unit = displayMetrics.widthPixels * displayMetrics.heightPixels * 4;

        // 内存缓存大小
        float memoryCacheSize = unit * 3f;
        // 图片复用池的大小
        float bitMapPoolSize = unit * 2f;

        // 如果超出可用大小，则 重新分配。
        if (bitMapPoolSize + memoryCacheSize > max) {
            // 总内存分5份
            float unit2 = max / 5f;
            memoryCacheSize = unit2 * 3f;
            bitMapPoolSize = unit2 * 2f;
        }


        if (bitmapPool == null) {
            bitmapPool = new BitmapPool((int) bitMapPoolSize);
        }
        if (diskCache == null) {
            diskCache = new DiskLruCacheWrapper(context);
        }


        if (memoryCache == null) {
            memoryCache = new LruMemoryCache((int) memoryCacheSize);
            // 监听内存缓存 中图片的移除
            memoryCache.setResouceRemoveListener(engine);
        }

        if (executor == null) {
            executor = GlideExecutor.newExecutor();
        }
        if (engine == null) {
            engine = new Engine(memoryCache, diskCache, bitmapPool, executor);
        }

        return new MyGlide(context, this);
    }

}
