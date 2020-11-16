package com.nzh.myglide.load.model.loaders;

import com.nzh.myglide.load.model.ModelLoader;

import java.util.List;

/**
 * 实际是个代理ModelLoader
 * <p>
 * 为什么要有这个类，因为有多个相同类型的 model 匹配 Data 的情况。
 * 比如 string 类型 model, 可以匹配多种Uri.
 * Uri : http , Uri : file .
 * 那么如何表示StringModelLoader呢？
 * 这里让 它持有一个MultiModelLoader。运行时再次筛选出唯一的ModelLoader.
 *
 * @param <Model>
 * @param <Data>
 */
public class MultiModelLoader<Model, Data> implements ModelLoader<Model, Data> {

    List<ModelLoader<Model, Data>> loaderList;

    public MultiModelLoader(List<ModelLoader<Model, Data>> loaderList) {
        this.loaderList = loaderList;
    }

    // 靠 真实ModelLoader 实现
    @Override
    public boolean isModelValid(Model m) {
        for (ModelLoader<Model, Data> modelLoader : loaderList) {
            if (modelLoader.isModelValid(m)) {
                // 如果有一个ModelLoader 是可用的，则表示 m可被处理。返回true即可。
                return true;
            }
        }
        return false;
    }
    // 靠 真实ModelLoader 实现
    @Override
    public LoadData buildLoaderData(Model m) {
        for (ModelLoader<Model, Data> modelLoader : loaderList) {
            if (modelLoader.isModelValid(m)) {
                // 使用真实ModelLoader 来生成 LoadData
                return modelLoader.buildLoaderData(m);
            }
        }
        return null;
    }
}
