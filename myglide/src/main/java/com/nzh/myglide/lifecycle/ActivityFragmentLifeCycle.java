package com.nzh.myglide.lifecycle;

import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 用来将Fragment中的生命周期 回调 传递给 sets中的LifeCycleListener。
 */
public class ActivityFragmentLifeCycle implements LifeCycle {

    Set<LifeCycleListener> sets = Collections.newSetFromMap(new WeakHashMap<LifeCycleListener, Boolean>());

    private boolean isStart;
    private boolean isDestory;

    @Override
    public void addListener(LifeCycleListener listener) {
        sets.add(listener);
    }

    @Override
    public void removeListener(LifeCycleListener listener) {
        sets.remove(listener);
        Log.e("myglide", "removeListener");
    }


    public void doOnStart() {
        isStart = true;
        for (LifeCycleListener listener : sets) {
            listener.onStart();
        }
    }

    public void doOnStop() {
        isStart = false;
        for (LifeCycleListener listener : sets) {
            listener.onStop();
        }
    }

    public void doOnDestory() {
        isDestory = false;
        for (LifeCycleListener listener : sets) {
            listener.onDestory();
        }
    }
}
