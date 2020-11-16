package com.nzh.myglide.cache.memory;

import com.nzh.myglide.cache.Key;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 活动缓存：管理正在使用的资源.
 *
 *      活动缓存移除的图片 放入 内存缓存。
 * <p>
 * get/put/remove
 */
public class ActiveResouceCache {
    /**
     * 标记 检测回收线程 是否关闭了。
     */
    private  boolean isClose ;

    /**
     * 检测 活动缓存(map)里面的 元素(value) 是否被回收了。
     */
    Thread checkIsRecycledThread;

    /**
     * 引用队列 : 当一个对象只剩下 弱引用时，gc扫描过内存后会回收此对象，
     * 如果设置了 引用队列，那么会将弱引用对象添加到 队列中。
     * <p>
     * 作用： 用来通知 对象被回收了。
     */
    ReferenceQueue<ActiveResource> queue = new ReferenceQueue<>();

    /**
     * 活动缓存
     */
    private Map<Key, MyWeakReference<ActiveResource>> map = new HashMap<>();


    // 用来监听活动缓存里的每一个图片资源。
    ActiveResource.OnResourceReleaseListener listener ;
    public ActiveResouceCache(ActiveResource.OnResourceReleaseListener listener){
        this.listener = listener ;
    }

    /**
     * 将图片加入活动缓存， 并监听每一张图片的引用计数情况。
     *
     * @param key
     * @param value
     */
    public void put(Key key, ActiveResource value) {
        value.setReleaseListener(key,listener);
        map.put(key, new MyWeakReference<>(key, value, queue));
    }

    /**
     * 从活动缓存移除
     *
     * @param key
     */
    public ActiveResource remove(Key key) {
        MyWeakReference<ActiveResource> reference = map.remove(key);
        if (reference != null) {
            return reference.get();
        }
        return null;
    }

    /**
     * 从活动缓存中获取 资源。
     * @param key
     * @return
     */
    public ActiveResource get(Key key){
        MyWeakReference<ActiveResource> reference = map.remove(key);
        if (reference != null) {
            return reference.get();
        }
        return null;
    }


    /**
     * 检测 对象是否被回收了。
     *
     * @return
     */
    public ReferenceQueue<ActiveResource> getQueue() {

        if (checkIsRecycledThread == null) {
            checkIsRecycledThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    while(!isClose){
                        try {
                            // queue.remove 是阻塞方法
                            MyWeakReference<ActiveResource> reference = (MyWeakReference<ActiveResource>) queue.remove();
                            // 从 缓存中删除
                            map.remove(reference.key);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            checkIsRecycledThread.start();
            isClose = false;
        }

        return queue;
    }

    /**
     * 关闭 检测回收线程。
     */
    public  void closeCheckThread(){
        isClose = true;
        if(checkIsRecycledThread!=null){
            // 关闭线程
            checkIsRecycledThread.interrupt();
            //等待 线程关闭。 等待5秒。
            try {
                checkIsRecycledThread.join(TimeUnit.SECONDS.toMillis(5));
                if(checkIsRecycledThread.isAlive()){
                    throw new IllegalArgumentException("等待关闭异常(join exception)");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static final class MyWeakReference<T> extends WeakReference<T> {

        Key key;

        public MyWeakReference(Key key, T referent, ReferenceQueue<? super T> q) {
            super(referent, q);
            this.key = key;
        }
    }

}
