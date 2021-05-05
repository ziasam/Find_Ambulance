package com.brainfluence.pickmeuprebuild.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioRouting;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.brainfluence.pickmeuprebuild.GetNearbyPlacesData;
import com.brainfluence.pickmeuprebuild.R;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


import java.util.ArrayList;
import java.util.List;

import static com.brainfluence.pickmeuprebuild.GetNearbyPlacesData.nearest;


public class HomeFragment extends Fragment implements OnMapReadyCallback , RoutingListener {

    private HomeViewModel homeViewModel;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private SupportMapFragment mapFragment;
    private LocationManager locationManager;
    private Boolean gpsEnabled;
    private int PROXIMITY_RADIUS = 5000;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.purple_500};


    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        polylines = new ArrayList<>();
        init();
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return root;
    }


    private void init() {


        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 12f));
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
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
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

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

        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
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

                        if(gpsEnabled)
                        {
                            mMap.setMyLocationEnabled(true);
                            mMap.getUiSettings().setMyLocationButtonEnabled(true);
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getContext(), "Couldn't Load", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f));
                                            GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();
                                            Object dataTransfer[] = new Object[4];
                                            String hospital = "hospital";
                                            String url = getUrl(userLatLng.latitude, userLatLng.longitude, hospital);
                                            dataTransfer[0] = mMap;
                                            dataTransfer[1] = url;
                                            dataTransfer[2] = location.getLatitude();
                                            dataTransfer[3] = location.getLongitude();
                                            getNearbyPlacesData.execute(dataTransfer);
                                            Toast.makeText(getContext(), "Showing Nearby Hospitals", Toast.LENGTH_SHORT).show();

                                            new CountDownTimer(5000, 1000) {
                                                @Override
                                                public void onFinish() {

                                                    getDirection(userLatLng);



                                                }
                                                @Override
                                                public void onTick(long millisUntilFinished) {
                                                }
                                            }.start();

                                        }
                                    });


                        }

                        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @Override
                            public boolean onMyLocationButtonClick() {
                                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return false;
                                }
                                if(gpsEnabled)
                                {
                                    fusedLocationProviderClient.getLastLocation()
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getContext(), "Couldn't Load", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnSuccessListener(new OnSuccessListener<Location>() {
                                                @Override
                                                public void onSuccess(Location location) {
                                                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f));
                                                }
                                            });
                                }
                                return true;
                            }
                        });
                        View locationButton =((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                .getParent()).findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP,0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,RelativeLayout.TRUE);
                        params.setMargins(0,0,0,50);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(),"Permission denied!",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();

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
}