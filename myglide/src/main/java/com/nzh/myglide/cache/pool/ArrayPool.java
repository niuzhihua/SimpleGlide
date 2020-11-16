package com.nzh.myglide.cache.pool;

import com.nzh.myglide.cache.Release;

/**
 *  byte[] 缓存池
 *   key: 字节数组的长度
 *   value:字节数组
 */
public interface ArrayPool extends Release {


    byte[] get(int length);

    void put(byte[] data);


}
