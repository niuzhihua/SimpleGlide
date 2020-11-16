package com.nzh.myglide.load.model.loaders;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.nzh.myglide.load.model.ModelLoader;
import com.nzh.myglide.load.model.ModelLoaderRegisty;
import com.nzh.myglide.load.model.StringKey;
import com.nzh.myglide.load.model.fetchers.FileUriFetcher;

import java.io.InputStream;

public class FileUriLoader implements ModelLoader<Uri, InputStream> {

    ContentResolver cr;

    public FileUriLoader(ContentResolver cr) {
        this.cr = cr;
    }

    /**
     * 校验格式是否符合  SCHEME_FILE
     *
     * @param model
     * @return
     */
    @Override
    public boolean isModelValid(Uri model) {
        return ContentResolver.SCHEME_FILE.equalsIgnoreCase(model.getScheme());
    }

    @Override
    public LoadData buildLoaderData(Uri model) {
        // 生成key
        StringKey stringKey = new StringKey(model);
        // 生成fetcher工具
        FileUriFetcher fileUriFetcher = new FileUriFetcher(model, cr);
        // 生成LoadData工具
        return new LoadData<>(fileUriFetcher, stringKey);
    }


    public static class Factory implements ModelLoaderFactory {

        ContentResolver cr;

        public Factory(ContentResolver cr) {
            this.cr = cr;
        }

        @Override
        public ModelLoader build(ModelLoaderRegisty registy) {
            return new FileUriLoader(cr);
        }
    }
}
