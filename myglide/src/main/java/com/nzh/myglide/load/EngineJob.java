package com.nzh.myglide.load;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.nzh.myglide.cache.Key;
import com.nzh.myglide.cache.memory.ActiveResource;
import com.nzh.myglide.load.callback.DecodeJobCallback;
import com.nzh.myglide.load.callback.EngineJobListener;
import com.nzh.myglide.load.callback.ResourceCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 1、负责从磁盘缓存 和网络中加载图片。 因为都是IO操作。
 * 2、做线程切换 回调到 Enging.
 * 肯定需要的元素：
 * <p>
 * Key: 用来从磁盘找图片。
 * 线程池 : 可以配置，所以不能在这个类中创建,需要传递进来。
 * 接口：将结果回传给调用者(Engine)，需要保存 EngineJobListener 回调对象.
 * 接口：将结果传给 Request，需要保存 ResourceCallback回调对象.
 */
public class EngineJob implements DecodeJobCallback {

    private Key key;
    // 将加载出来的图片 返回给Engine组件. 来做内存管理.
    private EngineJobListener engineJobCallback;
    // 用来启动IO 任务 : DecodeJob .
    private ThreadPoolExecutor executor;
    // 用来做线程切换
    private Handler handler = new Handler(Looper.getMainLooper(), new HandlerCallback());
    // 取消标记
    boolean isCancel = false;
    // IO任务
    DecodeJob decodeJob;
    // IO 任务加载出来的 ActiveResource
    ActiveResource resource;
    // Engine组件加载图片时的回调
    private List<ResourceCallback> resourceCallbackList = new ArrayList<>();

    public EngineJob(Key key, ThreadPoolExecutor executor, EngineJobListener callback) {
        this.key = key;
        this.engineJobCallback = callback;
        this.executor = executor;
    }

    /**
     * 注册一个回调.
     *
     * @param callback
     */
    public void addResourceCallback(ResourceCallback callback) {
        resourceCallbackList.add(callback);
    }

    /**
     * 移除 指定回调
     *
     * @param callback
     */
    public void removeResourceCallback(ResourceCallback callback) {

        resourceCallbackList.remove(callback);
        // 如果 没有回调处理了,就取消 IO任务.
        if (resourceCallbackList.isEmpty()) {
            cancel();
        }
    }

    public void start(DecodeJob decodeJob) {
        this.decodeJob = decodeJob;
        executor.execute(decodeJob);
    }

    /**
     * 取消 图片的加载
     */
    public void cancel() {
        isCancel = true;
        // 取消 磁盘或网络加载图片的io任务.
        decodeJob.cancel();
        // 通知Engine取消了
        engineJobCallback.onEngineJobCanceled(key);
    }

    /**
     * 从磁盘或网络 等IO任务加载完数据后的回调。
     *
     * @param resource
     */
    @Override
    public void onResourceReady(ActiveResource resource) {
        this.resource = resource;
        handler.obtainMessage(HandlerCallback.MSG_SUCCESS, this).sendToTarget();
    }

    @Override
    public void onLoadFailed(Exception e) {
        handler.obtainMessage(HandlerCallback.MSG_FAILED, this).sendToTarget();
    }


    private static class HandlerCallback implements Handler.Callback {

        static final int MSG_SUCCESS = 1;  //图片加载成功
        static final int MSG_FAILED = 2;  // 加载失败
        static final int MSG_CANCELED = 3; //加载取消

        @Override
        public boolean handleMessage(Message msg) {

            EngineJob job = (EngineJob) msg.obj;
            switch (msg.what) {
                case MSG_SUCCESS:
                    job.onSuccessToMainThread();
                    break;
                case MSG_FAILED:
                    job.onFailedToMainThread();
                    break;
                case MSG_CANCELED:
                    // TODO()
                    break;
            }

            return true;
        }
    }

    /**
     * IO任务成功完成后切换到 主线程 的回调
     */
    private void onSuccessToMainThread() {
        // 如果取消了,释放图片资源
        if (isCancel) {
            resource.recycle();
            // 释放资源
            release();
            return;
        }

        // 将图片 回调给Engine 组件 ,来做内存缓存的管理.
        engineJobCallback.onEngineJobCompleted(key, resource);

        // 将图片 返回给Request 组件
        for (ResourceCallback callback : resourceCallbackList) {
            // 引用计数 +1
            resource.acquired();
            callback.onResourceCallback(resource);
        }

        // 释放当前组件资源
        release();

    }

    /**
     * IO任务获取数据失败后切换到 主线程 的回调
     */
    private void onFailedToMainThread() {
        if (isCancel) {
            release();
            return;
        }

        // 将空值回调 给EngineJob
        engineJobCallback.onEngineJobCompleted(key, null);

        // 将空值 回调给 Engine
        for (ResourceCallback callback : resourceCallbackList) {
            callback.onResourceCallback(null);
        }


    }

    private void release() {
        resource = null;
        decodeJob = null;
        isCancel = false;
        key = null;
        resourceCallbackList.clear();
    }
}
