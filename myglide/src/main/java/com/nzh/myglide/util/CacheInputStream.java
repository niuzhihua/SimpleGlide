package com.nzh.myglide.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * 可以重复使用的流。
 * 背景：由于 解码bitmap 时需要两次读取流，第一次 读取大小，第二次读完整流解码图片。
 * 如果不对流做处理，则第二次解码失败，因为流不是完整的。 所以对流进行扩展，
 * 使得第二次解码时，也能读取到完整的流。
 */
public class CacheInputStream extends InputStream {


    // 流中需要缓存的位置 : 0 - needCachePosition 需要缓存
    private int needCachePosition = 0; // 缓存下标

    private int hasReadSize = 0;    // 记录读取流的长度。

    /**
     * 已经读取过的字节放入此缓存。
     */
    private byte[] cache;

    InputStream is;

    public CacheInputStream(InputStream is) {
        this.is = is;
        // 64K
        cache = new byte[64 * 1024];
    }


    /**
     * 控制流是否支持 标记(mark)和 重置(reset)。
     *
     * @return true: 支持
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void reset() throws IOException {
        // 缓存下标 设 为 0  ： 下次读取的时候，就可以从缓存的0位置读取了。
        needCachePosition = 0;
    }

    /**
     * 标记当前读取的位置。
     *
     * @param readlimit 在标记位置失效之前，允许读取的最大字节。
     */
    @Override
    public synchronized void mark(int readlimit) {
//        markPosition = needCachePosition;
//        markPosition = 0; // TODO
        needCachePosition = 0;
    }

    // 读 方法的实现1 ：单个字节读
    @Override
    public int read() throws IOException {

        // 解析图片大小的情况：          需要缓存流数据的情况下，这两个值是相等的。
        // 解析图片的情况：    此时流已经不是完整数据。两个值不相等，因为reset了。
        //                      needCachePosition 肯定比hasReadSize 小。
        if (needCachePosition < hasReadSize) { // 命中则在解析整个图片，此时先读缓存。
            // 如果被reset，则从缓存读。
            needCachePosition++;    // 记录从缓存读的大小。
            return cache[needCachePosition];
        }

        // 读取
        int nextData = is.read();
        // 判断是否读完了
        if (nextData == -1) {
            return -1;
        }


        hasReadSize++; // 记录已读长度
        needCachePosition++; // 记录需要缓存的长度

        if (needCachePosition >= cache.length) {
            throw new RuntimeException("1需要扩容吗");
        }
        cache[needCachePosition] = (byte) nextData;  // 缓存数据

        return nextData;
    }

    // 读 方法的实现2 ：多个字节读
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * @param b   缓冲区
     * @param off 写入缓冲区时的开始下标。 写死了 0.
     * @param len 读取的最大字节数。
     * @return
     * @throws IOException
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {

        // 缓冲区的长度 : 必 大于 0.
        int bufferSize = len - off;

        // 相减: hasReadSize - needCachePosition
        // 解析图片大小的情况：    两个值相等。 相减value等于0. value < bufferSize.
        // 解析图片的情况： 两个值不想等，因为reset了。且 needCachePosition < hasReadSize.
        //      相减value大于 0. value >= bufferSize.

        int value = hasReadSize - needCachePosition;

        if (value >= bufferSize) {  // 解析图片 : 先从缓存读。
            // 将缓存数据拷贝到缓冲区
            // 此时已经reset过了。 needCachePosition = 0
            System.arraycopy(cache, needCachePosition, b, off, bufferSize);
            // 记录从缓存读的大小。
            needCachePosition += bufferSize;
            return bufferSize;
        }

        // 解析图片大小
        if (value > 0) {
            throw new RuntimeException("value 值必小于0");
        }


        // 将数据读到缓冲区
        int size = is.read(b, off, bufferSize);
        // 判断是否读完了
        if (size == -1) {
            return -1;
        }
        if (needCachePosition + size >= cache.length) {
            throw new RuntimeException("2需要扩容吗");
        }
        // 将缓冲区数据 读到 缓存
        System.arraycopy(b, off, cache, needCachePosition, size);
        needCachePosition += size;      // 记录读到缓存的长度
        // 记录读取的长度
        hasReadSize += size;
        // 返回读的长度。
        return size;

    }


    @Override
    public void close() throws IOException {
        if (cache != null) {
            cache = null;
        }
        is.close();
    }

    public void release() {
        if (cache != null) {
            cache = null;
        }
    }
}
