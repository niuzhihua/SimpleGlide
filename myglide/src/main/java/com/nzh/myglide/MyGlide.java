package com.nzh.myglide;

import android.content.ComponentCallbacks;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;

import com.nzh.myglide.cache.disk.wrapper.DiskCache;
import com.nzh.myglide.cache.memory.MemoryCache;
import com.nzh.myglide.cache.pool.BitmapPool;
import com.nzh.myglide.lifecycle.RequestManager;
import com.nzh.myglide.lifecycle.RequestManagerRetriver;
import com.nzh.myglide.lifecycle.request.RequestOption;
import com.nzh.myglide.load.Engine;
import com.nzh.myglide.load.decoder.StreamBitmapDecoder;
import com.nzh.myglide.load.model.loaders.FileModelLoader;
import com.nzh.myglide.load.model.loaders.FileUriLoader;
import com.nzh.myglide.load.model.loaders.HttpUriLoader;
import com.nzh.myglide.load.model.loaders.StringModelLoader;

import java.io.File;
import java.io.InputStream;

import androidx.fragment.app.FragmentActivity;

public class MyGlide implements ComponentCallbacks {
    // 上下文
    // MyGlide配置项 :GlideBuilder
    // 内存缓存工具

    // 生命周期管理工具
    RequestManagerRetriver retriver;
    // 图片复用池

    // 总注册机 ：用来存放 ModelLoader注册机 和 图片解码器注册机
    private Registry registry;
    // 磁盘缓存工具
    private DiskCache diskCache;

    //内存缓存工具
    MemoryCache memoryCache;
    // 图片复用池
    BitmapPool bitmapPool;

    private Context context;
    // 图片加载组件
    private Engine engine;
    // 全局的：图片的加载展位图，失败展示图配置
    private RequestOption globalRequestOption;

    protected MyGlide(Context context, MyGlideBuilder builder) {
        this.context = context;
        memoryCache = builder.memoryCache;
        diskCache = builder.diskCache;
        engine = builder.engine;
        bitmapPool = builder.bitmapPool;
        // 用来注册生命周期管理 。
        retriver = new RequestManagerRetriver(this);
        // 图片的配置项
        globalRequestOption = new RequestOption();
        //  ModelLoader 和 图片解码器的 注册机
        registry = new Registry();

        ContentResolver cr = context.getContentResolver();
        registry.add(File.class, InputStream.class, new FileModelLoader.Factory())
                .add(String.class, InputStream.class, new StringModelLoader.Factory())
                .add(Uri.class, InputStream.class, new HttpUriLoader.Factory())
                .add(Uri.class, InputStream.class, new FileUriLoader.Factory(cr))
                .addDecoder(InputStream.class, new StreamBitmapDecoder(bitmapPool));
    }


    private static MyGlide glide;

    private static MyGlide get(Context context) {
        if (glide == null) {
            synchronized (MyGlide.class) {
                if (glide == null) {
                    init(context, new MyGlideBuilder());
                }
            }
        }
        return glide;
    }

    /**
     * 这样写可以传入自定义的 GlideBuilder ，从而来配置Glide.
     *
     * @param context
     * @param builder
     */
    public static void init(Context context, MyGlideBuilder builder) {
        if (glide != null) {
            // 命中表示重新配置了Glide
            resetGlide();
        }
        // 利用GlideBuilder 构建
        glide = builder.build(context.getApplicationContext());
        // 注册application 监听。
        context.registerComponentCallbacks(glide);
    }

    private static void resetGlide() {
        if (glide == null) {
            return;
        }
        // 关闭图片加载引擎
        glide.engine.shutDown();
        // 注销 application 监听
        glide.getContext().unregisterComponentCallbacks(glide);
        glide = null;

    }

    public static RequestManager with(FragmentActivity context) {
        return MyGlide.get(context).retriver.get(context);
    }


    public Registry getRegistry() {
        return registry;
    }

    public DiskCache getDiskCache() {
        return diskCache;
    }

    public Context getContext() {
        return context;
    }

    public BitmapPool getBitmapPool() {
        return bitmapPool;
    }

    public Engine getEngine() {
        return engine;
    }

    public RequestOption getGlobalRequestOption() {
        return globalRequestOption;
    }

    public MemoryCache getMemoryCache() {
        return memoryCache;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

    }

    @Override
    public void onLowMemory() {

    }
}
