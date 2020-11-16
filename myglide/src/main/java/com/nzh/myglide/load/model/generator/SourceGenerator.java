package com.nzh.myglide.load.model.generator;

import com.nzh.myglide.MyGlide;
import com.nzh.myglide.cache.Key;
import com.nzh.myglide.load.DataSource;
import com.nzh.myglide.load.callback.DataFetcher;
import com.nzh.myglide.load.callback.DataGenerator;
import com.nzh.myglide.load.model.ModelLoader;

import java.util.List;

/**
 * 数据加载器：负责 从网络加载图片文件 的 实现。
 * 需要：
 * Context :
 * model ： 图片的来源。
 * 接口：将结果返回给 DecodeJob。
 */
public class SourceGenerator implements DataGenerator, DataFetcher.FetcherCallback<Object> {

    GeneratorCallback generatorCallback;
    MyGlide glide;
    Object model;

    List<ModelLoader> modelLoaders;
    Key key;

    public SourceGenerator(MyGlide glide, Object model, GeneratorCallback generatorCallback) {
        this.generatorCallback = generatorCallback;
        this.glide = glide;
        this.model = model;

        modelLoaders = glide.getRegistry().getMetchedModelLoaders(model);
    }

    @Override
    public boolean canFetcherAndDecode() {


        // 1: 遍历 modelloaders ，获取大LoadData. 从LoadData中拿到 要加载的数据 类型 Data 。
        // 2: 查找是否有对应的解码器来解码 Data 类型。
        //      Y：加载数据。  N: 返回false

        boolean hasLoadPath = false;
        for (ModelLoader modelLoader : modelLoaders) {
            ModelLoader.LoadData loadData = modelLoader.buildLoaderData(model);
            Class dataClass = loadData.fetcher.getDataClass();
            if (glide.getRegistry().hasLoadPath(dataClass)) {
                hasLoadPath = true;
                this.key = loadData.key;
                loadData.fetcher.fetchData(this);
                break;
            }
        }

        return hasLoadPath;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void onFetchReady(Object data) {
        generatorCallback.onDataReady(this.key, data, DataSource.REMOTE);
    }

    @Override
    public void onFetchFailed(Exception e) {
        generatorCallback.onDataFailed(this.key, e);
    }
}
