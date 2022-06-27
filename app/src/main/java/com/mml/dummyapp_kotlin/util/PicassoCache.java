package com.mml.dummyapp_kotlin.util;

import android.annotation.SuppressLint;
import android.content.Context;

import com.squareup.picasso.Downloader;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class PicassoCache {

    @SuppressLint("StaticFieldLeak")
    private static Picasso picassoInstance = null;

    private PicassoCache(Context context) {

        Downloader downloader = new OkHttp3Downloader(context, Integer.MAX_VALUE);
        Picasso.Builder builder = new Picasso.Builder(context);
        builder.downloader(downloader);
        picassoInstance = builder.build();
    }

    public static Picasso getPicassoInstance(Context context) {

        if (picassoInstance == null) {

            //noinspection InstantiationOfUtilityClass
            new PicassoCache(context);
            return picassoInstance;
        }

        return picassoInstance;
    }
}