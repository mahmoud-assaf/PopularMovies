package com.mahmoud.popularmovies.Fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mahmoud.popularmovies.Adapter.ReviewsAdapter;
import com.mahmoud.popularmovies.Models.Review;
import com.mahmoud.popularmovies.Models.ReviewsResponse;
import com.mahmoud.popularmovies.MovieDetailsActivity;
import com.mahmoud.popularmovies.Provider.ProviderAPI;
import com.mahmoud.popularmovies.R;
import com.mahmoud.popularmovies.Rest.RetrofitApiInterface;
import com.mahmoud.popularmovies.Rest.RetrofitClient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReviewsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReviewsFragment extends Fragment {

    private static final String ARG_MOVIE_ID = "movie_id";
    private static final String ARG_MOVIE_REVIEWS = "movie_reviews";
    @BindView(R.id.movie_reviews_listview)
    ListView lvReviews;

    RetrofitApiInterface theMoviesDbService;
    public ProviderAPI providerAPI;
    ReviewsAdapter reviewsAdapter;
    public String reviewsString;
   public List<Review> reviews;
    private int movieId;
    private Unbinder unbinder;

    public ReviewsFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ReviewsFragment newInstance(int movieid,String reviews) {
        ReviewsFragment fragment = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieid);
        args.putString(ARG_MOVIE_REVIEWS,reviews);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movieId = getArguments().getInt(ARG_MOVIE_ID);
            reviewsString=getArguments().getString(ARG_MOVIE_REVIEWS);

            theMoviesDbService = RetrofitClient.getClient().create(RetrofitApiInterface.class);




        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_reviews, container, false);
        unbinder =  ButterKnife.bind(this, view);
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(reviewsString.isEmpty() && reviewsAdapter==null){
            getReviewsOnline();

        }
        if(!reviewsString.isEmpty()){
            providerAPI=ProviderAPI.getInstance(getActivity());
            reviews=providerAPI.stringToReviews(reviewsString);
            reviewsAdapter=new ReviewsAdapter(getActivity(),reviews);
            lvReviews.setAdapter(reviewsAdapter);
            ((MovieDetailsActivity)getActivity()).reviewsLoaded=1;

        }


         lvReviews.setAdapter(reviewsAdapter);



        //now using the trick in link http://findnerd.com/list/view/How-to-scroll-listview-inside-a-ScrollView-in-Android-/1248/
        //this is requered for listview to scroll inside a scrollview
        lvReviews.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }

        });


    }

    public  void  getReviewsOnline(){
        //getting reviews

        Call<ReviewsResponse> reviewsCall = theMoviesDbService.getMovieReviews(movieId, RetrofitClient.API_KEY);
        reviewsCall.enqueue(new Callback<ReviewsResponse>() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onResponse(Call<ReviewsResponse>call, Response<ReviewsResponse> response) {

                if(((MovieDetailsActivity)getActivity())==null) return;   //if back button pressed before loading just return ,it may occur when user quickly press back after activity starts ,context will be null
                if(response.code()>200){
                    Toast.makeText(getActivity(),getString(R.string.rest_error),Toast.LENGTH_SHORT).show();

                    return;
                }
                reviews=response.body().results;
                Log.e("num of reviews",String.valueOf(reviews.size()));
               reviewsAdapter=new ReviewsAdapter(getActivity(),reviews);
                lvReviews.setAdapter(reviewsAdapter);
                ((MovieDetailsActivity)getActivity()).reviewsLoaded=1;

            }

            @Override
            public void onFailure(Call<ReviewsResponse>call, Throwable t) {
                // Log error here since request failed
                Log.e("ERROR", t.toString());
            }
        });
    }
}
