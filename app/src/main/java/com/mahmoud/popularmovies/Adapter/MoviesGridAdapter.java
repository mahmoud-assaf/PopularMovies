package com.mahmoud.popularmovies.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mahmoud.popularmovies.Models.Movie;
import com.mahmoud.popularmovies.R;
import com.mahmoud.popularmovies.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import okhttp3.internal.Util;

/**
 * Created by mahmoud on 18/02/2018.
 */

public class MoviesGridAdapter extends RecyclerView.Adapter<MoviesGridAdapter.ViewHolder>  {
    private List<Movie> MoviesList = new ArrayList<Movie>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    Context context;
    int gridImageWidth;
    public static final String IMAGES_PATH="http://image.tmdb.org/t/p/w185";
    // data is passed into the constructor
   public MoviesGridAdapter(Context context, List<Movie> list) {
        this.mInflater = LayoutInflater.from(context);
        this.MoviesList = list;
        this.context=context;

    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the textview in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Movie movie = MoviesList.get(position);

        Picasso.with(context)

                .load(IMAGES_PATH+movie.getPosterPath())
                .placeholder(R.drawable.ic_loading)

                .into(holder.movieThumbnail);
      //  Log.e("image path ",IMAGES_PATH+movie.getPosterPath()); // just to know the exact size the site send
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return MoviesList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView movieThumbnail;

        ViewHolder(View itemView) {
            super(itemView);
            movieThumbnail = (ImageView) itemView.findViewById(R.id.movie_thumb);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    Movie getItem(int id) {
        return MoviesList.get(id);
    }

    // allows clicks events to be caught
   public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
