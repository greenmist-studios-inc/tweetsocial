package com.greenmiststudios.tweetsocial.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.greenmiststudios.tweetsocial.R;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by eckob on 3/28/2016.
 */
public class AuthenticateTask extends AsyncTask<Void, Void, Boolean> {

    private Context context;
    private OnAuthenticateListener listener;

    public AuthenticateTask(Context context, OnAuthenticateListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Twitter twitter = TwitterFactory.getSingleton();
        AccessToken accessToken = new AccessToken(context.getString(R.string.oauth_token), context.getString(R.string.oauth_secret));

        twitter.setOAuthConsumer(context.getString(R.string.twitter_consumer_key), context.getString(R.string.twitter_consumer_sec));
        twitter.setOAuthAccessToken(accessToken);
        return true;
    }


    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if (listener != null) listener.onAuthentication();
        super.onPostExecute(aBoolean);
    }

    public interface OnAuthenticateListener {
        void onAuthentication();
    }
}
