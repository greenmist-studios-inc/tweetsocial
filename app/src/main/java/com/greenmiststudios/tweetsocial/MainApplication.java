package com.greenmiststudios.tweetsocial;

import android.app.Activity;
import android.app.Application;
import android.location.Location;
import android.util.Log;

import com.greenmiststudios.tweetsocial.tasks.AuthenticateTask;

import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.LocationProvider;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesWithFallbackProvider;

/**
 * Created by eckob on 3/28/2016.
 */
public class MainApplication extends Application {

    private boolean isAuthenticated;

    private static MainApplication instance;
    private Location currentLocation;

    private static SmartLocation smartLocation;
    private static SmartLocation.LocationControl control;
    private boolean locationUpdating = false;

    @Override
    public void onCreate() {
        instance = this;
        authenticate();

        smartLocation = new SmartLocation.Builder(this).logging(true).build();

        super.onCreate();
    }

    public void startLocationUpdates(Activity activity, boolean force) {
        if (locationUpdating && !force) return;
        if (force && control != null) control.stop();

        locationUpdating = true;

        control = smartLocation.location(new com.greenmiststudios.tweetsocial.helper.LocationProvider(activity));

        control.config(new LocationParams.Builder().setAccuracy(LocationAccuracy.HIGH).setDistance(150.0F).setInterval(30000L).build())
                .continuous()
                .start(location -> {
                    Log.d("location", location.toString());
                    currentLocation = location;
                });
    }

    @Override
    public void onTerminate() {
        locationUpdating = false;
        control.stop();
        super.onTerminate();
    }

    private void authenticate() {
        new AuthenticateTask(this, () -> isAuthenticated = true).execute();
    }

    public static MainApplication getInstance() {
        return instance;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public SmartLocation getSmartLocation() {
        return smartLocation;
    }

    public static SmartLocation.LocationControl getControl() {
        return control;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }
}
