package com.greenmiststudios.tweetsocial.helper;

import android.text.format.DateUtils;

import java.util.Calendar;

/**
 * Created by eckob on 3/28/2016.
 */
public class TimeFormatter {

    public static String getTimeAgo(long timestamp) {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now - timestamp);
        return DateUtils.getRelativeTimeSpanString(timestamp,  System.currentTimeMillis(), 0).toString();
    }

}
