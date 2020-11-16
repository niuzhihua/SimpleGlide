package com.nzh.myglide.load.decoder;

import android.graphics.Bitmap;

import java.util.List;

public class LoadPath<Data> {

    Class dataClass;
    List<ResourceDecoder<Data>> decoderList;

    public LoadPath(Class dataClass, List<ResourceDecoder<Data>> decoderList) {
        this.dataClass = dataClass;
        this.decoderList = decoderList;
    }

    public Bitmap decodeBitmap(Data data, int width, int height) {
        Bitmap bitmap = null;
        try {
            for (ResourceDecoder<Data> decoder : decoderList) {
                if (decoder.handles()) {
                    bitmap = decoder.decode(data, width, height);
                }
                if (bitmap != null) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
}
