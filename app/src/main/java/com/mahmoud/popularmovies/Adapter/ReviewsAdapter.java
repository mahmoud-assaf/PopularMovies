package com.mahmoud.popularmovies.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mahmoud.popularmovies.Models.Review;
import com.mahmoud.popularmovies.R;

import java.util.List;

/**
 * Created by mahmoud on 18/02/2018.
 */

public class ReviewsAdapter extends ArrayAdapter<Review> {
    public ReviewsAdapter(Context context, List<Review> reviews) {
        super(context, 0, reviews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Review review = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.review_row, parent, false);
        }
        // Lookup view for data population
        TextView tvAuthor = (TextView) convertView.findViewById(R.id.review_author_name);
        TextView tvContent = (TextView) convertView.findViewById(R.id.review_content);
        // Populate the data into the template view using the data object
        tvAuthor.setText(review.author);
        tvContent.setText(review.content);
        // Return the completed view to render on screen
        return convertView;
    }
}
