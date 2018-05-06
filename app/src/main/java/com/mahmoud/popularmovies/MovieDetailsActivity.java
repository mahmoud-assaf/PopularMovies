package com.mahmoud.popularmovies;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mahmoud.popularmovies.Adapter.MoviesGridAdapter;
import com.mahmoud.popularmovies.Adapter.ReviewsAdapter;
import com.mahmoud.popularmovies.Fragments.ReviewsFragment;
import com.mahmoud.popularmovies.Fragments.TrailersFragment;
import com.mahmoud.popularmovies.Models.FavoriteMovie;
import com.mahmoud.popularmovies.Models.Genre;
import com.mahmoud.popularmovies.Models.Movie;
import com.mahmoud.popularmovies.Models.MovieDetailsResponse;
import com.mahmoud.popularmovies.Models.MovieVideo;
import com.mahmoud.popularmovies.Models.MoviesResponse;
import com.mahmoud.popularmovies.Models.Review;
import com.mahmoud.popularmovies.Models.ReviewsResponse;
import com.mahmoud.popularmovies.Models.VideosRresponse;
import com.mahmoud.popularmovies.Provider.ProviderAPI;
import com.mahmoud.popularmovies.Rest.RetrofitApiInterface;
import com.mahmoud.popularmovies.Rest.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v7.app.AlertDialog.*;

public class MovieDetailsActivity extends AppCompatActivity {

    static final String MOVIE_ID = "movieID";
    private static final String MOVIE_NAME ="movieName" ;
    static final String FROM_FAVORITES ="fromFavorites";

    public static final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final java.lang.String SHOW_FROM_FAVORITES ="show_from_favorites" ;

    final String FRAGMENT_TRAILERS_TAG="fragment_trailers";
    final String FRAGMENT_REVIEWS_TAG="fragment_reviews";

    public ProviderAPI providerAPI;
    int movieID;
    String movieNAME;
    String posterPath;
    String backdropPath;
    List<Genre> genres;
    List<Review> reviews;
    public  boolean finishing=false;
    boolean showFromFavorites=false;
    boolean thisMovieAddedToFavorites=false;
    public boolean isCurrentTrailer=true;
   public int detailsLoaded,trailersLoaded,reviewsLoaded;


    RetrofitApiInterface theMoviesDbService;
    Utils utils;
    private Menu menu;
    @BindView(R.id.movie_poster)
    ImageView movieThumb;

    @BindView(R.id.movie_title)
    TextView movieTitle;

    @BindView(R.id.movie_release_date)
    TextView movieReleaseDate;

    @BindView(R.id.movie_genres)
    TextView movieGeners;

    @BindView(R.id.movie_rating)
    TextView movieRating;

    @BindView(R.id.movie_overview)
    TextView movieOverview;



    @OnClick(R.id.button_showTrailers)
    public void onShowTrailersClick() {
        Log.e("click trailers","click");
        if(isCurrentTrailer) return;

        android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_REVIEWS_TAG);
        if(fragment != null) {
            Log.e("click trailers","reiews fragment exist");
            //detach reviews
            android.support.v4.app.Fragment fragment3 = getSupportFragmentManager().findFragmentByTag(FRAGMENT_REVIEWS_TAG);
            android.support.v4.app.FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
            fragTransaction.detach(fragment3);
            fragTransaction.commit();
            android.support.v4.app.Fragment fragment4 = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TRAILERS_TAG);
            android.support.v4.app.FragmentTransaction fragTransaction3 = getSupportFragmentManager().beginTransaction();
            fragTransaction3.attach(fragment4);

