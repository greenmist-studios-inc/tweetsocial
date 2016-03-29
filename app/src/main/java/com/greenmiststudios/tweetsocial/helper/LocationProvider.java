package com.greenmiststudios.tweetsocial.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationServices;

import io.nlopez.smartlocation.BuildConfig;
import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.location.config.LocationParams;
import io.nlopez.smartlocation.location.providers.LocationGooglePlayServicesProvider;
import io.nlopez.smartlocation.location.providers.LocationManagerProvider;
import io.nlopez.smartlocation.utils.GooglePlayServicesListener;
import io.nlopez.smartlocation.utils.Logger;

/**
 * Created by eckob on 3/29/2016.
 */
public class LocationProvider implements io.nlopez.smartlocation.location.LocationProvider, GooglePlayServicesListener {
    private Logger logger;
    private OnLocationUpdatedListener listener;
    private boolean shouldStart = false;
    private Context context;
    private LocationParams params;
    private boolean singleUpdate = false;
    private io.nlopez.smartlocation.location.LocationProvider provider;

    public LocationProvider(Activity context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return;
            }
        }

        if (apiAvailability.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            LocationGooglePlayServicesProvider provider = new LocationGooglePlayServicesProvider(this);
            provider.setCheckLocationSettings(true);
            this.provider = provider;
        } else {
            this.provider = new LocationManagerProvider();
        }


    }

    public void init(Context context, Logger logger) {
        this.logger = logger;
        this.context = context;
        if (provider == null) return;
        logger.d("Currently selected provider = " + this.provider.getClass().getSimpleName(), new Object[0]);
        this.provider.init(context, logger);
    }

    public void start(OnLocationUpdatedListener listener, LocationParams params, boolean singleUpdate) {
        this.shouldStart = true;
        this.listener = listener;
        this.params = params;
        this.singleUpdate = singleUpdate;
        if (provider == null) return;
        this.provider.start(listener, params, singleUpdate);
    }

    public void stop() {
        this.shouldStart = false;
        if (provider == null) return;
        this.provider.stop();
    }

    public Location getLastLocation() {
        return this.provider.getLastLocation();
    }

    public void onConnected(Bundle bundle) {
    }

    public void onConnectionSuspended(int i) {
        this.fallbackToLocationManager();
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        this.fallbackToLocationManager();
    }

    private void fallbackToLocationManager() {
        this.logger.d("FusedLocationProvider not working, falling back and using LocationManager", new Object[0]);
        this.provider = new LocationManagerProvider();
        this.provider.init(this.context, this.logger);
        if (this.shouldStart) {
            this.provider.start(this.listener, this.params, this.singleUpdate);
        }

    }
}
