package com.nzh.myglide.lifecycle;

import android.util.Log;

import com.nzh.myglide.MyGlide;
import com.nzh.myglide.lifecycle.request.Request;
import com.nzh.myglide.lifecycle.request.RequestBuilder;
import com.nzh.myglide.lifecycle.request.RequestTrackUtil;

/**
 * 负责管理 Request。
 */
public class RequestManager implements LifeCycleListener {

    // 管理请求Request的工具。
    RequestTrackUtil trackUtil;

    LifeCycle lifeCycle;

    MyGlide glide;

    public RequestManager(MyGlide glide, LifeCycle lifeCycle) {
        this.glide = glide;
        this.lifeCycle = lifeCycle;
        // 注册观察者： 即注册生命周期回调，
        // Fragment生命周期变化传递给 集合中的LifeCycleListener，然后调用到本观察者。
        lifeCycle.addListener(this);
        trackUtil = new RequestTrackUtil();
    }

    public RequestBuilder load(String model) {
        return new RequestBuilder(glide, model, this);
    }

    @Override
    public void onStart() {
        // 恢复执行所有请求
        resumeAllRequest();
    }

    @Override
    public void onStop() {
        // 暂停所有请求
        pauseAllRequest();
        Log.e("myglide", "onStop");
    }

    @Override
    public void onDestory() {
        Log.e("myglide", "onDestory");
        // 清理所有请求
        cleanAllRequest();
        // 移除生命周期监听。
        lifeCycle.removeListener(this);
    }


    /**
     * 预执行 一个请求: 这个请求也有可能被暂停，比如列表滑动时不加载图片。
     *
     * @param request
     */
    public void track(Request request) {
        trackUtil.runRequest(request);
    }

    /**
     * 暂停所有请求 (Request)
     */
    public void pauseAllRequest() {
        trackUtil.pauseAllRequest();
    }

    /**
     * 恢复执行 所有请求 (Request)
     */
    public void resumeAllRequest() {
        trackUtil.resumeAllRequest();
    }

    /**
     * 清除所有的请求
     */
    public void cleanAllRequest() {
        trackUtil.cleanAllRequest();
        Log.e("myglide", "cleanAllRequest");
    }
}