            fragTransaction3.commit();

        }
        isCurrentTrailer=true;
    }

    @OnClick(R.id.button_showReviews)
    public void onShowReviewsClick() {
        if (!isCurrentTrailer) return;

        Log.e("click reviews","click");
        android.support.v4.app.Fragment fragment = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TRAILERS_TAG);
        if (fragment != null) {
            Log.e("click reviews","trailers fragment exist");
            android.support.v4.app.FragmentTransaction fragTransaction = getSupportFragmentManager().beginTransaction();
            fragTransaction.detach(fragment);

            fragTransaction.commit();

            android.support.v4.app.Fragment fragment2 = getSupportFragmentManager().findFragmentByTag(FRAGMENT_REVIEWS_TAG);
            if (fragment2 != null) {
                Log.e("click reviews","reviwes fragment exist");
                android.support.v4.app.FragmentTransaction fragTransaction2 = getSupportFragmentManager().beginTransaction();
                fragTransaction2.attach(fragment2);

                fragTransaction2.commit();
            }

        }
        isCurrentTrailer = false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            return;

        }
        //r we coming after recreate
        if (savedInstanceState != null) {
            // Restore value of members from saved state
           showFromFavorites=savedInstanceState.getBoolean(SHOW_FROM_FAVORITES);
           movieID=savedInstanceState.getInt(MOVIE_ID);
           movieNAME=savedInstanceState.getString(MOVIE_NAME);


        }
        detailsLoaded=trailersLoaded=reviewsLoaded=0;
        movieID=intent.getIntExtra(MOVIE_ID, 1);
        movieNAME=intent.getStringExtra(MOVIE_NAME);
        showFromFavorites=intent.getBooleanExtra(FROM_FAVORITES,false);

        utils=new Utils(this);


        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(movieNAME);
        theMoviesDbService = RetrofitClient.getClient().create(RetrofitApiInterface.class);


        if(showFromFavorites)
            getMovieDetailsOffline();
        else
            getMovieDetailsOnline();


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current display state
        savedInstanceState.putBoolean(SHOW_FROM_FAVORITES, showFromFavorites);
        savedInstanceState.putInt(MOVIE_ID,movieID);
        savedInstanceState.putString(MOVIE_NAME,movieNAME);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    private void getMovieDetailsOffline() {
        providerAPI=ProviderAPI.getInstance(this);
        FavoriteMovie favoriteMovie=providerAPI.getMovieFromFavorites(movieID);
        Picasso.with(MovieDetailsActivity.this)
                .load(RetrofitClient.BASE_IMAGES_URL+favoriteMovie.movie.getBackdropPath())
                .placeholder(R.drawable.ic_loading)
                .into(movieThumb);
        movieTitle.setText(favoriteMovie.movie.getTitle());
        movieReleaseDate.setText(favoriteMovie.movie.getReleaseDate());
        movieRating.setText(String.valueOf(favoriteMovie.movie.getVoteAverage()));
        movieOverview.setText(favoriteMovie.movie.getOverview());

        movieGeners.setText(favoriteMovie.geners);

        ReviewsFragment reviewsFragment=ReviewsFragment.newInstance(movieID,providerAPI.reviewsToString(favoriteMovie.reviews));
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.trailers_reviews_container, reviewsFragment,FRAGMENT_REVIEWS_TAG).commit();

        //display trailers fragment first
        TrailersFragment trailersFragment=TrailersFragment.newInstance(movieID,providerAPI.trailersToString(favoriteMovie.trailers));
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.trailers_reviews_container, trailersFragment,FRAGMENT_TRAILERS_TAG).commit();
        isCurrentTrailer=true;
        detailsLoaded=1;


    }

    public  void getMovieDetailsOnline(){
        if(!utils.isOnline()){
            Toast.makeText(this,"Network connection not available",Toast.LENGTH_LONG).show();
            return;
        }
        Call<MovieDetailsResponse> detailsCall = theMoviesDbService.getMovieDetails(movieID,RetrofitClient.API_KEY);
        detailsCall.enqueue(new Callback<MovieDetailsResponse>() {
            @Override
            public void onResponse(Call<MovieDetailsResponse>call, Response<MovieDetailsResponse> response) {
                if(finishing) return;

                if(response.code()>200){
                    Toast.makeText(MovieDetailsActivity.this,getString(R.string.rest_error),Toast.LENGTH_SHORT).show();

                    return;
                }

                Picasso.with(MovieDetailsActivity.this)
                        .load(RetrofitClient.BASE_IMAGES_URL+response.body().backdropPath)
                        .placeholder(R.drawable.ic_loading)
                        .into(movieThumb);
                movieTitle.setText(response.body().title);
                movieReleaseDate.setText(response.body().releaseDate);
                movieRating.setText(String.valueOf(response.body().voteAverage));
                movieOverview.setText(response.body().overview);
                posterPath=response.body().posterPath;
                backdropPath=response.body().backdropPath;
                genres=response.body().genres;
                String genersText="";
                for (int i = 0; i <genres.size() ; i++) {
                    genersText+=genres.get(i).name +", ";
                }
                movieGeners.setText(genersText);
                detailsLoaded=1;

            }

            @Override
            public void onFailure(Call<MovieDetailsResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e("ERROR", t.toString());
            }
        });

        ReviewsFragment reviewsFragment=ReviewsFragment.newInstance(movieID,"");
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.trailers_reviews_container, reviewsFragment,FRAGMENT_REVIEWS_TAG).commit();

        //display trailers fragment first
        TrailersFragment trailersFragment=TrailersFragment.newInstance(movieID,"");
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction().add(R.id.trailers_reviews_container, trailersFragment,FRAGMENT_TRAILERS_TAG).commit();
        isCurrentTrailer=true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.deatails_menu, menu);
        this.menu = menu;
        if(showFromFavorites)
            menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_remove_from_favorites));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_add_remove_favorite:

                providerAPI =ProviderAPI.getInstance(this);

                if(showFromFavorites){
                    //its delete operation
                    //show confirm dialog
                    Builder builder = new Builder(this);
                    builder.setMessage("Remove from your favorites").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }else{
                    //its add operation
                    if(thisMovieAddedToFavorites){
                        Toast.makeText(this,"Movie already added to your favorites",Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    if(detailsLoaded+trailersLoaded+reviewsLoaded!=3){
                        Toast.makeText(this,"Some details still laoding in background ...",Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    FavoriteMovie favoriteMovie=new FavoriteMovie();
                    Movie movie=new Movie();
                    movie.setTitle(movieTitle.getText().toString());
                    movie.setId(movieID);
                    movie.setVoteAverage(Double.parseDouble(movieRating.getText().toString()));
                    movie.setOverview(movieOverview.getText().toString());
                    movie.setPosterPath(posterPath);
                    movie.setBackdropPath(backdropPath);
                    movie.setReleaseDate(movieReleaseDate.getText().toString());


                    TrailersFragment fragmenttrls = (TrailersFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TRAILERS_TAG);
                    List<String> trailers=fragmenttrls.trailersPaths;

                    ReviewsFragment fragmentrevws = (ReviewsFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_REVIEWS_TAG);
                    List<Review> revws=fragmentrevws.reviews;

                    favoriteMovie.movie=movie;
                    favoriteMovie.geners=movieGeners.getText().toString();
                    favoriteMovie.reviews=revws;
                    favoriteMovie.trailers=trailers;

                    providerAPI.addFavoriteMovie(favoriteMovie);
                    menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_remove_from_favorites));
                    thisMovieAddedToFavorites=true;


                    }

                return true;
            case R.id.menu_share:
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Popular Movies");
                TrailersFragment fragment = (TrailersFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TRAILERS_TAG);
                String trailerPath=fragment.trailersPaths.get(0);
                String content="hey ,i recommend checking this great move , "+movieNAME + "\n" +YOUTUBE_URL+ trailerPath;
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, content);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    providerAPI.deleteFavoriteMovie(movieID);
                    AlertDialog alertDialog = new Builder(MovieDetailsActivity.this).create();

                    alertDialog.setMessage(getString(R.string.alert_dialog_title));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,"Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    startActivity(new Intent(MovieDetailsActivity.this, MainActivity.class).putExtra(MainActivity.Current_DISPLAY,MainActivity.currentDisplay));
                                    finish();
                                }
                            });
                    alertDialog.show();

                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishing=true;
        startActivity(new Intent(MovieDetailsActivity.this, MainActivity.class).putExtra(MainActivity.Current_DISPLAY,MainActivity.currentDisplay));
        finish();
    }
}
