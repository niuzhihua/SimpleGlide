package com.nzh.myglide.cache.memory;

import android.os.Build;

import com.nzh.myglide.cache.Key;
import com.nzh.myglide.cache.MyLruCache;

/**
 * 内存缓存 实现类：
 *
 *      如果是主动从内存缓存中移除图片，则 放入活动缓存。
 *      如果是由于LRU淘汰算法 移除图片，则 将图片放入 复用池。
 */
public class LruMemoryCache  extends MyLruCache<Key, ActiveResource> implements MemoryCache {

    ResouceRemoveListener removeListener ;

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public LruMemoryCache(int maxSize) {
        super(maxSize);
    }

    /**
     *  使用LruCache 需要重写的方法。返回当前元素的大小
     * @param key
     * @param value
     * @return 当前元素大小
     */
    @Override
    protected int sizeOf(Key key, ActiveResource value) {

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT){
            // 4.4 以上 返回 图片占用的 内存 (考虑到内存复用的情况)。
            return value.getBitmap().getAllocationByteCount();
        }
        return value.getBitmap().getByteCount();
    }


    /**
     * 通知内存缓存 ，元素被移除掉了。
     * @param evicted true if the entry is being removed to make space, false
     *     if the removal was caused by a {@link #put} or {@link #remove}.
     * @param key
     * @param oldValue 被移除的元素
     * @param newValue the new value for {@code key}, if it exists. If non-null,
     *     this removal was caused by a {@link #put}. Otherwise it was caused by
     */
    @Override
    protected void entryRemoved(boolean evicted, Key key, ActiveResource oldValue, ActiveResource newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);

        if(removeListener!=null && oldValue!=null)
            removeListener.onRemoveFromLRUMemoryCache(oldValue);

    }

    @Override
    public ActiveResource remove2(Key key) {
        // TODO
        return null;
    }

    @Override
    public void setResouceRemoveListener(ResouceRemoveListener listener) {
        this.removeListener = listener;
    }
}
