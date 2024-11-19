package com.oussamaaouina.mybestlocation;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.oussamaaouina.mybestlocation.databinding.ActivityMapsBinding;
import com.oussamaaouina.mybestlocation.ui.slideshow.SlideshowFragment;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private double receivedLatitude;
    private double receivedLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         binding = ActivityMapsBinding.inflate(getLayoutInflater());
         setContentView(binding.getRoot());
         Intent intent = getIntent();
         if (intent != null) {

           receivedLatitude = intent.getDoubleExtra("latitude", 0.0);
            receivedLongitude = intent.getDoubleExtra("longitude", 0.0);

     }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        if (receivedLatitude != 0.0 && receivedLongitude != 0.0) {
            // Create a LatLng object with the received coordinates
            LatLng receivedLocation = new LatLng(receivedLatitude, receivedLongitude);

            // Add a marker at the received location
            mMap.addMarker(new MarkerOptions()
                    .position(receivedLocation)
                    .title("Received Location"));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(receivedLocation, 15f));
        } else {
            // Fallback to default location if no coordinates were received
            LatLng defaultLocation = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(defaultLocation).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // Clear previous markers
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));

                // Create an intent to send back the location
                Intent returnIntent = new Intent();
                returnIntent.putExtra("latitude", latLng.latitude);
                returnIntent.putExtra("longitude", latLng.longitude);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });
    }


}





