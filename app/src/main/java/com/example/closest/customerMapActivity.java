package com.example.closest;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class customerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    private Button logout,request;
    private LatLng pickupLocation;
    GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastLocation;
    private Marker userCurrentLocationMarker;
    private  static  final int requestUserLocationCode = 99;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkUserLcoationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        logout = findViewById(R.id.btnLogout);
        request = findViewById(R.id.btnRequest);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(customerMapActivity.this,MainActivity.class);
                startActivity(intent);

                finish();
            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("CustomerRequests");
                GeoFire geoFire = new GeoFire(reference);
                geoFire.setLocation(userId, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                    }
                });

                pickupLocation = new LatLng(lastLocation.getLatitude(),lastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup from here"));
                request.setText("Getting information");

                getClosestDriver();




            }
        });



    }

    private int radius = 1;
    private boolean driverFound = false;
    private String driverFoundID;

    private void getClosestDriver()
    {


        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriversAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);

        geoQuery.removeAllListeners();




        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {

                if(!driverFound)
                {
                    driverFound = true;
                    driverFoundID = dataSnapshot.getKey().toString();
                    if(driverFoundID.isEmpty())
                    {
                        Toast.makeText(customerMapActivity.this, "It is empty", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(customerMapActivity.this, "The Driver near you is" + driverFoundID, Toast.LENGTH_SHORT).show();
                    }

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(driverFoundID);
                    String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId", customerId);
                    driverRef.updateChildren(map);


                    getDriverLocation();
                    request.setText("Looking for driver location");
                }


            }

            private Marker mDriverMarker;

            private void getDriverLocation()
            {
                DatabaseReference driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversWorking").child(driverFoundID).child("l");
                driverLocationRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            List<Object> map = (List<Object>) dataSnapshot.getValue();
                            double locationLat =0;
                            double locationLng =0;
                            request.setText("Driver found");
                            if(map.get(0) != null)
                            {
                                locationLat = Double.parseDouble( map.get(0).toString());
                            }
                            if(map.get(1) != null)
                            {
                                locationLng = Double.parseDouble( map.get(1).toString());

                            }

                            LatLng driverLatlng = new LatLng(locationLat,locationLng);
                            if(mDriverMarker !=null)
                            {
                                mDriverMarker.remove();

                            }

                            Location loc1 = new Location("");
                            loc1.setLatitude(pickupLocation.latitude);
                            loc1.setLongitude(pickupLocation.longitude);


                            Location loc2 = new Location("");
                            loc2.setLatitude(driverLatlng.latitude);
                            loc2.setLongitude(driverLatlng.longitude);

                            float distance = loc1.distanceTo(loc2);

                            if (distance <=100)
                            {
                                request.setText("Driver has arrived");
                            }
                            else {
                                request.setText("Driver found " + String.valueOf(distance));
                            }




                            mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatlng).title("your Driver"));


                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }



            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound)
                {
                    radius++ ;
                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            buildGoogleMapApiClient();
        mMap.setMyLocationEnabled(true);


    }







    protected synchronized  void buildGoogleMapApiClient()
    {


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();


    }
    public boolean checkUserLcoationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestUserLocationCode);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestUserLocationCode);

            }
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {

            case requestUserLocationCode:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        if (mGoogleApiClient == null)
                        {
                            buildGoogleMapApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this, "Permission denied...", Toast.LENGTH_SHORT).show();
                }
                return;
        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates( mGoogleApiClient, mLocationRequest,this);
        }



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        if(userCurrentLocationMarker != null)
        {
            userCurrentLocationMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Driver");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        userCurrentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));





    }

    @Override
    protected void onStop() {
        super.onStop();


    }
}
