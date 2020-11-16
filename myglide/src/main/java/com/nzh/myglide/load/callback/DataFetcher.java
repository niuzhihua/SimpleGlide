package com.nzh.myglide.load.callback;

/**
 * 获取数据接口
 *
 * @param <Data>
 */
public interface DataFetcher<Data> {

    /**
     * 取消获取数据
     */
    void cancel();

    /**
     * 获取数据的实现
     *
     * @param callback 回调对象
     */
    void fetchData(FetcherCallback<Data> callback);

    /**
     * 要获取的数据类型
     *
     * @return
     */
    Class<Data> getDataClass();

    /**
     * 将结果回调 的接口
     *
     * @param <Data>
     */
    interface FetcherCallback<Data> {
        /**
         * 获取数据成功
         *
         * @param data
         */
        void onFetchReady(Data data);

        /**
         * 获取数据失败
         *
         * @param e
         */
        void onFetchFailed(Exception e);
    }


}
