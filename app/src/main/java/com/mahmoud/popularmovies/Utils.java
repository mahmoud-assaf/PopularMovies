package com.mahmoud.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

/**
 * Created by mahmoud on 18/02/2018.
 */

public class Utils {
Context context;
    public  Utils(Context context){
        this.context=context;
    }

    public  void showMessage(String msg, TextView textView,boolean isError){
        textView.setVisibility(View.VISIBLE);
        if ((isError)){
            textView.setBackgroundColor(context.getResources().getColor(R.color.bgStatusError));
        }else {
            textView.setBackgroundColor(context.getResources().getColor(R.color.bgStatusNormal));
        }
        textView.setText(msg);
    }



    //using @gar accepted answer at https://stackoverflow.com/questions/1560788/how-to-check-internet-access-on-android-inetaddress-never-times-out
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

//calculating num of columns for grid
// https://stackoverflow.com/questions/33575731/gridlayoutmanager-how-to-auto-fit-columns
    public  int calculateNoOfColumns() {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / 160);
        return noOfColumns;
    }



}
