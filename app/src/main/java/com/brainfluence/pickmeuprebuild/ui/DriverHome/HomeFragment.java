package com.brainfluence.pickmeuprebuild.ui.DriverHome;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.brainfluence.pickmeuprebuild.HomeActivity;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


import java.util.ArrayList;
import java.util.List;

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
    private int PROXIMITY_RADIUS = 5000;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.purple_500};
    private SharedPreferences sharedPref;
    private String accountType;
    private GoogleApiClient mGooleApiClient;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference, databaseReferenceGetDriverInfo;
    private String userId;
    private LatLng pickupLocation;
    private int radius, limit;
    private Boolean driverFound;
    private String driverId;
    private ValueEventListener listener;
    private Marker driverMarker;

    @Override
    public void onDestroy() {
//        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        super.onDestroy();

        if (accountType.equals("driver")) {
            GeoFire geoFire = new GeoFire(databaseReference);
            geoFire.removeLocation(userId);
        } else {
            databaseReferenceGetDriverInfo.removeEventListener(listener);
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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
        userId = sharedPref.getString(UID,"123");
        Log.d("usertype", "onCreateView: "+accountType);
        mGooleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGooleApiClient.connect();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("availableDrivers");
        databaseReferenceGetDriverInfo = firebaseDatabase.getReference("users");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        polylines = new ArrayList<>();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
                    Toast.makeText(getContext(), "Showing Nearby Hospitals", Toast.LENGTH_SHORT).show();

                    new CountDownTimer(5000, 1000) {
                        @Override
                        public void onFinish() {

//                            getDirection(latLng);
                            radius=1;
                            driverFound = false;
                            limit= 10;
                            getNearestDriver();



                        }
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }
                    }.start();
                }
            }
        });
        }



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
                    driverFound = true;
                    driverId = key;

                 listener =   databaseReferenceGetDriverInfo.child(key).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            User driver = snapshot.getValue(User.class);
                            MarkerOptions markerOptions = new MarkerOptions();

                            markerOptions.position(new LatLng(location.latitude,location.longitude));
                            markerOptions.title(driver.getName() + "  "+ driver.getPhoneNumber());
                            int height = 100;
                            int width = 100;
                            BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ambulance2);
                            Bitmap b = bitmapdraw.getBitmap();
                            Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                            driverMarker = mMap.addMarker(markerOptions);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {


                 if(driverMarker!=null)
                 {
                     driverMarker.setPosition(new LatLng(location.latitude,location.longitude));

                 }

            }

            @Override
            public void onGeoQueryReady() {

                if(!driverFound && radius<limit)
                {
                    radius++;
                    getNearestDriver();
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

    private void getDirection(LatLng userLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(userLatLng,new LatLng(23.91326, 90.39630),nearest)
                .key(getString(R.string.google_maps_key))
                .build();
        routing.execute();

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
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
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
        }
    }
}