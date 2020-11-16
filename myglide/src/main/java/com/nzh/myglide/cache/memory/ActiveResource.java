package com.nzh.myglide.cache.memory;

import android.graphics.Bitmap;

import com.nzh.myglide.cache.Key;

/**
 * 活动资源 (可以理解为一张图片)： 采用引用计数 来控制 缓存的 缓存。
 * <p>
 * 因为同一张图片可能在多个地方被同时使用，每一次使用都会将引用计数+1,
 * 而当引用计数为0时候，则表示这个图片没有被使用也就是没有强引用了。
 * 这样则会将图片从活动资源中移除，并加入内存缓存。
 */
public class ActiveResource {

    private Key key;
    private Bitmap bitmap;
    // 引用计数 变量。
    private int acquired;

    public ActiveResource(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    // 当前活动资源没有被引用时触发的监听。
    private OnResourceReleaseListener releaseListener;

    public void setReleaseListener(Key key, OnResourceReleaseListener releaseListener) {
        this.key = key;
        if (releaseListener == null)
            throw new IllegalArgumentException("活动资源引用计数监听不能为空。");
        this.releaseListener = releaseListener;
    }

    /**
     * 当前资源被引用时(使用的时候)，计数 + 1
     */
    public void acquired() {
        if (bitmap.isRecycled()) {
            throw new IllegalArgumentException("引用计数异常，活动资源已被回收了。");
        }
        ++acquired;
    }

    /**
     * 释放 当前活动资源
     */
    public void release() {
        acquired--;
        if (acquired == 0) {
            releaseListener.onRemoveFromActiveCache(this.key, this);
        }
    }

    /**
     * 释放当前活动资源。
     */
    public void recycle() {
        // 命中表示还引用着 ，不释放。
        if (acquired > 0) {
            return;
        }
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    /**
     * 当引用计数为0时，从活跃缓存移除该图片.
     */
    public interface OnResourceReleaseListener {
        /**
         * 引用计数为0时触发，表示当前图片资源没有被使用了。
         *
         * @param key
         * @param resouce
         */
        void onRemoveFromActiveCache(Key key, ActiveResource resouce);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
