package com.nzh.myglide.load.model.loaders;

import android.net.Uri;

import com.nzh.myglide.load.model.ModelLoader;
import com.nzh.myglide.load.model.ModelLoaderRegisty;

import java.io.InputStream;

/**
 * 实际是个代理ModelLoader
 */
public class StringModelLoader implements ModelLoader<String, InputStream> {

    ModelLoader<Uri, InputStream> realModelLoader;

    public StringModelLoader(ModelLoader<Uri, InputStream> realModelLoader) {
        this.realModelLoader = realModelLoader;
    }

    // 靠 真实ModelLoader 实现
    @Override
    public boolean isModelValid(String m) {
        return true;
    }

    // 靠 真实ModelLoader 实现
    @Override
    public LoadData buildLoaderData(String s) {
        return realModelLoader.buildLoaderData(Uri.parse(s));
    }


    public static class Factory implements ModelLoaderFactory<String, InputStream> {

        @Override
        public ModelLoader<String, InputStream> build(ModelLoaderRegisty registy) {

            return new StringModelLoader(registy.build(Uri.class, InputStream.class));
        }
    }
}
