package com.nzh.myglide.lifecycle.request;

import android.graphics.drawable.Drawable;

import com.nzh.myglide.MyGlide;
import com.nzh.myglide.cache.memory.ActiveResource;
import com.nzh.myglide.load.Engine;
import com.nzh.myglide.load.callback.ResourceCallback;
import com.nzh.myglide.load.callback.SizeReadyCallback;

import androidx.core.content.res.ResourcesCompat;

/**
 * 负责图片的加载、设置。
 * 加载: 1、获取需要加载的大小.
 * RequestOption : 固定大小的配置。
 * Target.SizeReadyCallback : 通知大小结果。
 */
public class Request implements SizeReadyCallback, ResourceCallback {

    // 上下文：用来获取资源
    private MyGlide glide;
    //
    private Object model;
    //
    private Target target;

    // 当前请求的 状态
    private Status status;

    // 图片的占位图及固定大小 配置
    private RequestOption requestOptions;
    // 加载失败图
    private Drawable errorDrawable;
    // 加载中的占位图
    private Drawable placeholderDrawable;

    // 用来执行图片加载的组件。
    Engine engine;

    // IO任务 加载图片的结果封装 : 用来取消IO任务。
    Engine.LoadStatus loadStatus;

    //  图片
    ActiveResource resource;

    /**
     * @param glide  上下文
     * @param option 当前request请求配置的占位图
     * @param model  图片的来源
     * @param target 图片包装器
     */
    public Request(MyGlide glide, RequestOption option, Object model, Target target) {
        this.glide = glide;
        this.model = model;
        this.target = target;
        engine = glide.getEngine();
        requestOptions = option;
    }

    /**
     * 取消加载图片
     */
    public void cancel() {
        // 取消回调
        if (loadStatus != null) {
            loadStatus.cancel();
        }
        // 更新为取消状态
        status = Status.CANCELLED;
        // 释放资源
        target.release();
    }

    /**
     * 暂停图片加载
     */
    public void pause() {
        if (status == Status.PAUSED) {
            return;
        }
        // 更新状态
        status = Status.PAUSED;
        // 清除资源
        if (this.resource != null) {
            this.resource.release();
        }
    }

    // 请求是否已经完成
    public boolean isCompleted() {
        return this.status == Status.CANCELLED;
    }

    // 请求是否 正在运行
    public boolean isRunning() {
        return this.status == Status.RUNNING || status == Status.WAITING_FOR_SIZE;
    }

    // 请求是否 取消了
    public boolean isCanceled() {
        return this.status == Status.CANCELLED;
    }

    // 请求是否 暂停了
    public boolean isPause() {
        return this.status == Status.PAUSED;
    }

    /**
     * 释放资源
     */
    public void release() {
        errorDrawable = null;
        placeholderDrawable = null;
        loadStatus = null;
        model = null;
        target = null;
        requestOptions = null;
        resource.release();
        resource = null;
    }

    @Override
    public void onSizeReady(int width, int height) {
        // 检查状态
        if (status != Status.WAITING_FOR_SIZE) {
            System.out.println("status 状态错误，应为WAITING_FOR_SIZE");
            return;
        }
        //更新状态
        status = Status.RUNNING;
        // 加载图片
        loadStatus = engine.load(glide, width, height, model, this);
    }

    /**
     * 图片加载的最终回调。 如果返回图片成功，则设置图片，否则设置错误占位图。
     *
     * @param resource 图片
     */
    @Override
    public void onResourceCallback(ActiveResource resource) {
        if (resource == null) {
            status = Status.FAILED;
            // 设置错误占位图
            target.onLoadFailed(getErrorPlaceHolder());
        } else {
            status = Status.COMPLETED;
            this.resource = resource;
            // 设置Bitmap
            target.setImageBitmap(resource);
        }
    }


    /**
     * 请求的状态
     */
    public enum Status {
        PENDING,    // 等待状态
        RUNNING,    // 正在加载状态
        WAITING_FOR_SIZE,  // 等待计算大小状态
        COMPLETED,  // 加载图片完成状态
        FAILED,     // 加载失败状态
        CANCELLED,  // 取消状态
        PAUSED,      // 暂停加载状态
    }


    /**
     * 确立 要加载多大的图片
     */
    public void begin() {
        // 1：设置加载状态为：WAITING_FOR_SIZE 等待大小的获取。
        status = Status.WAITING_FOR_SIZE;
        // 2：设置占位图
        if (getPlaceholderDrawable() != null)
            target.onLoadStarted(getPlaceholderDrawable());

        // 如果配置了大小，则直接 回调通知 大小。
        if (requestOptions.getOverrideHeight() > 0 && requestOptions.getOverrideWidth() > 0) {
            onSizeReady(requestOptions.getOverrideWidth(), requestOptions.getOverrideHeight());
        } else {
            // 计算大小 ,并 被回调。
            target.getSize(this);
        }
    }


    /**
     * 返回占位图
     *
     * @return
     */
    private Drawable getPlaceholderDrawable() {
        if (placeholderDrawable == null && requestOptions.getPlaceHolderId() > 0) {
            placeholderDrawable = loadDrawable(requestOptions.getPlaceHolderId());
        }
        return placeholderDrawable;
    }

    private Drawable getErrorPlaceHolder() {
        if (errorDrawable == null && requestOptions.getPlaceHolderId() > 0) {
            errorDrawable = loadDrawable(requestOptions.getErrorId());
        }
        return errorDrawable;
    }

    private Drawable loadDrawable(int resourceId) {
        return ResourcesCompat.getDrawable(glide.getContext().getResources(), resourceId, glide.getContext().getTheme());
    }


}
