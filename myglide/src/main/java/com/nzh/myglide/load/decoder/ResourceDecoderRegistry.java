package com.nzh.myglide.load.decoder;

import com.nzh.myglide.load.Engine;

import java.util.ArrayList;
import java.util.List;

/**
 * 用来注册解码器
 */
public class ResourceDecoderRegistry {


    List<Entry<?>> list = new ArrayList<>();

    /**
     * 注册图片解码器
     *
     * @param dataClass
     * @param resourceDecoder
     * @param <DECODER>
     */
    public <DECODER> void add(Class dataClass, ResourceDecoder<DECODER> resourceDecoder) {
        list.add(new Entry<>(dataClass, resourceDecoder));
    }


    public <Data> List<ResourceDecoder<Data>> getMatchedDecoder(Class<Data> dataClass) {
        List<ResourceDecoder<Data>> decoders = new ArrayList<>();
        for (Entry entry : list) {
            if (entry.isTypeMatch(dataClass)) {
                decoders.add(entry.decoder);
            }
        }
        return (decoders.size() == 0) ? null : decoders;
    }

    /**
     * 是否有解码器 能解码数据类型。
     *
     * @param dataClass 数据类型
     * @return
     */
    public boolean hasDecoder(Class dataClass) {
        boolean hasDecoders = false;
        for (Entry entry : list) {
            if (entry.isTypeMatch(dataClass)) {
                hasDecoders = true;
                break;
            }
        }
        return hasDecoders;
    }

    static class Entry<T> {
        Class dataClass;
        ResourceDecoder<T> decoder;

        public Entry(Class dataClass, ResourceDecoder<T> decoder) {
            this.dataClass = dataClass;
            this.decoder = decoder;
        }

        public boolean isTypeMatch(Class dataClass) {
            return this.dataClass.isAssignableFrom(dataClass);
        }
    }
}
