package com.nzh.myglide.load.model.loaders;

import android.net.Uri;

import com.nzh.myglide.load.model.ModelLoader;
import com.nzh.myglide.load.model.ModelLoaderRegisty;

import java.io.File;
import java.io.InputStream;

/**
 * 实际是个代理ModelLoader
 */
public class FileModelLoader implements ModelLoader<File, InputStream> {

    ModelLoader<Uri, InputStream> modelLoader;

    public FileModelLoader(ModelLoader<Uri, InputStream> modelLoader) {
        this.modelLoader = modelLoader;
    }

    // 靠真实ModelLoader 实现
    @Override
    public boolean isModelValid(File m) {
        return true;
    }

    // 靠真实ModelLoader 实现
    @Override
    public LoadData buildLoaderData(File f) {
        return modelLoader.buildLoaderData(Uri.fromFile(f));
    }


    public static class Factory implements ModelLoaderFactory<File, InputStream> {

        @Override
        public ModelLoader<File, InputStream> build(ModelLoaderRegisty registy) {
            return new FileModelLoader(registy.build(Uri.class, InputStream.class));
        }
    }
}
