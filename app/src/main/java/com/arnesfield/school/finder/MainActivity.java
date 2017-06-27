package com.arnesfield.school.finder;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;

import com.arnesfield.school.finder.tasks.CheckForNotifsTask;
import com.arnesfield.school.finder.tasks.LogoutUserTask;
import com.arnesfield.school.finder.tasks.SendNotifsTask;
import com.arnesfield.school.finder.tasks.UpdateLocationTask;
import com.arnesfield.school.finder.tasks.FetchLocationTask;
import com.arnesfield.school.mytoolslib.DialogCreator;
import com.arnesfield.school.mytoolslib.RequestStringCreator;
import com.arnesfield.school.mytoolslib.SnackBarCreator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, FetchLocationTask.OnPostExecuteListener,
        DialogCreator.DialogActionListener, UpdateLocationTask.OnUpdateLocationListener,
        LogoutUserTask.OnLogoutListener, SendNotifsTask.OnSendNotifsListener,
        CheckForNotifsTask.OnCheckForNotifsListener {

    private static int currDistanceVal = 0;
    private static int currTempDistanceVal = 0;
    private static final String DISTANCE_PREF = "distance_pref";
    private static final String DISTANCE_ID = "distance_id";

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FloatingActionButton fabGps;
    private FloatingActionButton fabSend;
    private boolean isMapReady;
    private Location currLocation;
    private boolean wasPressed;
    private View rootView;
    private int uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get extras
        uid = getIntent().getIntExtra("uid", -1);

        // handle if no uid
        if (uid == -1) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        boolean showMessage = getIntent().getBooleanExtra("show_message", false);

        // references
        rootView = findViewById(R.id.main_root_view);
        if (showMessage) {
            SnackBarCreator.set(R.string.snackbar_success_login);
            SnackBarCreator.show(rootView);

            // remove message
            getIntent().putExtra("show_message", false);
        }

        // get curr distance from shared pref
        SharedPreferences sharedPreferences = getSharedPreferences(DISTANCE_PREF, MODE_PRIVATE);
        int distance = sharedPreferences.getInt(DISTANCE_ID, 2);

        // init
        if (currDistanceVal <= 0) {
            currDistanceVal = distance;
        }

        fabGps = (FloatingActionButton) findViewById(R.id.main_fab_gps);
        fabSend = (FloatingActionButton) findViewById(R.id.main_fab_send);

        isMapReady = false;
        wasPressed = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                fabGps.setImageResource(R.drawable.ic_my_location_black_24dp);
                // when location updates
                whenLocationChanges(location, wasPressed);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                fabGps.setImageResource(R.drawable.ic_location_disabled_black_24dp);
                DialogCreator.create(MainActivity.this, "request")
                        .setTitle(R.string.dialog_request_title)
                        .setMessage(R.string.dialog_request_msg)
                        .setPositiveButton(R.string.dialog_request_positive)
                        .show();
            }
        };

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // check if has permission
        if (!checkForRuntimePermissions())
            whenPermissionIsSet();
    }

    private void triggerNotification(String username) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_location_on_white_24dp)
                        .setContentTitle(username + " " + getResources().getString(R.string.notification_title))
                        .setContentText(getResources().getString(R.string.notification_msg));

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, LoginActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(LoginActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void whenPermissionIsSet() {
        // enable buttons and functionality
        fabGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoCurrentLocation();
            }
        });

        fabSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send notifications
                DialogCreator.create(MainActivity.this, "notify")
                        .setTitle(R.string.dialog_notify_title)
                        .setMessage(R.string.dialog_notify_msg)
                        .setPositiveButton(R.string.dialog_notify_positive)
                        .setNegativeButton(R.string.dialog_notify_negative)
                        .show();
            }
        });
    }

    private void gotoCurrentLocation() {
        if (arePermissionsAllowed()) {
            // SnackBarCreator.set("Requestion for location updates.");
            // change provider
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
        }
        else {
            SnackBarCreator.set(R.string.snackbar_permission_denied);
            SnackBarCreator.show(rootView);
        }

        // go to current location saved
        whenLocationChanges(null, true);
    }

    private boolean checkForRuntimePermissions() {
        if (
            Build.VERSION.SDK_INT >= 23 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1);
            return true;
        }

        return false;
    }

    private boolean arePermissionsAllowed() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void whenLocationChanges(Location currLocation, boolean isPressed) {
        if (!isMapReady)
            return;

        // add current location
        UpdateLocationTask.execute(this);

        // only go to current position when fabGps is pressed
        wasPressed = isPressed;

        // if param location is null, set to instance current location
        if (currLocation == null)
            currLocation = this.currLocation;

        // param location is passed to instance current location
        this.currLocation = currLocation;

        // remove all markers first
        mMap.clear();

        // add markers based from list of users
        if (!UserLocation.isListEmpty()) {

            for (UserLocation u : UserLocation.getCopyOfList()) {

                if (currLocation != null) {
                    Location userLoc = new Location(u.getUsername());

                    userLoc.setLatitude(u.getLatitude());
                    userLoc.setLongitude(u.getLongitude());

                    float distanceMeters = currLocation.distanceTo(userLoc);
                    int targetKilometers = currDistanceVal;

                    // meters to km
                    float distance = distanceMeters / 1000;

                    // if distance is greater than target km
                    if (distance > targetKilometers) {
                        // remove user
                        UserLocation.removeLocation(u);
                        continue;
                    }
                }

                // add all markers
                mMap.addMarker(new MarkerOptions()
                        .title(u.getUsername())
                        .position(u.getLatLng())
                );
            }
        }

        // notify here
        if (!UserLocation.LIST_OF_NOTIFS.isEmpty()) {
            for (String username : UserLocation.LIST_OF_NOTIFS)
                triggerNotification(username);
        }

        // clear notifs
        UserLocation.LIST_OF_NOTIFS.clear();


        // fetch locations
        FetchLocationTask.execute(this);

        // check for notifs
        CheckForNotifsTask.execute(this);


        // DO ONLY ON CLICK FAB
        // if param location is still null
        // or if fabGps was not pressed
        if (currLocation == null || !wasPressed)
            return;

        wasPressed = false;

        // Add my current location
        double latitude = currLocation.getLatitude();
        double longitude = currLocation.getLongitude();

        LatLng coordinates = new LatLng(latitude, longitude);

        // enable
        if (arePermissionsAllowed()) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        // add marker to current position
        // mMap.addMarker(new MarkerOptions().position(coordinates).title("Current Location"));
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));

        // animate to position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(coordinates)
                .zoom(17)
                // .bearing(0)
                // .tilt(30)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void triggerLogout() {
        DialogCreator.create(this, "logout")
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_msg)
                .setPositiveButton(R.string.dialog_logout_positive)
                .setNegativeButton(R.string.dialog_logout_negative)
                .show();
    }

    private int getZoomLevel(int radius) {
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(currLocation.getLatitude(), currLocation.getLongitude()))
                .radius(radius * 1000);

        Circle circle = mMap.addCircle(circleOptions);
        mMap.clear();

        if (circle != null){
            double rad = circle.getRadius();
            double scale = rad / 500;
            return (int) (16 - Math.log(scale) / Math.log(2));
        }

        return 17;
    }

    /*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            triggerLogout();
        }
        return super.onKeyDown(keyCode, event);
    }
    */

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // remove
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

        // logout task or offline
        LogoutUserTask.execute(this, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // logout task or offline
        LogoutUserTask.execute(this, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // logout task or offline
        LogoutUserTask.execute(this, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                whenPermissionIsSet();
            }
            else {
                checkForRuntimePermissions();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_logout:
                triggerLogout();
                return true;
            case R.id.action_menu_set_distance:
                DialogCreator.create(this, "distance")
                        .setTitle(R.string.dialog_distance_title)
                        .setView(R.layout.dialog_picker)
                        .setPositiveButton(R.string.dialog_distance_positive)
                        .setNegativeButton(R.string.dialog_distance_negative)
                        .show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // set on ready
        isMapReady = true;

        gotoCurrentLocation();
    }

    // fetch location listener
    @Override
    public void parseJSONString(String jsonString) {
        // clears list to avoid duplicates
        UserLocation.clearList();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("locations");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);

                String id = jsonObject.getString("id");
                String username = jsonObject.getString("username");
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");

                String dateTime = jsonObject.getString("date_time");

                // add user locations to list
                UserLocation.addLocation(id, username, latitude, longitude, dateTime);
            }
        } catch (JSONException ignored) {}
    }

    @Override
    public String createUserIdPostString(ContentValues contentValues) throws UnsupportedEncodingException {
        contentValues.put("uid", uid);
        contentValues.put("fetch", true);
        return RequestStringCreator.create(contentValues);
    }

    // dialog action listener
    @Override
    public void onClickPositiveButton(String actionId) {
        Intent intent;
        switch (actionId) {
            case "logout":
                LogoutUserTask.execute(this, true);
                break;

            case "request":
                intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                break;

            case "notify":
                SendNotifsTask.execute(this);
                break;

            case "distance":
                currDistanceVal = currTempDistanceVal;

                // save val to shared pref
                SharedPreferences sharedPreferences = getSharedPreferences(DISTANCE_PREF, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(DISTANCE_ID, currDistanceVal);
                editor.apply();

                // zoom out or in
                if (isMapReady) {
                    try {
                        // Add my current location
                        double latitude = currLocation.getLatitude();
                        double longitude = currLocation.getLongitude();

                        LatLng coordinates = new LatLng(latitude, longitude);
                        // animate to position
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(coordinates)
                                .zoom(getZoomLevel(currDistanceVal))
                                // .bearing(0)
                                // .tilt(30)
                                .build();

                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    } catch (Exception e) {}
                }

                SnackBarCreator.set("Fetching users within " + currDistanceVal + "km radius.");
                SnackBarCreator.show(rootView, true);
                break;
        }
    }

    @Override
    public void onClickNegativeButton(String actionId) {
        switch (actionId) {
            case "distance":
                currTempDistanceVal = currDistanceVal;
                break;
        }
    }

    @Override
    public void onClickNeutralButton(String actionId) {

    }

    @Override
    public void onClickMultiChoiceItem(String actionId, int which, boolean isChecked) {

    }

    @Override
    public void onCreateDialogView(String actionId, View view) {
        NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.dialog_distance_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(30);

        currTempDistanceVal = currDistanceVal;
        numberPicker.setValue(currDistanceVal);

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                currTempDistanceVal = newVal;
            }
        });
    }

    // add location task listener
    @Override
    public String createLocationPostString(ContentValues contentValues) throws UnsupportedEncodingException {
        if (currLocation == null)
            return null;

        double latitude = currLocation.getLatitude();
        double longitude = currLocation.getLongitude();

        contentValues.put("uid", uid);
        contentValues.put("location", true);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);

        return RequestStringCreator.create(contentValues);
    }

    // logout listener
    @Override
    public void onLoggedOut() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("logout", 1);
        startActivity(intent);
        finish();
    }

    @Override
    public String createLogoutPostString(ContentValues contentValues) throws UnsupportedEncodingException {
        contentValues.put("uid", uid);
        contentValues.put("logout", true);
        return RequestStringCreator.create(contentValues);
    }

    // notifs send
    @Override
    public void onSentNotif() {
        SnackBarCreator.set(R.string.snackbar_success_notifs_sent);
        SnackBarCreator.show(rootView, true);
    }

    @Override
    public String createNotifsPostString(ContentValues contentValues) throws UnsupportedEncodingException {
        contentValues.put("uid", uid);
        contentValues.put("notify", true);
        contentValues.put("send_to", UserLocation.createStringOfIdsFromList());
        return RequestStringCreator.create(contentValues);
    }

    // check for notifs
    @Override
    public void parseCheckNotifsJSONString(String jsonString) {
        // clear user list
        UserLocation.LIST_OF_NOTIFS.clear();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("notifs");

            for (int i = 0; i < jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);

                String id = jsonObject.getString("id");
                String username = jsonObject.getString("username");

                // send notif
                UserLocation.LIST_OF_NOTIFS.add(username);
            }
        } catch (JSONException ignored) {}
    }

    @Override
    public String createCheckNotifsPostString(ContentValues contentValues) throws UnsupportedEncodingException {
        contentValues.put("uid", uid);
        contentValues.put("check-notif", true);
        return RequestStringCreator.create(contentValues);
    }
}
