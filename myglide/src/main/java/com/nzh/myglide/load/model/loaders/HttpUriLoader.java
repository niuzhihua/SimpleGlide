package com.nzh.myglide.load.model.loaders;

import android.content.ContentResolver;
import android.net.Uri;

import com.nzh.myglide.load.model.ModelLoader;
import com.nzh.myglide.load.model.ModelLoaderRegisty;
import com.nzh.myglide.load.model.StringKey;
import com.nzh.myglide.load.model.fetchers.HttpUriFetcher;

import java.io.InputStream;

public class HttpUriLoader implements ModelLoader<Uri, InputStream> {
    @Override
    public boolean isModelValid(Uri model) {
        return "http".equalsIgnoreCase(model.getScheme()) || "https".equalsIgnoreCase(model.getScheme());
    }

    @Override
    public LoadData buildLoaderData(Uri model) {
        StringKey stringKey = new StringKey(model);
        // 生成 http Fetcher
        HttpUriFetcher httpUriFetcher = new HttpUriFetcher(stringKey, model);
        // 生成 LoadData 返回
        return new LoadData<>(httpUriFetcher, stringKey);
    }


    public static class Factory implements ModelLoaderFactory {

        @Override
        public ModelLoader build(ModelLoaderRegisty registy) {

            return new HttpUriLoader();
        }
    }
}
