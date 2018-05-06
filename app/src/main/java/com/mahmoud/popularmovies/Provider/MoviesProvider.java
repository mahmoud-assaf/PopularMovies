package com.mahmoud.popularmovies.Provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by mahmoud on 19/02/2018.
 */

public class MoviesProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.mahmoud.popularmovies.Provider.MoviesProvider";
    static final String URL = "content://" + PROVIDER_NAME + "/movies";
    static final Uri CONTENT_URI = Uri.parse(URL);

    static final String MOVIE_ID = "_id";
    static final String MOVIE_TMDB_ID = "id";    //id in themoviesdb.com
    static final String MOVIE_TITLE = "title";
    static final String MOVIE_RELEASE_DATE = "release_date";
    static final String MOVIE_GENERS = "geners";
    static final String MOVIE_RATING = "rating";
    static final String MOVIE_OVERVIEW = "overview";
    static final String MOVIE_THUMB = "thumb";
    static final String MOVIE_BACKDROP = "backdrop";
    static final String MOVIE_TRAILERS = "trailers";
    static final String MOVIE_REVIEWS = "reviews";

    private static HashMap<String, String> MOVIES_PROJECTION_MAP;

    static final int BY_MOVIES = 1;
    static final int BY_MOVIE_ID = 2;

    static final UriMatcher uriMatcher;

    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "movies", BY_MOVIES);
        uriMatcher.addURI(PROVIDER_NAME, "movies/#", BY_MOVIE_ID);
    }

    // database specific constant declarations

    private SQLiteDatabase db;
    static final String DATABASE_NAME = "TheMoviesDB";
    static final String TABLE_NAME = "movies";
    static final int DATABASE_VERSION = 1;

    static final String CREATE_DB_TABLE =
            " CREATE TABLE " + TABLE_NAME +
                    " ("+MOVIE_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MOVIE_TMDB_ID+ " INTEGER NOT NULL , "+
                    MOVIE_TITLE+ " TEXT NOT NULL , "+
                    MOVIE_RELEASE_DATE+ " TEXT NOT NULL , "+
                    MOVIE_RATING+ " TEXT NOT NULL , "+
                    MOVIE_THUMB+ " TEXT NOT NULL , "+
                    MOVIE_TRAILERS+ " TEXT , "+
                    MOVIE_GENERS+ " TEXT ,"+
                    MOVIE_OVERVIEW+ " TEXT ,"+
                    MOVIE_BACKDROP+ " TEXT , "+
                    MOVIE_REVIEWS+ " TEXT)";


    // helper class that actually creates and manages the provider's underlying data repository.

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_DB_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  TABLE_NAME);
            onCreate(db);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        DatabaseHelper dbHelper = new DatabaseHelper(context);

        // create a write able database which will trigger its creation if it doesn't already exist.

        db = dbHelper.getWritableDatabase();
        return (db == null)? false:true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
           // Log.e("values off saved movie",values.toString());        // add a new movie record
        long rowID = db.insert(TABLE_NAME, "", values);

        // if record is added successfully
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a movie " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection,String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        switch (uriMatcher.match(uri)) {
            case BY_MOVIES:
                qb.setProjectionMap(MOVIES_PROJECTION_MAP);
                break;

            case BY_MOVIE_ID:
                qb.appendWhere(MOVIE_TMDB_ID + "=" + uri.getPathSegments().get(1));
                break;

            default:
        }

        if (sortOrder == null || sortOrder == ""){
            // sort on movie id
            sortOrder = MOVIE_ID;
        }

        Cursor c = qb.query(db,   projection, selection,
                selectionArgs,null, null, sortOrder);
        // register to watch a content URI for changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case BY_MOVIES:
                count = db.delete(TABLE_NAME, selection, selectionArgs);
                break;

            case BY_MOVIE_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(TABLE_NAME, MOVIE_TMDB_ID +  " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)) {
            case BY_MOVIES:
                count = db.update(TABLE_NAME, values, selection, selectionArgs);
                break;

            case BY_MOVIE_ID:
                count = db.update(TABLE_NAME, values, MOVIE_ID + " = " + uri.getPathSegments().get(1) +
                        (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            // get all movies
            case BY_MOVIES:
                return "vnd.android.cursor.dir/vnd.com.mahmoud.popularmovies.Provider.MoviesProvider.movies";
            //  get a single movie
            case BY_MOVIE_ID:
                return "vnd.android.cursor.item/vnd.com.mahmoud.popularmovies.Provider.MoviesProvider.movies";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
}
