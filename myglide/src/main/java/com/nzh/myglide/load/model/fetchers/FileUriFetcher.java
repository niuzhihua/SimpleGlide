package com.nzh.myglide.load.model.fetchers;

import android.content.ContentResolver;
import android.net.Uri;

import com.nzh.myglide.load.callback.DataFetcher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 通过Uri 从磁盘获取文件(图片)的实现
 */
public class FileUriFetcher implements DataFetcher<InputStream> {

    Uri uri;
    ContentResolver cr;

    boolean isCancel = false;

    public FileUriFetcher(Uri uri, ContentResolver cr) {
        this.uri = uri;
        this.cr = cr;
    }

    @Override
    public void cancel() {
        isCancel = true;
    }

    @Override
    public void fetchData(FetcherCallback<InputStream> callback) {
        if (isCancel) {
            return;
        }
        InputStream is = null;
        try {
            is = cr.openInputStream(this.uri);
            callback.onFetchReady(is);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            callback.onFetchFailed(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }
}
