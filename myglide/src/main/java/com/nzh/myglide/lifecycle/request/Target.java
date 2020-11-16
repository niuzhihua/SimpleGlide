package com.nzh.myglide.lifecycle.request;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;

import com.nzh.myglide.cache.memory.ActiveResource;
import com.nzh.myglide.load.callback.SizeReadyCallback;

import java.lang.ref.WeakReference;

/**
 * 1、负责计算View 的大小，并将结果回调给调用者。
 * 2、设置占位图片
 */
public class Target {

    ImageView imageView;
    // imageView大小计算完成后的 回调
    private SizeReadyCallback cb;
    // 结合ViewTreeObserver 计算View大小
    private LayoutListener layoutListener;

    public Target(ImageView imageView) {
        this.imageView = imageView;
    }

    /**
     * 计算View 大小 ，并 回调给调用者。
     *
     * @param cb
     */
    public void getSize(SizeReadyCallback cb) {

        if (cb == null) {
            System.out.println("回调对象cb不能为null");
            return;
        }

        // 获取到大小后直接回调。
        int w = getImageViewWidth();
        int h = getImageViewHeight();
        if (w > 0 && h > 0) {
            cb.onSizeReady(w, h);
        }

        // 否则，保持回调对象,
        this.cb = cb;
        // 并继续获取大小

        if (null == layoutListener) {
            layoutListener = new LayoutListener(this);
            // 向view 树中注册一个监听，view树会在 绘制之前回调 这个listener.
            imageView.getViewTreeObserver().addOnPreDrawListener(layoutListener);
        }


    }

    private static final class LayoutListener implements ViewTreeObserver.OnPreDrawListener {

        WeakReference<Target> weakReference;

        public LayoutListener(Target target) {
            this.weakReference = new WeakReference<>(target);
        }

        @Override
        public boolean onPreDraw() {
            Target target = weakReference.get();
            if (target != null) {
                target.tryGetSize();
            }
            return true;
        }
    }

    /**
     *
     */
    private void tryGetSize() {

        int width = getImageViewWidth();
        int height = getImageViewHeight();
        if (width <= 0 && height <= 0) {
            Log.e("", "");
            return;
        }

        // 回调对象在 addOnPreDrawListener前设置了，所以不会为空

        cb.onSizeReady(width, height);

        // 释放资源
        release();
    }

    /**
     * 释放资源
     */
    public void release() {
        ViewTreeObserver observer = imageView.getViewTreeObserver();
        if (observer.isAlive()) {
            observer.removeOnPreDrawListener(layoutListener);
        }
        layoutListener = null;
        cb = null;

    }

    private int getImageViewHeight() {
        int verticalPadding = imageView.getPaddingTop() + imageView.getPaddingBottom();
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        int layoutParamSize = layoutParams != null ? layoutParams.height : 0;
        return getImageViewDimen(imageView.getHeight(), layoutParamSize, verticalPadding);
    }

    private int getImageViewWidth() {
        // ImageView的padding值
        int horizontalPadding = imageView.getPaddingLeft() + imageView.getPaddingRight();

        // ImageView的 param 的 width
        ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
        int layoutParamWidth = layoutParams != null ? layoutParams.width : 0;

        // ImageView的width
        int viewSize = imageView.getWidth();
        return getImageViewDimen(viewSize, layoutParamWidth, horizontalPadding);
    }

    /**
     * 计算ImageView的大小
     *
     * @param viewSize         ImageView的大小
     * @param layoutParamWidth 父容器给的大小
     * @param padding          padding大小
     * @return
     */
    private int getImageViewDimen(int viewSize, int layoutParamWidth, int padding) {
        // 固定的大小
        int adjustSize = layoutParamWidth - padding;
        if (adjustSize > 0) {
            return adjustSize;
        }
        // view.getWidth 能够获得大小
        adjustSize = viewSize - padding;
        if (adjustSize > 0) {
            return adjustSize;
        }
        // 布局属性设置的是WRAP_CONTENT 并且不能接到回调(addOnPreDrawListener)了
        if (!imageView.isLayoutRequested() && layoutParamWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {

            //
            return getMaxDisplayLength(imageView.getContext());
        }

        return 0;
    }


    private static int maxDisplayLength = -1;

    private static int getMaxDisplayLength(Context context) {
        if (maxDisplayLength == -1) {
            WindowManager windowManager =
                    (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();
            Point displayDimensions = new Point();
            display.getSize(displayDimensions);
            maxDisplayLength = Math.max(displayDimensions.x, displayDimensions.y);
        }
        return maxDisplayLength;
    }


    /**
     * 设置请求开始时 的占位图
     *
     * @param placeHolderDrawable 占位图
     */
    public void onLoadStarted(Drawable placeHolderDrawable) {
        imageView.setImageDrawable(placeHolderDrawable);
    }

    /**
     * 设置请求失败后 的占位图
     *
     * @param failedDrawable
     */
    public void onLoadFailed(Drawable failedDrawable) {
        imageView.setImageDrawable(failedDrawable);
    }
    public void setImageBitmap(ActiveResource resource) {
        imageView.setImageBitmap(resource.getBitmap());
    }


}
