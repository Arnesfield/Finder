package com.arnesfield.school.finder;

import android.Manifest;
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
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.arnesfield.school.mytoolslib.SnackBarCreator;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private FloatingActionButton fab;
    private TextView tvSamp;
    private boolean isMapReady;
    private Location currLocation;
    private boolean wasPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // references
        fab = (FloatingActionButton) findViewById(R.id.main_fab);
        tvSamp = (TextView) findViewById(R.id.main_tv_samp);

        isMapReady = false;
        wasPressed = false;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                tvSamp.append("\n" + location.getLatitude() + " " + location.getLongitude());

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
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
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
                if (arePermissionsAllowed()) {
                    // SnackBarCreator.set("Requestion for location updates.");
                    // change provider
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 0, locationListener);
                }
                else {
                    SnackBarCreator.set("Could not request for location updates.");
                    SnackBarCreator.show(view);
                }

                // go to current location saved
                whenLocationChanges(null, true);
            }
        });

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

        // only go to current position when fab is pressed
        wasPressed = isPressed;

        // if param location is null, set to instance current location
        if (currLocation == null)
            currLocation = this.currLocation;

        // param location is passed to instance current location
        this.currLocation = currLocation;

        // if param location is still null
        // or if fab was not pressed
        if (currLocation == null || !wasPressed)
            return;

        wasPressed = false;

        // Add a marker
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // remove
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
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
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // set on ready
        isMapReady = true;
    }
}
