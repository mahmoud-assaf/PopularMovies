package com.mahmoud.popularmovies.Provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mahmoud.popularmovies.Models.FavoriteMovie;
import com.mahmoud.popularmovies.Models.Movie;
import com.mahmoud.popularmovies.Models.Review;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mahmoud on 19/02/2018.
 */

public class ProviderAPI {
    private static ProviderAPI providerAPI;
    Context context;

    Uri moviesUri = MoviesProvider.CONTENT_URI;

    public static ProviderAPI getInstance(Context context) {
        if (providerAPI == null) {
            providerAPI = new ProviderAPI(context);
        }
        return providerAPI;
    }

    private ProviderAPI(Context context) {
        this.context=context;
    }



    public void addFavoriteMovie(FavoriteMovie movie) {
        // add a new movie record
            if(getMovieFromFavorites(movie.movie.getId())!=null){
                //movie already founded in favorites just return
                Toast.makeText(context,"Movie already added to your favorites",Toast.LENGTH_SHORT).show();
                return;
            }


        ContentValues values = new ContentValues();

        values.put(MoviesProvider.MOVIE_TITLE, movie.movie.getTitle());
        values.put(MoviesProvider.MOVIE_OVERVIEW, movie.movie.getOverview());
        values.put(MoviesProvider.MOVIE_RELEASE_DATE, movie.movie.getReleaseDate());
        values.put(MoviesProvider.MOVIE_RATING, String.valueOf(movie.movie.getVoteAverage()));
        values.put(MoviesProvider.MOVIE_TMDB_ID, String.valueOf(movie.movie.getId()));
        values.put(MoviesProvider.MOVIE_THUMB, movie.movie.getPosterPath());
        values.put(MoviesProvider.MOVIE_TRAILERS, trailersToString(movie.trailers));
        values.put(MoviesProvider.MOVIE_BACKDROP,movie.movie.getBackdropPath());
        values.put(MoviesProvider.MOVIE_REVIEWS, reviewsToString(movie.reviews));

        values.put(MoviesProvider.MOVIE_GENERS, movie.geners);
        Uri uri = context.getContentResolver().insert(MoviesProvider.CONTENT_URI, values);
        Toast.makeText(context,"Movie added to your favorites",Toast.LENGTH_LONG).show();

    }

    public List<Movie> getFavoriteMovies() {
        // retrieve all movies in favorites

        List<Movie> result=new ArrayList<Movie>();

        String orderBy = MoviesProvider.MOVIE_ID;
        Cursor c = context.getContentResolver().query(moviesUri, null, null, null, orderBy);

        if (c != null && c.moveToFirst()) {
            String title, releasedate, overview, thumb, geners;
            int tmdbID;
            double rating;
            List<Review> reviews;
            List<String> trailers;
            Movie movie;
            do {
                movie = new Movie();
                movie.setTitle(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_TITLE)));

                movie.setPosterPath(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_THUMB)));

                movie.setId(Integer.parseInt(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_TMDB_ID))));
                result.add(movie);

            } while (c.moveToNext());
        }
        if (c != null) {
            c.close();
        }
        return result;
    }


    public FavoriteMovie getMovieFromFavorites(int id) {
        // retrieve single movie

        FavoriteMovie result=null;

        String orderBy = MoviesProvider.MOVIE_ID;
        Cursor c = context.getContentResolver().query(moviesUri.buildUpon().appendPath(String.valueOf(id)).build(), null, null, null, orderBy);

        if (c != null && c.moveToFirst()) {
            result = new FavoriteMovie();
            String title, releasedate, overview, thumb, geners;
            int tmdbID;
            double rating;
            List<Review> reviews;
            List<String> trailers;
            Movie movie;

            movie = new Movie();
            movie.setTitle(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_TITLE)));
            movie.setReleaseDate(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_RELEASE_DATE)));
            movie.setOverview(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_OVERVIEW)));
            movie.setPosterPath(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_THUMB)));
            movie.setBackdropPath(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_BACKDROP)));
            movie.setVoteAverage(Double.parseDouble(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_RATING))));
            result.geners = c.getString(c.getColumnIndex(MoviesProvider.MOVIE_GENERS));
            result.reviews = stringToReviews(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_REVIEWS)));
            result.trailers = stringToTrailers(c.getString(c.getColumnIndex(MoviesProvider.MOVIE_TRAILERS)));
            result.movie = movie;


        }
        if (c != null) {
            c.close();
        }
        return result;
    }


    public  void deleteFavoriteMovie(int id){
        context.getContentResolver().delete(moviesUri.buildUpon().appendPath(String.valueOf(id)).build(),null, null);
    }


    //method helper to convert reviews class to string to store in database
    public  String reviewsToString(List<Review> reviews){
        String result="";
        if(reviews==null ||reviews.size()==0){
            return result;
        }


        int i;
        for (i = 0; i <reviews.size()-1 ; i++) {
            result+=reviews.get(i).author;
            result+=":::";
            result+=reviews.get(i).content;
            result+="|||";
        }
        result+=reviews.get(i).author;
        result+=":::";
        result+=reviews.get(i).content;
        return result;

    }

    //method helper to convert string of reviews  to review class
    public  List<Review> stringToReviews(String reviewsstr){

        //Log.d("reviews string",reviewsstr);
        List<Review> result=new ArrayList<Review>();
        if(reviewsstr.equals("")) return  result;
        String[] reviewSTR=reviewsstr.split("\\|\\|\\|");
        Review review;
        for (int i = 0; i <reviewSTR.length ; i++) {
           // Log.d("review num",reviewSTR[i]);
            String[] reveiw_parts=reviewSTR[i].split(":::");
            review=new Review();
            review.author=reveiw_parts[0];
            review.content=reveiw_parts[1];
            result.add(review);
            
        }
        return  result;
    }

    //method helper to convert trailers paths to string to store in database
    public  String trailersToString(List<String> trailerslinks){

        if(trailerslinks.size()==0) return "";
        String result="";
        int i;
        for (i = 0; i <trailerslinks.size()-1 ; i++) {
            result+=trailerslinks.get(i);
            result+="|||";
            
        }
        result+=trailerslinks.get(i);

return  result;
    }

    //method helper to convert trailers string to paths
    public  List<String> stringToTrailers(String trailersstr){
        List<String> result=new ArrayList<String>();
        String[] reviewSTR=trailersstr.split("\\|\\|\\|");
        String trailer;
        for (int i = 0; i <reviewSTR.length ; i++) {
            trailer=reviewSTR[i];

            result.add(trailer);

        }
        return  result;
    }



}
