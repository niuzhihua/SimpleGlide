package com.nzh.myglide.load.decoder;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nzh.myglide.cache.pool.BitmapPool;
import com.nzh.myglide.util.CacheInputStream;

import java.io.InputStream;

/**
 * 1、负责 将Stream流 解析成 Bitmap
 * 2、准备内存复用：将可复用的Bitmap 添加到 复用池。
 */
public class StreamBitmapDecoder implements ResourceDecoder<InputStream> {
    BitmapPool bitmapPool;

    public StreamBitmapDecoder(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    @Override
    public boolean handles() {
        // 先写死
        return true;
    }

    /**
     * @param source
     * @param width  目标宽：图片的宽不大于的width
     * @param height 目标高：图片的高不大于的height
     * @return
     * @throws Exception
     */
    @Override
    public Bitmap decode(InputStream source, int width, int height) throws Exception {

        CacheInputStream is = null;
        if (source instanceof CacheInputStream) {
            is = (CacheInputStream) source;
        } else {
            is = new CacheInputStream(source);
        }

        is.mark(0);

        // 1、解码出图片的 原配置
        BitmapFactory.Options options = new BitmapFactory.Options();
        // 只读取图片的 outWidth,outHeight
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        // 必须重置
        options.inJustDecodeBounds = false;
        // 原宽高
        int sourceWidth = options.outWidth;
        int sourceHeight = options.outHeight;

        // 2、确定目标宽高
        int targetWidth = width < 0 ? sourceWidth : width;
        int targetHeight = height < 0 ? sourceHeight : height;

        // 3、计算缩放因子
        float heightFactor = targetHeight / (float) sourceHeight;
        float widthFactor = targetWidth / (float) sourceWidth;
        // 取最大的缩放因子
        float factor = Math.max(widthFactor, heightFactor);

        // 4、计算出最终要显示的宽，高
        int outWidth = Math.round(factor * sourceWidth);
        int outHeight = Math.round(factor * sourceHeight);


        //

        int widthScaleFactor = sourceWidth % outWidth == 0 ? sourceWidth / outWidth : sourceWidth
                / outWidth + 1;
        int heightScaleFactor = sourceHeight % outHeight == 0 ? sourceHeight / outHeight :
                sourceHeight / outHeight + 1;
        // 2
        int sampleSize = Math.max(widthScaleFactor, heightScaleFactor);
        sampleSize = Math.max(1, sampleSize);

        //

        options.inSampleSize = sampleSize;

        // TODO: 读取图片字节数据,根据图片格式来设置
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        // 查找可复用的内存
        Bitmap reUseMemory = bitmapPool.get(outWidth, outHeight, Bitmap.Config.RGB_565);
        if (reUseMemory != null) {
            options.inMutable = true;  // 设置为允许复用
            options.inBitmap = reUseMemory; // 复用
        }

        // 缓存 流数据的标记
        is.reset();

        // 解码出Bitmap
        Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

        // TODO close ?
        is.release();

        return bitmap;

    }
}
