package com.nzh.myglide.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;

import com.nzh.myglide.MyGlide;
import com.nzh.myglide.lifecycle.fragment.SupportRequestManagerFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * 用来获取RequestManager
 */
public class RequestManagerRetriver implements Handler.Callback {

    final String FRAGMENT_TAG = "FRAGMENT_TAG";
    final int REMOVE_FRAFMENT = 111;

    MyGlide glide;

    public RequestManagerRetriver(MyGlide glide) {
        this.glide = glide;
    }

    /**
     * 为了防止重复创建Fragment,使用临时集合来保存创建的Fragment. 随后再删除。
     */
    private ArrayMap<FragmentManager, SupportRequestManagerFragment> tempPool = new ArrayMap<>();

    private Handler handler = new Handler(Looper.getMainLooper(), this);


    /**
     * 无法进行生命周期管理
     */
    RequestManager applicationRequestManager;

    public RequestManager getApplicationRequestManager() {
        if (applicationRequestManager == null) {
            applicationRequestManager = new RequestManager(glide, new ApplicationLifeCycle());
        }
        return applicationRequestManager;
    }

    /**
     * 根据传入的context类型 来添加 相应的Fragment
     *
     * @param context
     */
    public RequestManager get(Context context) {

        if (context instanceof Application) {
            return getApplicationRequestManager();
        } else if (context instanceof FragmentActivity) { // AppCompatActivity extends FragmentActivity
            // v4 Fragment / androidx.Fragment
            SupportRequestManagerFragment fragment = getByFragmentActivity((FragmentActivity) context);
            RequestManager requestManager = fragment.getRequestManager();
            if (requestManager == null) {

                requestManager = new RequestManager(glide, fragment.getLifeCycle());
                // 将 requestManager和 fragment 绑定。
                fragment.setRequestManager(requestManager);
            }
            return requestManager;

        } else if (context instanceof Activity) {

            // android.app.Fragment
            throw new RuntimeException("未实现1");
        } else if (context instanceof ContextWrapper) {
            throw new RuntimeException("未实现2");
        }

        throw new RuntimeException("未实现");
    }


    private SupportRequestManagerFragment getByFragmentActivity(FragmentActivity fa) {
        FragmentManager fm = fa.getSupportFragmentManager();
        // 1、从FragmentManager找 Fragment
        SupportRequestManagerFragment fragment = (SupportRequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        // 2  从缓存中找 Fragment.
        if (fragment == null) {
            fragment = tempPool.get(fm);
            // 3、找不到则创建.
            if (fragment == null) {
                fragment = new SupportRequestManagerFragment();
                // 4、加入临时缓存
                tempPool.put(fm, fragment);
                // 5、添加Fragment
                fm.beginTransaction().add(fragment, FRAGMENT_TAG).commitAllowingStateLoss();
                // 为了防止重复创建Fragment
                // 6、发送消息 ，从缓存中移除 Fragment
                handler.obtainMessage(REMOVE_FRAFMENT, fm).sendToTarget();

            }
        }

        return fragment;
    }

    @Override
    public boolean handleMessage(Message msg) {

        int w = msg.what;

        if (w == REMOVE_FRAFMENT) {
            FragmentManager fm = (FragmentManager) msg.obj;
            tempPool.remove(fm);
        }

        return true;
    }
}
