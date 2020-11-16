package com.nzh.myglide.load.model;

import com.nzh.myglide.load.model.loaders.MultiModelLoader;

import java.util.ArrayList;
import java.util.List;

public class ModelLoaderRegisty {

    List<Entry> entries = new ArrayList<>();

    public void add(Class model, Class data, ModelLoader.ModelLoaderFactory factory) {
        // 注册 modelloader
        entries.add(new Entry(model, data, factory));
    }

    public ModelLoader getSupportLoader(Class modelClass, Class dataClass) {


        return null;
    }


    /**
     * 根据 model 和 data类型选出 唯一一个可用的 ModelLoader/MultiModelLoader
     *
     * @param modelClass model类型
     * @param dataClass  data类型
     * @param <Model>
     * @param <Data>
     * @return 类型匹配的ModelLoader
     */
    public <Model, Data> ModelLoader build(Class<Model> modelClass, Class<Data> dataClass) {

        List<ModelLoader<Model, Data>> loaders = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.isTypeMatch(modelClass, dataClass)) {
                ModelLoader modelLoader = entry.factory.build(this);
                loaders.add(modelLoader);
            }
        }

        if (loaders.size() > 1) {
            return new MultiModelLoader<>(loaders);
        } else if (loaders.size() == 1) {
            return loaders.get(0);
        }

        throw new RuntimeException("没有找到合适的ModelLoader");

    }

    /**
     * 查找 匹配 model 类型的 ModelLoader
     *
     * @param modelClass
     */
    public List<ModelLoader> getMatchedModelLoaders(Class modelClass) {
        List<ModelLoader> list = new ArrayList<>();
        for (Entry entry : entries) {
            if (entry.isTypeMatch(modelClass)) {
                ModelLoader modelLoader = entry.factory.build(this);
                list.add(modelLoader);
            }
        }
        return list;
    }


    /**
     * 用此bean来描述一个ModelLoader.
     */
    static class Entry {

        Class modelClass;
        Class dataClass;
        ModelLoader.ModelLoaderFactory factory;

        public Entry(Class modelClass, Class dataClass, ModelLoader.ModelLoaderFactory factory) {
            this.modelClass = modelClass;
            this.dataClass = dataClass;
            this.factory = factory;
        }

        /**
         * model 类型 和 Data 类型都匹配
         *
         * @param modelClass
         * @param dataClass
         * @return
         */
        public boolean isTypeMatch(Class modelClass, Class dataClass) {
            return this.modelClass.isAssignableFrom(modelClass) && this.dataClass.isAssignableFrom(dataClass);
        }

        /**
         * Model类型 匹配
         *
         * @param modelClass
         * @return
         */
        public boolean isTypeMatch(Class modelClass) {
            return this.modelClass.isAssignableFrom(modelClass);
        }


    }
}
