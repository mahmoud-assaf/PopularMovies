package com.mahmoud.popularmovies.Rest;

import com.mahmoud.popularmovies.Models.MovieDetailsResponse;
import com.mahmoud.popularmovies.Models.MoviesResponse;
import com.mahmoud.popularmovies.Models.ReviewsResponse;
import com.mahmoud.popularmovies.Models.VideosRresponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by mahmoud on 18/02/2018.
 */

public interface RetrofitApiInterface {
    @GET("movie/top_rated")
    Call<MoviesResponse> getTopRatedMovies(@Query("api_key") String apiKey);

    @GET("movie/popular")
    Call<MoviesResponse> getPopularMovies(@Query("api_key") String apiKey);


    @GET("movie/{id}")
    Call<MovieDetailsResponse> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<VideosRresponse> getMovieVideos(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<ReviewsResponse> getMovieReviews(@Path("id") int id, @Query("api_key") String apiKey);

}
