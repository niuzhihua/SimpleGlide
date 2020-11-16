package com.nzh.myglide.cache.pool;

import android.graphics.Bitmap;

import com.nzh.myglide.cache.MyLruCache;

import java.util.NavigableMap;
import java.util.TreeMap;


/**
 * 图片复用池： Integer ：图片大小 Bitmap：图片
 */
public class BitmapPool extends MyLruCache<Integer, Bitmap> {

    /**
     * 区分 是手动调用remove 删除 还是 LRU 淘汰算法删除 的。
     * false ：手动调用remove 删除
     * true: LRU 自动删除
     */
    boolean isRemoveByCall = true;

    // 从复用池取图片时，要复用的图片  不超过 被复用图片 的几倍。
    final int TIMES = 4; // 给4倍

    NavigableMap<Integer, Integer> utilMap = new TreeMap<>();

    public BitmapPool(int maxSize) {
        super(maxSize);

    }


    /**
     * 加入复用池
     *
     * @param bitmap
     */
    public void put(Bitmap bitmap) {

        if (!bitmap.isMutable())
            // 图片不可复用
            return;

        if (bitmap.getAllocationByteCount() > maxSize()) {
            // 图片太大，也不复用。
            return;
        }

        put(bitmap.getAllocationByteCount(), bitmap);

        // 记录添加的图片大小
        utilMap.put(bitmap.getAllocationByteCount(), null);
    }

    /**
     * 从复用池获取一个可复用其内存的 图片。
     *
     * @param width
     * @param height
     * @param config
     * @return
     */
    public Bitmap get(int width, int height, Bitmap.Config config) {


        int type = 2;
        if (config == Bitmap.Config.ARGB_8888) {
            type = 4;
        } else if (config == Bitmap.Config.ARGB_4444 || config == Bitmap.Config.RGB_565) {
            type = 2;
        }
        // 要复用图片的 大小
        int size = width * height * type;

        // 借助 NavigableMap 工具 求出 大于或等于 size 的key

        Integer avaliableSize = utilMap.ceilingKey(size);

        // 注意 avaliableSize 已经大于 size 了
        if (avaliableSize != null && avaliableSize < size * TIMES) {
            // 将图片从复用池 移除 。
            isRemoveByCall = true;
            Bitmap bitmap = remove(avaliableSize);
            // 标记手动删除
            isRemoveByCall = false;
            return bitmap;
        }

        return null;

    }

    @Override
    protected int sizeOf(Integer key, Bitmap value) {
        return value.getAllocationByteCount();
    }

    /**
     * 元素被移除时触发， 有2种情况：
     * 1 ：主动调用remove移除   不需要recycle ，要将图片放入活动缓存。
     * 2：缓存淘汰算法移除。 需要recycle
     *
     * @param evicted  true if the entry is being removed to make space, false
     *                 if the removal was caused by a {@link #put} or {@link #remove}.
     * @param key
     * @param oldValue 被移除的元素
     * @param newValue the new value for {@code key}, if it exists. If non-null,
     *                 this removal was caused by a {@link #put}. Otherwise it was caused by
     */
    @Override
    protected void entryRemoved(boolean evicted, Integer key, Bitmap oldValue, Bitmap newValue) {
        super.entryRemoved(evicted, key, oldValue, newValue);

        utilMap.remove(key);

        // LRU 自动删除的  要recycle.
        if (isRemoveByCall) {
            oldValue.recycle();
        }
    }




}
