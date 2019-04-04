package com.example.closest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.provider.ContactsContract;
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

import java.util.List;
import java.util.Map;

public class driverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;

    private Button logout;
    private String customerID = "";
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
        setContentView(R.layout.activity_driver_map);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkUserLcoationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(driverMapActivity.this,MainActivity.class);
                startActivity(intent);

                finish();
            }
        });
        getAssignedCustomer();



    }


    protected void getAssignedCustomer()
    {

        String DriverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
         DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Drivers").child(DriverId).child("customerRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    Toast.makeText(driverMapActivity.this, "The customer id is working fine", Toast.LENGTH_SHORT).show();
                    customerID = dataSnapshot.getValue().toString();
                    getAssignedCustomerPickupLocation();
                }

                else
                {

                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void getAssignedCustomerPickupLocation()
    {
        DatabaseReference assignedCustomerPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("DriversWorking").child(customerID).child("l");


        assignedCustomerPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat =0;
                    double locationLng =0;

                    if(map.get(0) != null)
                    {
                        locationLat = Double.parseDouble( map.get(0).toString());
                    }
                    if(map.get(1) != null)
                    {
                        locationLng = Double.parseDouble( map.get(1).toString());

                    }

                    LatLng driverLatlng = new LatLng(locationLat,locationLng);


                    mMap.addMarker(new MarkerOptions().position(driverLatlng).title("your Driver"));

                }

            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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


        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED );

        {
            if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
        }
       LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, this);



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext() != null) {


            mLastLocation = location;
            if (userCurrentLocationMarker != null) {
                userCurrentLocationMarker.remove();
            }

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Driver");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            userCurrentLocationMarker = mMap.addMarker(markerOptions);

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            String userId = "";


            try {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }


            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("DriversAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriversWorking");




            //DatabaseReference refCustomer = FirebaseDatabase.getInstance().getReference("CustomerRequests");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

           // GeoFire geoFireCustomer = new GeoFire(refCustomer);


            final String gg = customerID.toString();



              switch (customerID) {

                  case   "" :

                  geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
                      @Override
                      public void onComplete(String key, DatabaseError error) {

                      }
                  });
                  geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                      @Override
                      public void onComplete(String key, DatabaseError error) {

                      }
                  });

                  break;


                  default:


                  geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
                      @Override
                      public void onComplete(String key, DatabaseError error) {
                          Toast.makeText(driverMapActivity.this, "Driver removed from Drivers available", Toast.LENGTH_SHORT).show();

                      }
                  });
                  geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()), new GeoFire.CompletionListener() {
                      @Override
                      public void onComplete(String key, DatabaseError error) {

                          Toast.makeText(driverMapActivity.this, "Driver inserted to Drivers working", Toast.LENGTH_SHORT).show();

                      }
                  });

                  break;


              }















        }



    }

    @Override
    protected void onStop() {
        /* super.onStop();

        if(mGoogleApiClient != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refAvailable  = FirebaseDatabase.getInstance().getReference("DriversAvailable");
        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("DriversWorking");
        GeoFire geoFireAvailable = new GeoFire(refAvailable);
        GeoFire geoFireWorking = new GeoFire(refWorking);

        */
       /* geoFireAvailable.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }


        }); */

      /*  geoFireWorking.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {

            }
        });
       */


    }


}
