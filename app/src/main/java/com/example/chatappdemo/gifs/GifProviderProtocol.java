package com.example.chatappdemo.gifs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.List;

public interface GifProviderProtocol {

    @Nullable
    @WorkerThread
    List<Gif> getTrendingGifs(int limit);

    @Nullable
    @WorkerThread
    List<Gif> searchGifs(int limit, @NonNull String query);
}
