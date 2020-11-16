package com.nzh.myglide.load.decoder;

import android.graphics.Bitmap;

/**
 * 解码 Bitmap 的接口
 *
 * @param <T>
 */
public interface ResourceDecoder<T> {
    /**
     * 是否支持解码
     *
     * @return
     */
    boolean handles();

    // 将 数据 根据指定宽高 解码出bitmap
    Bitmap decode(T source, int width, int height) throws Exception;
}
