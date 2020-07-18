package com.example.chatappdemo.giphy;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface GiphyApiService {

    String GIPHY_BASE_URL = "http://api.giphy.com/";


    @GET("v1/gifs/trending")
    Call<ResponseBody> getTrending(@Query("api_key") String apiKey,
                                   @Query("limit") int limit,
                                   @Query("fmt") String format);
    @GET("/v1/gifs/search")
    Call<ResponseBody> searchGif(@Query("api_key") String apiKey,
                                 @Query("q") String query,
                                 @Query("limit") int limit,
                                 @Query("fmt") String format);
}
