package com.nzh.myglide.load;

import android.graphics.Bitmap;

import com.nzh.myglide.MyGlide;
import com.nzh.myglide.cache.Key;
import com.nzh.myglide.cache.disk.wrapper.DiskCache;
import com.nzh.myglide.cache.memory.ActiveResource;
import com.nzh.myglide.load.callback.DecodeJobCallback;
import com.nzh.myglide.load.callback.DataGenerator;
import com.nzh.myglide.load.decoder.LoadPath;
import com.nzh.myglide.load.model.generator.DiskCacheGenerator;
import com.nzh.myglide.load.model.generator.SourceGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 最终去磁盘、网络加载图片的实现：
 * 肯定需要的要素：
 * <p>
 * 图片来源：model
 * 图片大小：width,height
 * 磁盘缓存工具
 * 接口：用来将结果回调给EngineJob
 */
public class DecodeJob implements Runnable, DataGenerator.GeneratorCallback {

    Object model;
    int width;
    int height;
    DiskCache diskCache;
    IOState state;
    boolean isCancel = false;
    DecodeJobCallback callback;

    MyGlide glide;

    Key key;

    DataGenerator currentGenerator;

    public DecodeJob(MyGlide glide, Object model, int width, int height, DiskCache diskCache, DecodeJobCallback callback) {
        this.glide = glide;
        this.model = model;
        this.width = width;
        this.height = height;
        this.diskCache = diskCache;
        this.callback = callback;
    }

    @Override
    public void run() {

        // 如果取消了，则返回。。
        if (isCancel) {
            callback.onLoadFailed(new RuntimeException("decode job has been canceled"));
            return;
        }

        // 更新 加载图片任务(IO)的状态.
        state = getNextState(IOState.INIT);
        currentGenerator = getCurrentStateGenerator();
        // 执行当前状态下的 数据加载器 来加载数据。
        runGenerator();
    }

    /**
     * 使用数据加载器 来加载数据
     */
    private void runGenerator() {

        // 能否 产生图片
        boolean canLoadAndDecode = false;

        //  循环 通过加载器 生成 xx
        while (true) {

            if (currentGenerator != null) {
                canLoadAndDecode = currentGenerator.canFetcherAndDecode();
                // 能产生图片则退出循环
                if (canLoadAndDecode) {
                    break;
                }
            }

            // 更新IO 状态.
            state = getNextState(state);
            // 如果是FINISH 状态 ，就结束
            if (state == IOState.FINIESHED) {
                break;
            }
            currentGenerator = getCurrentStateGenerator();
        }
        // 加载失败 或取消
        if (!canLoadAndDecode || isCancel) {
            callback.onLoadFailed(new RuntimeException("load failed or canceled"));
        }
    }

    /**
     * 1、对数据执行解码
     * 2、解码失败使用下一个加载器 加载数据。
     *
     * @param key
     * @param data
     * @param dataSource
     */
    @Override
    public void onDataReady(Key key, Object data, DataSource dataSource) {
        this.key = key;
        runLoadPath(data, dataSource);
    }

    /**
     * 1、通过LoadPath解码数据
     * 2、如果解码成功 则加入本地磁盘缓存。
     * 3、如果解码失败，则允许一下一个 数据加载器加载数据。
     *
     * @param data
     * @param dataSource
     */
    private <Data> void runLoadPath(Data data, DataSource dataSource) {
        LoadPath<Data> loadPath = glide.getRegistry().getLoadPath(data.getClass());
        final Bitmap bitmap = loadPath.decodeBitmap(data, width, height);
        if (bitmap != null) {

            //如果是远程设备获取的数据，则加入本地磁盘缓存
            if (dataSource == DataSource.REMOTE) {
                diskCache.put(key, new DiskCache.Writer() {
                    @Override
                    public boolean write(File file) {
                        FileOutputStream fos = null;

                        try {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        return false;
                    }
                });


                ActiveResource activeResouce = new ActiveResource(bitmap);
                // 将结果回调给调用者(EngineJob)
                callback.onResourceReady(activeResouce);
            }
        } else {
            // 解码失败 ： TODO
            runGenerator();
        }

    }


    @Override
    public void onDataFailed(Key key, Exception e) {
        callback.onLoadFailed(new RuntimeException("load failed  "));
    }

    /**
     * 从磁盘或网络加载这个io任务的流程阶段。
     */
    private enum IOState {
        INIT,       // 初始化阶段
        DATA_CACHE, // 从磁盘加载,使用文件缓存阶段。
        SOURCE,     // 从网络加载。
        FINIESHED   // 加载完成阶段。

    }


    /**
     * 根据当前状态 获取下一个阶段状态。
     *
     * @param currentState
     * @return
     */
    private IOState getNextState(IOState currentState) {

        switch (currentState) {
            case INIT:
                return IOState.DATA_CACHE;
            case DATA_CACHE:
                return IOState.SOURCE;
            case SOURCE:
            case FINIESHED:
                return IOState.FINIESHED;
            default:
                throw new IllegalStateException("IOState 状态异常");
        }
    }

    /**
     * 根据IO状态获取 对应的加载器。
     *
     * @return
     */
    private DataGenerator getCurrentStateGenerator() {
        switch (state) {
            case DATA_CACHE:
                return new DiskCacheGenerator(glide, glide.getDiskCache(), model, this);
            case SOURCE:
                return new SourceGenerator(glide, model, this);
            case INIT:
            case FINIESHED:
                return null;
            default:
                throw new IllegalStateException("IOState 状态异常");
        }
    }

    public void cancel() {

    }
}
