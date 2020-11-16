package com.nzh.myglide.load;

import android.content.Context;
import android.util.ArrayMap;

import com.nzh.myglide.MyGlide;
import com.nzh.myglide.cache.Key;
import com.nzh.myglide.cache.disk.wrapper.DiskCache;
import com.nzh.myglide.cache.memory.ActiveResource;
import com.nzh.myglide.cache.memory.ActiveResouceCache;
import com.nzh.myglide.cache.memory.MemoryCache;
import com.nzh.myglide.cache.pool.BitmapPool;
import com.nzh.myglide.load.callback.EngineJobListener;
import com.nzh.myglide.load.callback.ResourceCallback;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 1:负责图片加载流程的实现
 * 2: 负责内存缓存的管理
 * <p>
 * 一、加载流程 ：
 * 1、从内存缓存加载。
 * 2、从磁盘缓存或网络加载，这就涉及到IO操作。因此需要 线程池。
 * <p>
 * 二、加载图片需要的要素：
 * <p>
 * 大小(宽高) 、
 * 数据来源(model)、
 * 接收者(这里为Request组件) 、
 * 上下文(工具类)
 * Key : 根据以上元素生成。
 * <p>
 * 活动缓存工具类
 * 内存缓存工具类 ： 可以配置，所以不能在这个类中创建,需要传递进来。
 * 磁盘缓存工具类 ： 可以配置，所以不能在这个类中创建,需要传递进来。
 * 线程池：可用通过 MyGlide拿到，传给EngineJob来用。 也可以不在这里传，直接在
 * EngineJob中全局获取 . ????
 */
public class Engine implements
        ActiveResource.OnResourceReleaseListener,
        EngineJobListener,
        MemoryCache.ResouceRemoveListener {
    // 需要的要素
    private int width;
    private int height;
    private Context context;
    private Object model;

    //内存缓存1：活跃缓存
    ActiveResouceCache activeCache;

    // 内存缓存2
    MemoryCache memoryCache;

    // 磁盘缓存
    DiskCache diskCache;

    // bitmap 复用池
    BitmapPool bitmapPool;

    // 代表IO任务的集合
    private ArrayMap<Key, EngineJob> jobs = new ArrayMap<>();

    ThreadPoolExecutor executor;

    public Engine(MemoryCache memoryCache, DiskCache diskCache, BitmapPool bitmapPool, ThreadPoolExecutor executor) {
        activeCache = new ActiveResouceCache(this);
        this.memoryCache = memoryCache;
        this.diskCache = diskCache;
        this.bitmapPool = bitmapPool;
        this.executor = executor;
    }

    /**
     * 关闭Engine 组件。释放资源
     */
    public void shutDown() {
    }


    /**
     * IO任务加载图片时的 结果封装
     */
    public static class LoadStatus {
        ResourceCallback callback;
        EngineJob job;

        public LoadStatus(ResourceCallback callback, EngineJob job) {
            this.callback = callback;
            this.job = job;
        }

        /**
         * 取消实现：就是不回调就可以了。
         */
        public void cancel() {
            job.removeResourceCallback(callback);
        }
    }

    /**
     * @param glide
     * @param width
     * @param height
     * @param model
     * @param callbck 将结果返回给Request组件.
     */
    public LoadStatus load(MyGlide glide, int width, int height, Object model, ResourceCallback callbck) {

        //0、 生成一个key,用来取缓存找图片
        EngineKey engineKey = new EngineKey(model, width, height);

        //1、 先从活跃缓存当中查找 图片。
        ActiveResource resource = activeCache.get(engineKey);
        if (null != resource) {
            callbck.onResourceCallback(resource);
            // 引用技术 +1
            resource.acquired();
            return null;
        }

        // 2、从内存缓存中找(并移除) 图片 。
        resource = memoryCache.remove2(engineKey);
        if (null != resource) {
            // 放入活跃缓存
            activeCache.put(engineKey, resource);
            // 引用计数 +1
            resource.acquired();
            // 设置 活动资源 的监听，当引用计数为0时再次放入 内存缓存。
            resource.setReleaseListener(engineKey, this);
            // 回调给调用者
            callbck.onResourceCallback(resource);
            return null;
        }

        // 3、从磁盘缓存 或网络 找图片.  IO操作。
        //    这个IO任务应该怎么做? 下面是流程。

        //查找正在执行的IO任务。如果找到，则直接添加一个回调来使用。
        EngineJob job = jobs.get(engineKey);
        if (null != job) {
            job.addResourceCallback(callbck);
            return new LoadStatus(callbck, job);
        }

        // 找不到则 创建IO任务，并添加回调。
        EngineJob engineJob = new EngineJob(engineKey, executor, this);
        engineJob.addResourceCallback(callbck);

        // 启动任务
        DecodeJob decodeJob = new DecodeJob(glide, model, width, height, diskCache, engineJob);
        engineJob.start(decodeJob);

        // 并保存到集合。
        jobs.put(engineKey, engineJob);

        return new LoadStatus(callbck, engineJob);
    }


    /**
     * 引用计数为0时回调 .
     * 1、将resource从活跃缓存移除 .
     * 2、并加入内存缓存 .
     *
     * @param key
     */
    @Override
    public void onRemoveFromActiveCache(Key key, ActiveResource resouce) {
        activeCache.remove(key);
        memoryCache.put(key, resouce);
        System.out.println("计数为0:从活跃缓存移除,并加入内存缓存");
    }

    /**
     * 从内存缓存中移除图片时回调 。 并将图片加入 图片复用池。
     *
     * @param resource
     */
    @Override
    public void onRemoveFromLRUMemoryCache(ActiveResource resource) {
        bitmapPool.put(resource.getBitmap());
    }

    /**
     * IO 任务完成后的操作：
     * 1、加入活跃缓存并设置 活跃缓存监听。
     * 2、清除本次IO任务
     *
     * @param key
     * @param resource
     */
    @Override
    public void onEngineJobCompleted(Key key, ActiveResource resource) {
        if (resource != null) {
            resource.setReleaseListener(key, this);
            activeCache.put(key, resource);
        }
        // 删除IO 任务
        jobs.remove(key);
    }

    /**
     * 删除 IO 任务
     *
     * @param key
     */
    @Override
    public void onEngineJobCanceled(Key key) {
        jobs.remove(key);
    }
}
