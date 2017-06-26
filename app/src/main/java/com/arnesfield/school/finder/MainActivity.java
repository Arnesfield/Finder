package com.arnesfield.school.finder;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.arnesfield.school.finder.tasks.LogoutUserTask;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity implements
        OnMapReadyCallback, FetchLocationTask.OnPostExecuteListener,
        DialogCreator.DialogActionListener, UpdateLocationTask.OnUpdateLocationListener,
        LogoutUserTask.OnLogoutListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FloatingActionButton fab;
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

        fab = (FloatingActionButton) findViewById(R.id.main_fab);

        isMapReady = false;
        wasPressed = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
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

    private void whenPermissionIsSet() {
        // enable buttons and functionality
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoCurrentLocation();
            }
        });

    }

    private void gotoCurrentLocation() {
        if (arePermissionsAllowed()) {
            // SnackBarCreator.set("Requestion for location updates.");
            // change provider
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, locationListener);
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

        // only go to current position when fab is pressed
        wasPressed = isPressed;

        // if param location is null, set to instance current location
        if (currLocation == null)
            currLocation = this.currLocation;

        // param location is passed to instance current location
        this.currLocation = currLocation;

        // add markers based from list of users
        if (!UserLocation.isListEmpty()) {
            // remove all markers first
            mMap.clear();
            for (UserLocation u : UserLocation.getCopyOfList()) {
                // add all markers
                mMap.addMarker(new MarkerOptions()
                        .title(u.getUsername())
                        .position(u.getLatLng())
                );
            }
        }

        // if param location is still null
        // or if fab was not pressed
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

        // fetch locations
        FetchLocationTask.execute(this);
    }

    private void triggerLogout() {
        DialogCreator.create(this, "logout")
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_msg)
                .setPositiveButton(R.string.dialog_logout_positive)
                .setNegativeButton(R.string.dialog_logout_negative)
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            triggerLogout();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // remove
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

        // logout task
        LogoutUserTask.execute(this);
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
            case R.id.action_logout:
                triggerLogout();
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

                String username = jsonObject.getString("username");
                double latitude = jsonObject.getDouble("latitude");
                double longitude = jsonObject.getDouble("longitude");

                String dateTime = jsonObject.getString("date_time");

                // add user locations to list
                UserLocation.addLocation(username, latitude, longitude, dateTime);
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
                intent = new Intent(this, LoginActivity.class);
                intent.putExtra("logout", 1);
                startActivity(intent);
                finish();
                break;

            case "request":
                intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onClickNegativeButton(String actionId) {

    }

    @Override
    public void onClickNeutralButton(String actionId) {

    }

    @Override
    public void onClickMultiChoiceItem(String actionId, int which, boolean isChecked) {

    }

    @Override
    public void onCreateDialogView(String actionId, View view) {

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
    public String createLogoutPostString(ContentValues contentValues) throws UnsupportedEncodingException {
        contentValues.put("uid", uid);
        contentValues.put("logout", true);
        return RequestStringCreator.create(contentValues);
    }
}
