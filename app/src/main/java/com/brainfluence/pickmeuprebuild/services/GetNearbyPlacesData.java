package com.brainfluence.pickmeuprebuild.services;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.brainfluence.pickmeuprebuild.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
/**
 * @author Priyanka
 */

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private GoogleMap mMap;
    String url;
    private double startLat;
    private double startLong;
    public static LatLng nearest;
    public static String nearestPlaceName,nearestVicinity;

    @Override
    protected String doInBackground(Object... objects){
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        startLat = (Double)objects[2];
        startLong = (Double)objects[3];

        DownloadURL downloadURL = new DownloadURL();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s){

        List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        Log.d("nearbyplacesdata","called parse method");
        showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList)
    {
        nearest = null;
        float min = 10000000;
        nearestPlaceName = null;
        nearestVicinity = null;
        for(int i = 0; i < nearbyPlaceList.size(); i++)
        {

            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            double lat = Double.parseDouble( googlePlace.get("lat"));
            double lng = Double.parseDouble( googlePlace.get("lng"));

            float[] results = new float[10];
            Location.distanceBetween(startLat,startLong,lat,lng,results);

            if(results[0]<min)
            {
                min = results[0];
                nearest =  new LatLng( lat, lng);
                nearestPlaceName = googlePlace.get("place_name");
                nearestVicinity = googlePlace.get("vicinity");
            }


        }

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(nearest);
        markerOptions.title(nearestPlaceName + " : "+ nearestVicinity);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(nearest));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));

    }
}
