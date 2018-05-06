package com.mahmoud.popularmovies.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by mahmoud on 18/02/2018.
 */

public class VideosRresponse {
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("results")
    @Expose
    public List<MovieVideo> results = null;
}
