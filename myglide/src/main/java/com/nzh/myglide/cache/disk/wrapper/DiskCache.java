package com.nzh.myglide.cache.disk.wrapper;


import com.nzh.myglide.cache.Key;

import java.io.File;

public interface DiskCache {


    interface Writer {
        boolean write(File file);
    }

    File get(Key key);

    void put(Key key, Writer writer);

    void delete(Key key);

    void clear();
}
