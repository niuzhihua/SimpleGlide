package com.nzh.myglide.load.model;

import com.nzh.myglide.cache.Key;
import com.nzh.myglide.load.callback.DataFetcher;

/**
 * ModelLoader分为两种，一种是代理ModelLoader，一种是 真实ModelLoader。
 * <p>
 * 代理ModelLoader ： 包括 FileModelLoader、StringModelLoader、MultiModelLoader.
 * 代理ModelLoader 持有 真实ModelLoader ，实现此接口也是靠真实ModelLoader。
 * <p>
 * 真实ModelLoader ：FileUriModelLoader、HttpUriModelLoader.
 *
 * @param <Model>
 * @param <Data>
 */
public interface ModelLoader<Model, Data> {

    /**
     * 检测model 是否合法
     *
     * @param m
     * @return true:合法的 图片model
     */
    boolean isModelValid(Model m);

    /**
     * 根据 model 获取 LoadData
     *
     * @param m
     * @return
     */
    LoadData buildLoaderData(Model m);

    class LoadData<Data> {
        public DataFetcher<Data> fetcher;
        public Key key;

        public LoadData(DataFetcher<Data> fetcher, Key key) {
            this.fetcher = fetcher;
            this.key = key;
        }
    }

    /**
     * ModelLoader 工厂
     *
     * @param <Model>
     * @param <Data>
     */
    interface ModelLoaderFactory<Model, Data> {

        ModelLoader<Model, Data> build(ModelLoaderRegisty registy);

    }
}
