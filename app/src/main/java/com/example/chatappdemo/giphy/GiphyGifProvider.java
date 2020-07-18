package com.example.chatappdemo.giphy;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.chatappdemo.gifs.Gif;
import com.example.chatappdemo.gifs.GifProviderProtocol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public final class GiphyGifProvider implements GifProviderProtocol {

    private final String mApiKey;

    private final Context mContext;

    @SuppressWarnings("ConstantConditions")
    private GiphyGifProvider(@NonNull final Context context,
                             @NonNull final String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) throw new RuntimeException("Invalid GIPHY key.");
        mContext = context;
        mApiKey = apiKey;
    }

    public static GiphyGifProvider create(@NonNull final Context context,
                                          @NonNull final String apiKey) {
        return new GiphyGifProvider(context, apiKey);
    }

    @Nullable
    private static String getStringFromStream(@NonNull final InputStream inputStream) throws IOException {
        //noinspection ConstantConditions
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) total.append(line).append('\n');
        return total.toString();
    }

    @Nullable
    @Override
    public List<Gif> getTrendingGifs(final int limit) {
        try {
            Response<ResponseBody> responseBody = new Retrofit.Builder()
                    .baseUrl(GiphyApiService.GIPHY_BASE_URL)
                    .client(new OkHttpClient.Builder()
                            .addNetworkInterceptor(new CacheInterceptor(mContext, 15))
                            .cache(CacheInterceptor.getCache(mContext))
                            .build())
                    .build()
                    .create(GiphyApiService.class)
                    .getTrending(mApiKey, limit, "json")
                    .execute();


            //Check if the response okay?
            if (responseBody.code() == 200 && responseBody.body() != null) {
                //noinspection ConstantConditions
                String response = getStringFromStream(responseBody.body().byteStream());
                if (response == null) return null;

                JSONArray data = new JSONObject(response).getJSONArray("data");
                ArrayList<Gif> gifs = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject images = data.getJSONObject(i).getJSONObject("images");
                    Gif gif = new Gif(images.getJSONObject("original").getString("url"),
                            images.has("preview_gif") ? images.getJSONObject("preview_gif").getString("url") : null,
                            images.has("original_mp4") ? images.getJSONObject("original_mp4").getString("mp4") : null);
                    gifs.add(gif);
                }
                return gifs;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @Override
    public List<Gif> searchGifs(final int limit, @NonNull final String query) {
        try {
            Response<ResponseBody> responseBody = new Retrofit.Builder()
                    .baseUrl(GiphyApiService.GIPHY_BASE_URL)
                    .client(new OkHttpClient.Builder()
                            .addNetworkInterceptor(new CacheInterceptor(mContext, 4))
                            .cache(CacheInterceptor.getCache(mContext))
                            .build())
                    .build()
                    .create(GiphyApiService.class)
                    .searchGif(mApiKey, query, limit, "json")
                    .execute();


            //Check if the response okay?
            if (responseBody.code() == 200 && responseBody.body() != null) {
                //noinspection ConstantConditions
                String response = getStringFromStream(responseBody.body().byteStream());
                if (response == null) return null;

                JSONArray data = new JSONObject(response).getJSONArray("data");
                ArrayList<Gif> gifs = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject images = data.getJSONObject(i).getJSONObject("images");
                    Gif gif = new Gif(images.getJSONObject("original").getString("url"),
                            images.has("preview_gif") ? images.getJSONObject("preview_gif").getString("url") : null,
                            images.has("original_mp4") ? images.getJSONObject("original_mp4").getString("mp4") : null);
                    gifs.add(gif);
                }
                return gifs;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
