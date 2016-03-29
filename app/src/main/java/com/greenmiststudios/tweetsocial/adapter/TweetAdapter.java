package com.greenmiststudios.tweetsocial.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.greenmiststudios.tweetsocial.MainApplication;
import com.greenmiststudios.tweetsocial.R;
import com.greenmiststudios.tweetsocial.helper.TimeFormatter;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.Manifest;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.nlopez.smartlocation.SmartLocation;
import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Place;
import twitter4j.Status;

/**
 * Created by eckob on 3/28/2016.
 */
public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private List<Status> tweetList;

    private Activity activity;
    private ScaleDrawable scaleDrawable;
    private Drawable drawable;

    public TweetAdapter(Activity activity) {
        this.activity = activity;
        tweetList = new ArrayList<>();

        drawable = this.activity.getResources().getDrawable(R.drawable.reply_grey);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 0.6),
                (int) (drawable.getIntrinsicHeight() * 0.6));
    }

    public void setTweets(List<Status> tweets) {
        tweetList.clear();
        tweetList = tweets;
        notifyDataSetChanged();
    }

    public void addTweets(List<Status> tweets) {
        int initialCount = tweetList.size();
        tweetList.addAll(tweets);
        notifyItemRangeChanged(initialCount, tweets.size());
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.list_item_tweet, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Status tweet = tweetList.get(position);

        String profileURL = tweet.getUser().getOriginalProfileImageURL();

        Picasso.with(activity)
                .load(profileURL)
                .fit()
                .centerInside()
                .into(holder.profile);

        holder.username.setText(tweet.getUser().getName());
        holder.tweet.setText(tweet.getText());
        holder.timestamp.setText(TimeFormatter.getTimeAgo(tweet.getCreatedAt().getTime()));

        GeoLocation geoLocation = tweet.getGeoLocation();
        Place place = tweet.getPlace();

        holder.location.setVisibility(View.VISIBLE);
        if (place != null) {
            holder.location.setText(place.getFullName());
        } else if (geoLocation != null) {
            Location location = new Location("");
            location.setLatitude(geoLocation.getLatitude());
            location.setLongitude(geoLocation.getLongitude());

            MainApplication.getInstance().getSmartLocation().geocoding().reverse(location, (loc, addresses) -> {
                if (addresses.size() > 0) {
                    holder.location.setText(addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea());
                }
            });

        } else if (!TextUtils.isEmpty(tweet.getUser().getLocation().trim())){
            holder.location.setText(tweet.getUser().getLocation());
        } else {
            holder.location.setVisibility(View.GONE);
        }

        //Set scaled drawable for reply button
        scaleDrawable = new ScaleDrawable(drawable, 0, holder.reply.getMeasuredHeight(), holder.reply.getMeasuredHeight());
        holder.reply.setCompoundDrawablesRelative(null, null, scaleDrawable.getDrawable(), null);

        //Add all images attached to tweet
        holder.imageList.removeAllViews();
        for (MediaEntity entity : tweet.getMediaEntities()) {
            RoundedImageView imageView = new RoundedImageView(activity);
            float density = activity.getResources().getDisplayMetrics().density;
            imageView.setCornerRadius(4 * density);
            imageView.setPaddingRelative(4,4,4,4);
            holder.imageList.addView(imageView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (96 * density)));

            Picasso.with(activity)
                    .load(entity.getMediaURL())
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }
    }

    @Override
    public int getItemCount() {
        return tweetList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.profile)
        RoundedImageView profile;

        @Bind(R.id.username)
        TextView username;

        @Bind(R.id.timestamp)
        TextView timestamp;

        @Bind(R.id.imageList)
        LinearLayout imageList;

        @Bind(R.id.tweet)
        TextView tweet;

        @Bind(R.id.location)
        TextView location;

        @Bind(R.id.reply)
        Button reply;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        @OnClick(R.id.reply)
        void reply() {
            Status tweet = tweetList.get(getAdapterPosition());
            Snackbar.make(activity.findViewById(R.id.content),
                    "TweetID: " + tweet.getId() + " | User ID: " + tweet.getUser().getId() + " " + tweet.getText(),
                    Snackbar.LENGTH_LONG).show();
        }
    }

}
