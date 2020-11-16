package com.nzh.myglide.load.model.generator;

import com.nzh.myglide.MyGlide;
import com.nzh.myglide.cache.Key;
import com.nzh.myglide.cache.disk.wrapper.DiskCache;
import com.nzh.myglide.load.DataSource;
import com.nzh.myglide.load.callback.DataGenerator;
import com.nzh.myglide.load.model.ModelLoader;
import com.nzh.myglide.load.callback.DataFetcher;

import java.io.File;
import java.util.List;

/**
 * 数据加载器：负责 从磁盘加载图片文件 的 实现。
 * 需要：
 * Context :
 * 磁盘缓存工具:
 * <p>
 * Key :
 * model ： 图片的来源。
 * DataGenerator.Callback接口：将结果返回给 DecodeJob。
 */
public class DiskCacheGenerator implements DataGenerator, DataFetcher.FetcherCallback<Object> { // TODO 为什么是Object

    DiskCache diskCache;
    Object model;
    GeneratorCallback generatorCallback;

    MyGlide glide;

    List<Key> keys;
    Key key;

    public DiskCacheGenerator(MyGlide glide, DiskCache diskCache, Object model, GeneratorCallback callback) {
        this.glide = glide;
        this.diskCache = diskCache;
        this.model = model;
        this.generatorCallback = callback;

        /**
         * 根据 model 的类型 匹配出 可处理此类型的 所有ModelLoader.
         */
        keys = glide.getRegistry().getMetchedKeys(model);
    }

    /**
     * 去磁盘加载 数据，并解码出Bitmap.
     * <p>
     * 1、查找 匹配 model 的ModelLoader. 并用modelloader 去加载数据 data。
     * 2、查找是否有合适的解码器能解码  data.
     *
     * @return
     */
    @Override
    public boolean canFetcherAndDecode() {

        // 0：根据model获取所有支持model类型的 Keys:  model --> ModelLoader-->LoadData-->Key
        // 1: 通过key去磁盘获取 File. 空则返回false

        // 由于去磁盘加载,这里进行了一次model 转换。 String -- File

        // 2: 以File为model 重新 获取所有的ModelLoader .   model --> ModelLoader
        // 3: 遍历 modelloaders ，获取大LoadData. 从LoadData中拿到 要加载的数据 类型 Data 。
        // 4: 查找是否有对应的解码器来解码 Data 类型。
        //      Y：加载数据。  N: 返回false

        boolean findModelLoaders = false;
        List<ModelLoader> modelLoaders = null;
        for (Key key : keys) {
            File file = diskCache.get(key);
            if (file != null) {
                modelLoaders = glide.getRegistry().getMetchedModelLoaders(model);
                this.key = key;
                findModelLoaders = true;
                break;
            }
        }
        if (!findModelLoaders) {
            return false;
        }

        boolean hasLoadPath = false;
        for (ModelLoader modelLoader : modelLoaders) {
            ModelLoader.LoadData loadData = modelLoader.buildLoaderData(model);
            Class dataClass = loadData.fetcher.getDataClass();
            if (glide.getRegistry().hasLoadPath(dataClass)) {
                hasLoadPath = true;
                loadData.fetcher.fetchData(this);
                break;
            }
        }
        return hasLoadPath;
    }

    @Override
    public void cancel() {

    }

    /**
     * 磁盘加载成功 的回调
     *
     * @param data
     */
    @Override
    public void onFetchReady(Object data) {
        generatorCallback.onDataReady(this.key, data, DataSource.DATA_DISK_CACHE);
    }


    /**
     * 磁盘加载失败 的回调
     *
     * @param e
     */
    @Override
    public void onFetchFailed(Exception e) {
        generatorCallback.onDataFailed(this.key, e);
    }
}
