package com.brainfluence.pickmeuprebuild.ui.Home;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.brainfluence.pickmeuprebuild.DriverRegActivity;
import com.brainfluence.pickmeuprebuild.LoginActivity;
import com.brainfluence.pickmeuprebuild.model.PassengerRequest;
import com.brainfluence.pickmeuprebuild.model.User;
import com.brainfluence.pickmeuprebuild.services.GetNearbyPlacesData;
import com.brainfluence.pickmeuprebuild.R;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;

import es.dmoral.toasty.Toasty;

import static com.brainfluence.pickmeuprebuild.LoginActivity.ACCOUNT_TYPE;
import static com.brainfluence.pickmeuprebuild.LoginActivity.SHARED_PREFS;
import static com.brainfluence.pickmeuprebuild.LoginActivity.UID;
import static com.brainfluence.pickmeuprebuild.LoginActivity.USER_NAME;
import static com.brainfluence.pickmeuprebuild.services.GetNearbyPlacesData.nearest;


public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, RoutingListener {

    private HomeViewModel homeViewModel;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest mLocationRequest, locationRequest;
    private LocationCallback locationCallback;
    private SupportMapFragment mapFragment;
    private LocationManager locationManager;
    private Boolean gpsEnabled;
    private int PROXIMITY_RADIUS = 10000;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.purple_500};
    private SharedPreferences sharedPref;
    private String accountType;
    private GoogleApiClient mGooleApiClient;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference, databaseReferenceGetDriverInfo,databaseReferenceDriverWrite;
    private String userId,phoneNumber,name;
    private LatLng pickupLocation,driverCurrentLocation,passengerCurrentLocation,passengerDestinationLocation;
    private int radius, limit;
    private Boolean driverFound;
    private String driverId;
    private ValueEventListener listener,listener1;
    private Marker driverMarker;
    private Button callAmbulance;
    private ProgressDialog progressDialog,progressDialog1;
    private Boolean buttonPressed,gotRequest;

    @Override
    public void onDestroy() {
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        super.onDestroy();

        if (accountType.equals("driver")) {
            GeoFire geoFire = new GeoFire(databaseReference);
            geoFire.removeLocation(userId);
            if(listener1!=null)
            {
                databaseReferenceDriverWrite.removeEventListener(listener1);
            }

        } else {
            if(listener!=null)
            {
                databaseReferenceGetDriverInfo.removeEventListener(listener);
            }

        }
    }

    @Override
    public void onStop() {

        super.onStop();
        if (accountType.equals("driver")) {
            GeoFire geoFire = new GeoFire(databaseReference);
            geoFire.removeLocation(userId);
        }
        mGooleApiClient.disconnect();
        // Disconnect GoogleApiClient when stopping Activity

    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (accountType.equals("driver")) {
                    GeoFire geoFire = new GeoFire(databaseReference);
                    geoFire.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                }

            }
        });

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        sharedPref = getActivity().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        accountType = sharedPref.getString(ACCOUNT_TYPE,"passenger");
        name = sharedPref.getString(USER_NAME,"user");
        userId = sharedPref.getString(UID,"123");
        callAmbulance = root.findViewById(R.id.callAmbulance);
        mGooleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGooleApiClient.connect();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("availableDrivers");
        databaseReferenceGetDriverInfo = firebaseDatabase.getReference("users");
        callAmbulance.setVisibility(View.INVISIBLE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        polylines = new ArrayList<>();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buttonPressed = false;
        gotRequest = false;
        driverMarker = null;
        callAmbulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radius=1;
                driverFound = false;
                limit= 10;

                buttonPressed = true;
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Please wait..."); // Setting Message
                progressDialog.setTitle("Finding Closest Driver"); // Setting Title
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                progressDialog.show(); // Display Progress Dialog
                progressDialog.setCancelable(false);


                new CountDownTimer(2000, 1000) {
                    @Override
                    public void onFinish() {

                        getNearestDriver();

                    }
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                }.start();
            }
        });

        return root;
    }




    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           requestPermissions( new String[] {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 500);
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);


        if(!gpsEnabled)
        {
            new AlertDialog.Builder(getContext())
                    .setMessage("Please Turn On Location Service")
                    .setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            getContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel",null)
                    .show();


        }

        if(gpsEnabled)
        {
                    fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                pickupLocation = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation,11));

                if(!accountType.equals("driver"))
                {
                    GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                    Object dataTransfer[] = new Object[4];
                    String hospital = "hospital";
                    String url = getUrl(location.getLatitude(), location.getLongitude(), hospital);
                    dataTransfer[0] = mMap;
                    dataTransfer[1] = url;
                    dataTransfer[2] = location.getLatitude();
                    dataTransfer[3] = location.getLongitude();
                    getNearbyPlacesData.execute(dataTransfer);



                    new CountDownTimer(2000, 1000) {
                        @Override
                        public void onFinish() {

//                            getDirection(latLng);
                            callAmbulance.setVisibility(View.VISIBLE);



                        }
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }
                    }.start();
                }
            }
        });
        }


        if(accountType.equals("driver"))
        {
            listenRequest();
        }



    }

    private void listenRequest() {

        databaseReferenceDriverWrite = firebaseDatabase.getReference("users").child(userId);
        databaseReferenceDriverWrite.child("currentPassenger").setValue("");

        listener1 = databaseReferenceDriverWrite.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String val = snapshot.child("currentPassenger").getValue().toString().trim();

                if(val.length()>0)
                {
                    gotRequest = true;
                    Toasty.success(getContext(),"Received User Request",Toasty.LENGTH_SHORT).show();
                    progressDialog1 = new ProgressDialog(getContext());
                    progressDialog1.setMessage("Finding best route"); // Setting Message
                    progressDialog1.setTitle("Received a passenger request"); // Setting Title
                    progressDialog1.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                    progressDialog1.show(); // Display Progress Dialog
                    progressDialog1.setCancelable(false);
                    DatabaseReference df = firebaseDatabase.getReference("passengerRequests").child(val);
                    df.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            PassengerRequest passengerRequest = snapshot.getValue(PassengerRequest.class);
                             driverCurrentLocation = pickupLocation;
                             passengerCurrentLocation = new LatLng(passengerRequest.getPickupLat(),passengerRequest.getPickupLon());
                             passengerDestinationLocation = new LatLng(passengerRequest.getHospitalLat(),passengerRequest.getHospitalLon());

                            new CountDownTimer(2000, 1000) {
                                @Override
                                public void onFinish() {
                                    MarkerOptions user = new MarkerOptions();
                                    user.position(passengerCurrentLocation);
                                    user.title(passengerRequest.getName()+" "+passengerRequest.getPhoneNumber());
                                    user.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                    MarkerOptions hospital = new MarkerOptions();
                                    hospital.position(passengerDestinationLocation);
                                    hospital.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                    mMap.addMarker(user);
                                    mMap.addMarker(hospital);
                                    getDirection(driverCurrentLocation,passengerCurrentLocation,passengerDestinationLocation);
                                }
                                @Override
                                public void onTick(long millisUntilFinished) {
                                }
                            }.start();


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getDirection(LatLng driverCurrentLocation, LatLng passengerCurrentLocation, LatLng passengerDestinationLocation) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(driverCurrentLocation,passengerCurrentLocation,passengerDestinationLocation)
                .key(getString(R.string.google_maps_key))
                .build();
        routing.execute();



    }

    private void getNearestDriver() {
        GeoFire geoFireGetDrivers = new GeoFire(databaseReference);


        GeoQuery geoQuery = geoFireGetDrivers.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);

        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {

                if(!driverFound)
                {

                 listener =   databaseReferenceGetDriverInfo.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            String val = snapshot.child("currentPassenger").getValue().toString().trim();
                            if (val.length() == 0) {
                                driverFound = true;
                                driverId = key;

                                buttonPressed = false;
                                User driver = snapshot.getValue(User.class);
                                MarkerOptions markerOptions = new MarkerOptions();

                                callAmbulance.setVisibility(View.INVISIBLE);
                                callAmbulance.setClickable(false);


                                DatabaseReference df = firebaseDatabase.getReference("passengerRequests").child(userId);
                                DatabaseReference df1 = firebaseDatabase.getReference("users").child(key);
                                DatabaseReference df2 = firebaseDatabase.getReference("users").child(userId);
                                df2.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        PassengerRequest passengerRequest = new PassengerRequest(userId, snapshot.child("name").getValue().toString().trim(),
                                                snapshot.child("phoneNumber").getValue().toString().trim(),
                                                pickupLocation.latitude, pickupLocation.longitude,
                                                nearest.latitude, nearest.longitude);
                                        df.setValue(passengerRequest);
                                        df1.child("currentPassenger").setValue(userId);

                                        DatabaseReference df3 = firebaseDatabase.getReference("availableDrivers").child(key).child("l");

                                        df3.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                List<Object> map = (List<Object>) snapshot.getValue();
                                                LatLng updatedLoc = new LatLng(Double.parseDouble(map.get(0).toString()),Double.parseDouble(map.get(1).toString()));
                                                float[] results = new float[10];
                                                Location.distanceBetween(pickupLocation.latitude,pickupLocation.longitude,updatedLoc.latitude,updatedLoc.longitude,results);

                                                if(results[0]<=100)
                                                {
                                                    Toasty.success(getContext(),"Driver arrived",Toasty.LENGTH_SHORT).show();
                                                }

                                                if(driverMarker!=null)
                                                {
                                                    driverMarker.setPosition(updatedLoc);
                                                }
                                                else {
                                                    markerOptions.position(updatedLoc);
                                                    markerOptions.title(driver.getName() + "  " + driver.getPhoneNumber());
                                                    int height = 100;
                                                    int width = 100;
                                                    BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.ambulance2);
                                                    Bitmap b = bitmapdraw.getBitmap();
                                                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                                    driverMarker = mMap.addMarker(markerOptions);
                                                }





                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });


                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                    buttonPressed = false;
                                }
                            }
                            else
                            {
                                databaseReferenceGetDriverInfo.removeEventListener(listener);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }

            @Override
            public void onKeyExited(String key) {

//                if(driverMarker!=null)
//                {
//                    driverMarker.remove();
//
//                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {




            }

            @Override
            public void onGeoQueryReady() {

                if(!driverFound && radius<=limit)
                {
                    radius++;
                    getNearestDriver();
                }
                else if(radius>limit && radius!=100 && buttonPressed){
                    radius = 100;

                    if(progressDialog!=null)
                    {
                        progressDialog.dismiss();
                        buttonPressed = false;

                                        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                        builder.setCancelable(true);
                                        builder.setIcon(R.drawable.ic_baseline_info_24);
                                        builder.setTitle("Not Found");
                                        builder.setMessage("No driver is available at this moment");
                                        builder.setInverseBackgroundForced(true);
                                        builder.setPositiveButton("Close",new DialogInterface.OnClickListener(){

                                            @Override
                                            public void onClick(DialogInterface dialog, int which){
                                                dialog.dismiss();
                                            }
                                        });

                                        AlertDialog alert=builder.create();
                                        alert.show();

                    }
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    private synchronized void buildGoogleApiClient() {

        mGooleApiClient.connect();
    }



    private String getUrl(double latitude , double longitude , String nearbyPlace)
    {

        StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlaceUrl.append("location="+latitude+","+longitude);
        googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
        googlePlaceUrl.append("&type="+nearbyPlace);
        googlePlaceUrl.append("&sensor=true");
        googlePlaceUrl.append("&key="+getString(R.string.google_maps_key));

        Log.d("MapsActivity", "url = "+googlePlaceUrl.toString());

        return googlePlaceUrl.toString();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if( requestCode == 500 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED)
        {
            mapFragment.getMapAsync(this::onMapReady);
        }
        else {

        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getContext(), "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int smallestIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <1; i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
        }

        if(progressDialog1!=null)
        {
            progressDialog1.dismiss();
        }

    }



    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng UsrLtLng = new LatLng(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude());
//                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UsrLtLng,11));
            }
        };
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGooleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if(accountType.equals("driver"))
        {
            GeoFire geoFire = new GeoFire(databaseReference);
            geoFire.removeLocation(userId);
        }

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {


        if(accountType.equals("driver"))
        {

            GeoFire geoFire = new GeoFire(databaseReference);
            geoFire.setLocation(userId,new GeoLocation(location.getLatitude(),location.getLongitude()));

            if(gotRequest)
            {
                getDirection(new LatLng(location.getLatitude(),location.getLongitude()),passengerCurrentLocation,passengerDestinationLocation);
            }

        }
    }


}