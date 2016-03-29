package com.greenmiststudios.tweetsocial.tasks;

import android.location.Location;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

import com.greenmiststudios.tweetsocial.MainApplication;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.nlopez.smartlocation.SmartLocation;
import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Created by eckob on 3/28/2016.
 */
public class SearchTask extends AsyncTask<Query, Void, QueryResult> {

    private OnSearchResultListener listener;

    public SearchTask(OnSearchResultListener listener) {
        this.listener = listener;
    }

    private TwitterException exception;
    private Thread timeoutThread;

    @Override
    protected void onPreExecute() {
        timeoutThread = new Thread(() -> {
            try {
                this.get(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Log.d(SearchTask.class.getSimpleName(), "Search timed out");
                cancel(true);
                exception = new TwitterException("Timeout", e);
            }
        });
        timeoutThread.start();
        super.onPreExecute();
    }

    @Override
    protected QueryResult doInBackground(Query... params) {

        Twitter twitter = TwitterFactory.getSingleton();

        Query query = params[0];
        Location location = MainApplication.getInstance().getCurrentLocation();
        if (location != null) {
            query.setGeoCode(new GeoLocation(location.getLatitude(), location.getLongitude()), 5, Query.Unit.mi);
        }
        try {
            return twitter.search().search(query);
        } catch (TwitterException e) {
            exception = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(QueryResult queryResult) {
        timeoutThread.interrupt();
        if (listener != null) {
            if (queryResult != null) {
                listener.onSearchResult(queryResult);
            } else {
                listener.onError(exception);
            }
        }
        super.onPostExecute(queryResult);
    }

    @Override
    protected void onCancelled(QueryResult result) {
        if (listener != null) {
            if (result != null) {
                listener.onSearchResult(result);
            } else {
                listener.onError(exception);
            }
        }
        super.onCancelled(result);
    }

    @Override
    protected void onCancelled() {
        if (listener != null) {
            listener.onError(exception);
        }
        super.onCancelled();
    }

    public interface OnSearchResultListener {
        void onSearchResult(QueryResult result);
        void onError(TwitterException e);
    }

}
