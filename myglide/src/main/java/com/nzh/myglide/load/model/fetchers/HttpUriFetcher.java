package com.nzh.myglide.load.model.fetchers;

import android.net.Uri;

import com.nzh.myglide.cache.Key;
import com.nzh.myglide.load.callback.DataFetcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.Hashtable;

/**
 * 通过Uri 从网络 获取文件(图片)的实现
 */
public class HttpUriFetcher implements DataFetcher<InputStream> {

    Key key;
    Uri uri;

    boolean isCancel = false;

    public HttpUriFetcher(Key key, Uri uri) {
        this.key = key;
        this.uri = uri;
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
        HttpURLConnection httpURLConnection = null;
        URL url;
        InputStream is = null;
        try {
            url = new URL(uri.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();
            is = httpURLConnection.getInputStream();
            int resCode = httpURLConnection.getResponseCode();
            if (isCancel) {
                return;
            }
            if (resCode == HttpURLConnection.HTTP_OK) {
                callback.onFetchReady(is);
            } else {
                callback.onFetchFailed(new RuntimeException(httpURLConnection.getResponseMessage()));
            }
        } catch (Exception e) {
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
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }


    }

    @Override
    public Class<InputStream> getDataClass() {
        return InputStream.class;
    }
}
