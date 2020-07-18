package com.example.chatappdemo.giphy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

import okhttp3.Cache;
import okhttp3.Interceptor;

final class CacheInterceptor implements Interceptor {
    private static final int CACHE_SIZE = 5242880;          //5 MB //Cache size.

    @NonNull
    private final Context mContext;
    private final int mCacheTimeMills;

    CacheInterceptor(@NonNull final Context context,
                     final int cacheTimeMins) {
        mContext = context;
        mCacheTimeMills = cacheTimeMins * 60000;
    }

    static Cache getCache(@NonNull final Context context) {
        //Define mCache
        File httpCacheDirectory = new File(context.getCacheDir(), "responses");
        return new Cache(httpCacheDirectory, CACHE_SIZE);
    }

    @Override
    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
        okhttp3.Response originalResponse = chain.proceed(chain.request());
        if (isNetworkAvailable(mContext)) {
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=" + mCacheTimeMills)
                    .build();
        } else {
            int maxStale = 2419200; // tolerate 4-weeks stale
            return originalResponse.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
    }

    private boolean isNetworkAvailable(@NonNull final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
