package com.nzh.myglide.lifecycle;

public interface LifeCycle {

    void addListener(LifeCycleListener listener);

    void removeListener(LifeCycleListener listener);
}
