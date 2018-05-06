package com.mahmoud.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.mahmoud.popularmovies.Adapter.MoviesGridAdapter;
import com.mahmoud.popularmovies.Models.Movie;
import com.mahmoud.popularmovies.Models.MoviesResponse;
import com.mahmoud.popularmovies.Provider.ProviderAPI;
import com.mahmoud.popularmovies.Rest.RetrofitApiInterface;
import com.mahmoud.popularmovies.Rest.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MoviesGridAdapter.ItemClickListener{


    RecyclerView rvMovies;
    MoviesGridAdapter adapter;
    List<Movie> movies;
    RetrofitApiInterface theMoviesDbService;
    Utils utils;
    public ProviderAPI providerAPI;
    TextView tvStatus;

    public static CurrentDisplay currentDisplay=CurrentDisplay.TOP_RATED;
    static final String Current_DISPLAY = "CurrentDisplay";

    static final String MOVIE_ID = "movieID";
     static final String MOVIE_NAME ="movieName" ;
    static final String FROM_FAVORITES ="fromFavorites";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            int display=savedInstanceState.getInt(Current_DISPLAY);
            if(display==CurrentDisplay.TOP_RATED.ordinal())
                currentDisplay=CurrentDisplay.TOP_RATED;

            else if (display==CurrentDisplay.MOST_POPULAR.ordinal())
                currentDisplay=CurrentDisplay.MOST_POPULAR;

            else
            currentDisplay=CurrentDisplay.FAVORITES;




        }

        utils=new Utils(this);
        providerAPI=ProviderAPI.getInstance(this);
        setContentView(R.layout.activity_main);

        tvStatus=findViewById(R.id.status_tv);

        rvMovies=findViewById(R.id.moviesrv);
        int numberOfColumns = utils.calculateNoOfColumns();
        Log.e("num of columns ", "total: " + numberOfColumns);
        rvMovies.setLayoutManager(new GridLayoutManager(getApplicationContext(), numberOfColumns));

        theMoviesDbService = RetrofitClient.getClient().create(RetrofitApiInterface.class);



            //return from onsavedinstance for example rotation
        switch (currentDisplay) {

            case TOP_RATED:
                getTopRatedMovies(this);
                break;
            case MOST_POPULAR:
                 getPopularMovies(this);
                 break;
            case FAVORITES:
                getFavoriteMovies(this);
                break;

            //start with top rated movies


        }



    }


    public  void  getTopRatedMovies(Context context){

        if(!utils.isOnline()){
            utils.showMessage(getString(R.string.no_connection),tvStatus,true);
            return;
        }

        utils.showMessage(getString(R.string.status_connecting),tvStatus,false);

        Call<MoviesResponse> call = theMoviesDbService.getTopRatedMovies(RetrofitClient.API_KEY);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse>call, Response<MoviesResponse> response) {
                utils.showMessage(getString(R.string.status_laoding),tvStatus,false);
                Log.e("code",String.valueOf(response.code()));
                if(response.code()>200){
                    utils.showMessage(getString(R.string.rest_error),tvStatus,true);
                    return;
                }
                movies = response.body().getResults();
                //Log.e("num of movies ", "total: " + movies.size());

                adapter = new MoviesGridAdapter(getApplicationContext(), movies);
                adapter.setClickListener(MainActivity.this);
                rvMovies.setAdapter(adapter);
                tvStatus.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<MoviesResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e("ERROR", t.toString());
            }
        });


    }

    public  void  getPopularMovies(Context context) {

        if (!utils.isOnline()) {
            utils.showMessage(getString(R.string.no_connection), tvStatus, true);
            return;
        }

        utils.showMessage(getString(R.string.status_connecting), tvStatus, false);

        Call<MoviesResponse> call = theMoviesDbService.getPopularMovies(RetrofitClient.API_KEY);
        call.enqueue(new Callback<MoviesResponse>() {
            @Override
            public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                utils.showMessage(getString(R.string.status_laoding), tvStatus, false);
                Log.e("code",String.valueOf(response.code()));
                if(response.code()>200){
                    utils.showMessage(getString(R.string.rest_error),tvStatus,true);
                    return;
                }
                movies = response.body().getResults();
                //Log.e("num of movies ", "total: " + movies.size());

                adapter = new MoviesGridAdapter(getApplicationContext(), movies);
                adapter.setClickListener(MainActivity.this);
                rvMovies.setAdapter(adapter);
                tvStatus.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<MoviesResponse> call, Throwable t) {

                Log.e("ERROR", t.toString());
            }
        });
    }


    public void getFavoriteMovies(Context context) {
        utils.showMessage("Your Favorites", tvStatus, false);
        //getting movies from ic_favorites content provider
         movies=providerAPI.getFavoriteMovies();
        adapter = new MoviesGridAdapter(getApplicationContext(), movies);
        adapter.setClickListener(MainActivity.this);
        adapter.notifyDataSetChanged();
        rvMovies.setAdapter(adapter);

    }

    @Override
    public void onItemClick(View view, int position) {

       // Log.e("item clicked",movies.get(position).getTitle());
        Intent detailsIntent=new Intent(MainActivity.this,MovieDetailsActivity.class);
        detailsIntent.putExtra(MOVIE_ID,movies.get(position).getId());
        detailsIntent.putExtra(MOVIE_NAME,movies.get(position).getTitle());
        if(currentDisplay==CurrentDisplay.FAVORITES)
            detailsIntent.putExtra(FROM_FAVORITES,true);
        startActivity(detailsIntent);
        finish();

    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first



    }
    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first


    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current display state
        savedInstanceState.putInt(Current_DISPLAY, currentDisplay.ordinal());



        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_top_rated:
               //
                getTopRatedMovies(this);
               currentDisplay=CurrentDisplay.TOP_RATED;
                return true;
            case R.id.menu_popular:
               //
                getPopularMovies(this);
                currentDisplay=CurrentDisplay.MOST_POPULAR;
                return true;
            case R.id.menu_show_favorites:
                //
               getFavoriteMovies(this);
                currentDisplay=CurrentDisplay.FAVORITES;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
public enum CurrentDisplay{
        TOP_RATED,
        MOST_POPULAR,
        FAVORITES;

}

}
