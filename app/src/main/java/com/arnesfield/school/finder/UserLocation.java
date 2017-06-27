package com.arnesfield.school.finder;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by User on 06/26.
 */

public final class UserLocation {
    // list of user locations
    private static final ArrayList<UserLocation> LIST_OF_USER_LOCATIONS = new ArrayList<>();
    public static final ArrayList<String> LIST_OF_NOTIFS = new ArrayList<>();

    public static ArrayList<UserLocation> getCopyOfList() {
        return new ArrayList<>(LIST_OF_USER_LOCATIONS);
    }

    public static String createStringOfIdsFromList() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (UserLocation u : LIST_OF_USER_LOCATIONS)
            arrayList.add(u.getId());
        return TextUtils.join(",", arrayList);
    }

    // clear list
    public static void clearList() {
        LIST_OF_USER_LOCATIONS.clear();
    }

    public static UserLocation addLocation(String id, String username, double latitude, double longitude, String dateTime) {
        UserLocation userLocation = new UserLocation(id, username, latitude, longitude, dateTime);
        LIST_OF_USER_LOCATIONS.add(userLocation);
        return userLocation;
    }

    public static boolean isListEmpty() {
        return LIST_OF_USER_LOCATIONS.isEmpty();
    }

    // instance
    private String id, username, dateTime;
    private double latitude, longitude;

    // constructor
    private UserLocation(String id, String username, double latitude, double longitude, String dateTime) {
        this.id = id;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dateTime = dateTime;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDateTime() {
        return dateTime;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }
}
