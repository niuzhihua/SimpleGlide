package com.nzh.myglide.load.callback;

import com.nzh.myglide.cache.Key;
import com.nzh.myglide.load.DataSource;

/**
 * 负责用不同的 数据加载器(从磁盘加载的，从网络加载的)加载数据。
 * 并将结果回调。
 */
public interface DataGenerator {


    /**
     * 用来将结果回调给 DecodeJob
     */
    interface GeneratorCallback {
        void onDataReady(Key key, Object data, DataSource dataSource);

        void onDataFailed(Key key, Exception e);
    }

    /**
     * 是否能加载成功一张图
     * 1、是否有合适的ModelLoader 来用。
     * 2、并且加载的InputStream 能够找到解码器 解码图片。
     *
     * @return true: 能生成一张图。
     */
    boolean canFetcherAndDecode();

    /**
     * 取消加载
     */
    void cancel();

}
