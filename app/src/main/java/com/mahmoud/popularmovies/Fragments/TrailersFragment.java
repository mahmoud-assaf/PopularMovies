package com.mahmoud.popularmovies.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mahmoud.popularmovies.MainActivity;
import com.mahmoud.popularmovies.Models.MovieVideo;
import com.mahmoud.popularmovies.Models.VideosRresponse;
import com.mahmoud.popularmovies.MovieDetailsActivity;
import com.mahmoud.popularmovies.Provider.ProviderAPI;
import com.mahmoud.popularmovies.R;
import com.mahmoud.popularmovies.Rest.RetrofitApiInterface;
import com.mahmoud.popularmovies.Rest.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TrailersFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_MOVIE_ID = "movie_id";
    private static final String ARG_MOVIE_TRAILERS = "movie_trailers";
    private Unbinder unbinder;
    @BindView(R.id.movie_trailers_list)
    ListView lvTrailers;

    RetrofitApiInterface theMoviesDbService;
    public ProviderAPI providerAPI;
    // TODO: Rename and change types of parameters
    private int movieId;
    public String trailersString;
    public  List<String> trailersPaths;
   public List<String> trailersTitles;
    ArrayAdapter<String> adapter;
    public TrailersFragment() {
        // Required empty public constructor
    }



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment TrailersFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrailersFragment newInstance(int movieid,String trailers) {
        TrailersFragment fragment = new TrailersFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MOVIE_ID, movieid);
        args.putString(ARG_MOVIE_TRAILERS,trailers);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            movieId = getArguments().getInt(ARG_MOVIE_ID);
            trailersString=getArguments().getString(ARG_MOVIE_TRAILERS);


            theMoviesDbService = RetrofitClient.getClient().create(RetrofitApiInterface.class);
        //    Log.e("trailers fragment","onCreate()");



        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      //  Log.e("trailers fragment","onCreateView()");
        // Inflate the layout for this fragment
         View view= inflater.inflate(R.layout.fragment_trailers, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
       // bar.setVisibility(View.VISIBLE);
       // Log.e("trailers fragment","onViewCreated()");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
      //  Log.e("trailers fragment","onActivityCreated()");


    }

    @Override
    public void onStart() {
        super.onStart();
       // Log.e("trailers fragment","onStart()");
    }

    @Override
    public void onResume() {
        super.onResume();



        if(trailersString.isEmpty() && adapter==null){
            getTrailersOnline();

        }
        if(!trailersString.isEmpty()){
            trailersPaths=new ArrayList<String>();
            trailersTitles=new ArrayList<String>();
            providerAPI=ProviderAPI.getInstance(getActivity());
            trailersPaths = providerAPI.stringToTrailers(trailersString);
            Log.e("trlrspaths  frgmnt ofli",String.valueOf(trailersPaths.size()));
            for (int i = 0; i < trailersPaths.size(); i++) {

                trailersTitles.add("Trailer " + String.valueOf(i + 1));
            }


            adapter=new ArrayAdapter<String>(getActivity(), R.layout.trailer_row, R.id.trailer_index,trailersTitles);
            lvTrailers.setAdapter(adapter);
            ((MovieDetailsActivity)getActivity()).trailersLoaded=1;
        }


        lvTrailers.setAdapter(adapter);

        lvTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String videoAddress=MovieDetailsActivity.YOUTUBE_URL+trailersPaths.get(position);
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(videoAddress));
                startActivity(i);
            }
        });


        //now using the trick in link http://findnerd.com/list/view/How-to-scroll-listview-inside-a-ScrollView-in-Android-/1248/
        //this is requered for listview to scroll inside a scrollview
        lvTrailers.setOnTouchListener(new ListView.OnTouchListener() {
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




    @Override
    public void onDestroyView() {
        super.onDestroyView();


        unbinder.unbind();
    }






    public  void getTrailersOnline(){
        Call<VideosRresponse> videosCall = theMoviesDbService.getMovieVideos(movieId,RetrofitClient.API_KEY);
        videosCall.enqueue(new Callback<VideosRresponse>() {
            @Override
            public void onResponse(Call<VideosRresponse>call, Response<VideosRresponse> response) {
                trailersPaths=new ArrayList<String>();
                trailersTitles=new ArrayList<String>();
                if(response.code()>200){
                    Toast.makeText(getActivity(),getString(R.string.rest_error),Toast.LENGTH_SHORT).show();

                    return;
                }
                List<MovieVideo> trailers=response.body().results;
                //  Log.e("num of trailers",String.valueOf(trailers.size()));

                for (int i = 0; i <trailers.size() ; i++) {
                    trailersPaths.add(trailers.get(i).key);
                    trailersTitles.add("Trailer "+String.valueOf(i+1));
                }
                if(((MovieDetailsActivity)getActivity())==null) return;   //if back button pressed before loading just return ,it may occur when user quickly press back after activity starts ,context will be null
                adapter=new ArrayAdapter<String>(getActivity(), R.layout.trailer_row, R.id.trailer_index,trailersTitles);
                lvTrailers.setAdapter(adapter);
                ((MovieDetailsActivity)getActivity()).trailersLoaded=1;


            }

            @Override
            public void onFailure(Call<VideosRresponse>call, Throwable t) {
                // Log error here since request failed
                Log.e("ERROR", t.toString());
            }
        });
    }

}
