package com.nzh.myglide.cache.pool;

import android.util.LruCache;

public class LruByteArrayPool implements ArrayPool {

    private int maxLenght;

    private LruCache<Integer, byte[]> lruCache;

    public LruByteArrayPool(int maxLenght) {
        this.maxLenght = maxLenght;

        lruCache = new LruCache<Integer, byte[]>(maxLenght) {

            /**
             * 返回缓存元素的大小
             * @param key
             * @param value
             * @return
             */
            @Override
            protected int sizeOf(Integer key, byte[] value) {
                return value.length;
            }


            @Override
            protected void entryRemoved(boolean evicted, Integer key, byte[] oldValue, byte[] newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
            }
        };
    }

    @Override
    public byte[] get(int length) {
        return new byte[0];
    }

    @Override
    public void put(byte[] data) {

    }

    @Override
    public void clearMemory() {

    }

    @Override
    public void trimMemory(int level) {

    }
}
