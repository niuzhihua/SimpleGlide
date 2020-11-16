package com.nzh.myglide.load.callback;

import com.nzh.myglide.cache.Key;
import com.nzh.myglide.cache.memory.ActiveResource;

/**
 * 用来将 数据返回给 Engine 组件, 以便做内存缓存的 管理.
 */
public interface EngineJobListener {

    void onEngineJobCompleted(Key key, ActiveResource resouce);

    void onEngineJobCanceled(Key key);
}
