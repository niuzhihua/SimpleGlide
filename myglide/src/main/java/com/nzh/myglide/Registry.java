package com.nzh.myglide;

import com.nzh.myglide.cache.Key;
import com.nzh.myglide.load.decoder.LoadPath;
import com.nzh.myglide.load.decoder.ResourceDecoder;
import com.nzh.myglide.load.decoder.ResourceDecoderRegistry;
import com.nzh.myglide.load.model.ModelLoader;
import com.nzh.myglide.load.model.ModelLoaderRegisty;

import java.util.ArrayList;
import java.util.List;

public class Registry {
    // ModelLoader注册机
    ModelLoaderRegisty modelLoaderRegisty = new ModelLoaderRegisty();

    // 图片解码器注册机
    ResourceDecoderRegistry decoderRegistry = new ResourceDecoderRegistry();

    /**
     * 注册ModelLoader
     *
     * @param modelClass
     * @param dataClass
     * @param factory
     * @return
     */
    public Registry add(Class modelClass,
                        Class dataClass,
                        ModelLoader.ModelLoaderFactory factory) {

        modelLoaderRegisty.add(modelClass, dataClass, factory);

        return this;
    }


    /**
     * 注册图片解码器
     */
    public <T> Registry addDecoder(Class dataClass, ResourceDecoder<T> decoder) {
        decoderRegistry.add(dataClass, decoder);
        return this;
    }


    /**
     * 根据model获取所有匹配的LoadData
     *
     * @param model
     * @return
     */
    public List<ModelLoader.LoadData> getMatchedLoaderDatas(Object model) {
        // 根据model 查找匹配的 ModelLoader
        List<ModelLoader> modelLoaders = modelLoaderRegisty.getMatchedModelLoaders(model.getClass());
        // 根据ModelLoader 获得 LoadData
        List<ModelLoader.LoadData> loadDatas = new ArrayList<>();
        for (ModelLoader modelLoader : modelLoaders) {
            ModelLoader.LoadData loadData = modelLoader.buildLoaderData(model.getClass());
            loadDatas.add(loadData);
        }
        return loadDatas;
    }


    /**
     * 根据Model类型 ，查找匹配的 ModelLoader(多个),,,,,最后查出匹配的 Key 集合.
     * <p>
     * 一个model 对应多个 ModelLoader ，例如：
     * model = Uri --httpUri --ModelLoader
     * model = Uri --FileUri --ModelLoader
     * <p>
     * 因为不确定用户传递的什么Uri. 所以一个model对应多个ModelLoader.
     * 一个ModelLoader对应一个LoadData .
     * model -- ModelLoader   = 一对多
     * ModelLoader -- LoadData  = 一对一。
     *
     * @param model
     */
    public List<Key> getMetchedKeys(Object model) {
        // 根据model 查找匹配的 ModelLoader
        List<ModelLoader> modelLoaders = modelLoaderRegisty.getMatchedModelLoaders(model.getClass());
        // 根据ModelLoader 获得 LoadData
        List<ModelLoader.LoadData> loadDatas = new ArrayList<>();
        for (ModelLoader modelLoader : modelLoaders) {
            ModelLoader.LoadData loadData = modelLoader.buildLoaderData(model);
            loadDatas.add(loadData);
        }
        //根据LoadData 获取到 Key s
        List<Key> keys = new ArrayList<>();
        for (ModelLoader.LoadData loadData : loadDatas) {
            keys.add(loadData.key);
        }
        return keys;
    }

    /**
     * 根据Model类型 ，查找匹配的 ModelLoader(多个)
     *
     * @param model
     * @return
     */
    public List<ModelLoader> getMetchedModelLoaders(Object model) {
        // 根据model 查找匹配的 ModelLoader s
        return modelLoaderRegisty.getMatchedModelLoaders(model.getClass());
    }

    /**
     * 是否有对应的解码器 匹配
     *
     * @param dataClass
     * @return
     */
    public boolean hasLoadPath(Class dataClass) {
        return decoderRegistry.hasDecoder(dataClass);
    }


    public <Data> LoadPath<Data> getLoadPath(Class dataClass) {
        // 获取到解码器列表
        List<ResourceDecoder<Data>> decoderList = decoderRegistry.getMatchedDecoder(dataClass);
        return new LoadPath<>(dataClass, decoderList);
    }
}
