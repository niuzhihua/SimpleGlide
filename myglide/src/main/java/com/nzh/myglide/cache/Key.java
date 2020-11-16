package com.nzh.myglide.cache;

import java.security.MessageDigest;

public interface Key {


    /**
     * 目的： 将Key对象转为字符串。这个字符串作为 缓存时的Key.
     * @param messageDigest
     */
    void updateDiskCacheKey(MessageDigest messageDigest);

    byte[] getKeyBytes();

    @Override
    boolean equals(Object o);

    @Override
    int hashCode();

}
