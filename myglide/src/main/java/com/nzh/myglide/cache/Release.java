package com.nzh.myglide.cache;


/**
 * 用来释放内存缓存。
 * 内存缓存需要释放资源，所有的内存缓存接口继承此接口。
 */
public interface Release {

    void clearMemory();

    void trimMemory(int level);
}
