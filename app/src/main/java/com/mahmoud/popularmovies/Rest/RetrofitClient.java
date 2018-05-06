package com.mahmoud.popularmovies.Rest;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by mahmoud on 18/02/2018.
 */

public class RetrofitClient {
    public static final String BASE_URL = "http://api.themoviedb.org/3/";
    public static final String BASE_IMAGES_URL = "http://image.tmdb.org/t/p/w342/";
    public  static  final String API_KEY="";
    private static Retrofit retrofit = null;


    public static Retrofit getClient() {
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
