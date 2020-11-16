package com.nzh.myglide.lifecycle.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 最终负责对Request的执行，暂停，清除操作。
 */
public class RequestTrackUtil {

    /**
     * 由于没有WeakHashSet,只有WeakHashMap,所以将 WeakHashMap转化一下。
     */
    private Set<Request> requests = Collections.newSetFromMap(new WeakHashMap<Request, Boolean>());
    // 请求是否暂停了
    boolean isPause = false;


    /**
     * 停止的Request需要放入此集合。
     * 防止停止的Request被回收。
     */
    private List<Request> pendingRequests = new ArrayList<>();

    /**
     * 暂停请求
     */
    public void pauseAllRequest() {
        isPause = true;
        for (Request request : requests) {
            if (request.isRunning()) {
                request.pause();
                // 为了防止被回收，加入 等待集合。
                pendingRequests.add(request);
            }
        }
    }

    /**
     * 继续执行请求的实现
     */
    public void resumeAllRequest() {
        for (Request request : requests) {
            // 如果没有取消、没有完成、就继续执行
            if (!request.isCanceled() && !request.isCompleted()) {
                request.begin();
            }
        }
        pendingRequests.clear();
    }

    /**
     * 执行请求的实现
     *
     * @param request
     */
    public void runRequest(Request request) {
        // 加入到集合 来管理
        requests.add(request);

        if (isPause) {
            pendingRequests.add(request);
        } else {
            request.begin();
        }
    }

    /**
     * 清除请求
     */
    public void cleanAllRequest() {
        pendingRequests.clear();
        // 释放资源
        for (Request request : requests) {
            request.release();
        }
        requests.clear();
    }
}
