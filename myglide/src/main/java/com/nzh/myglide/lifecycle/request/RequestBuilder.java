package com.nzh.myglide.lifecycle.request;

import android.widget.ImageView;

import com.nzh.myglide.MyGlide;
import com.nzh.myglide.lifecycle.RequestManager;

import java.nio.file.Paths;
import java.nio.file.WatchService;

public class RequestBuilder {
    // 上下文工具类
    MyGlide glide;
    // 数据的数据源
    Object model;
    // 图片的配置项
    RequestOption option;
    // 管理Request
    RequestManager requestManager;

    public RequestBuilder(MyGlide glide, Object model, RequestManager requestManager) {
        this.glide = glide;
        this.model = model;
        this.requestManager = requestManager;
    }

    /**
     * 配置占位图
     *
     * @param option
     * @return
     */
    public RequestBuilder apply(RequestOption option) {
        this.option = option;
        return this;
    }

    /**
     * 找到一张图片，并设置到ImageView中。
     *
     * @param imageView
     */
    public void into(ImageView imageView) {
        //1、 在找图片前需要计算Image View的大小。
        // 将imageView 交给 Target
        Target target = new Target(imageView);


        //2、 如何找到一张图片？ 通过发送Request请求来 获取和管理图片。

        // 创建request
        Request request = new Request(glide, option, model, target);

        // 执行 request : 交给RequestManager管理
        requestManager.track(request);

    }
}
