package com.nzh.myglide.lifecycle.fragment;

import com.nzh.myglide.lifecycle.ActivityFragmentLifeCycle;
import com.nzh.myglide.lifecycle.RequestManager;

import androidx.fragment.app.Fragment;

/**
 * 向Activity添加的Fragment. 当Activity的生命周期变化时，接收到 onStart、onStop、onDestory方法。
 * 并传递给 RequestManager ，从而到达自动通知 ReqeustManager 来
 * 执行请求、停止请求、清除请求 。  这样就实现了自动管理生命周期。
 */
public class SupportRequestManagerFragment extends Fragment {

    /**
     * 管理请求：停止、执行、清除。
     */
    RequestManager requestManager;
    /**
     * 用来传递生命周期。
     */
    ActivityFragmentLifeCycle lifeCycle;


    public SupportRequestManagerFragment() {
        lifeCycle = new ActivityFragmentLifeCycle();

    }

    public RequestManager getRequestManager() {
        return requestManager;
    }

    public ActivityFragmentLifeCycle getLifeCycle() {
        return lifeCycle;
    }

    public void setRequestManager(RequestManager requestManager) {
        this.requestManager = requestManager;
    }

    @Override
    public void onStart() {
        super.onStart();
        lifeCycle.doOnStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        lifeCycle.doOnStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifeCycle.doOnDestory();
    }
}
