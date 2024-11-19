package com.oussamaaouina.mybestlocation.ui.slideshow;

import static android.content.Intent.getIntent;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.oussamaaouina.mybestlocation.Config;
import com.oussamaaouina.mybestlocation.JSONParser;
import com.oussamaaouina.mybestlocation.MapsActivity;
import com.oussamaaouina.mybestlocation.databinding.FragmentSlideshowBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



public class SlideshowFragment extends Fragment {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private double receivedLatitude;
    private double receivedLongitude;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private FragmentSlideshowBinding binding;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, get location
            getCurrentLocation();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
//                Toast.makeText(getActivity(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void getCurrentLocation() {
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                System.out.println(location);
                updateLocationUI(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
//                alert.dismiss();

//                Toast.makeText(getActivity(), "Please enable GPS", Toast.LENGTH_SHORT).show();
            }
        };

        // Check permission again before requesting location updates
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Request location updates
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,  // 5 seconds
                    10,    // 10 meters
                    locationListener
            );

            // Get last known location immediately
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                updateLocationUI(lastKnownLocation);
            }
        }
    }
    private void updateLocationUI(Location location) {
        if (location != null) {
            binding.textLatitude.setText(String.format("%.6f", location.getLatitude()));
            binding.textLongitude.setText(String.format("%.6f", location.getLongitude()));
        }
    }


    private void handleBackPress() {
        if (binding != null) {
            clearFields();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }
    private void clearFields() {
        if (binding != null) {
            binding.textNumero.setText("");
            binding.textPseudo.setText("");
            binding.textLongitude.setText("");
            binding.textLatitude.setText("");
        }
    }
    private void updateLocationTexts(double latitude, double longitude) {
        if (binding != null) {
            binding.textLatitude.setText(String.format("%.6f", latitude));
            binding.textLongitude.setText(String.format("%.6f", longitude));
        }
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Button add = binding.addBtn;
        Button map = binding.mapBtn;
        Button back = binding.backBtn;
        Button mylocation = binding.mylocationBtn;

        // open map
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivityForResult(new Intent(getActivity(), MapsActivity.class), 1);
            }

        });


        // go baaack
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.textNumero.setText("");
                binding.textPseudo.setText("");
                binding.textLongitude.setText("");
                binding.textLatitude.setText("");

                getActivity().onBackPressed();

            }
        });

        mylocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermission();
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,String> params = new HashMap<>();
                params.put("num",binding.textNumero.getText().toString());
                params.put("pseudo",binding.textPseudo.getText().toString());

                params.put("longitude",binding.textLongitude.getText().toString());
                params.put("latitude",binding.textLatitude.getText().toString());

                Upload u = new Upload(params);
                u.execute();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    AlertDialog alert;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            receivedLatitude = data.getDoubleExtra("latitude", 0.0);
            receivedLongitude = data.getDoubleExtra("longitude", 0.0);

            updateLocationTexts(receivedLatitude, receivedLongitude);
        }
    }
    class Upload extends AsyncTask {
        HashMap<String,String> params;
        public Upload(HashMap<String,String> params) {
            this.params = params;
        }

        @Override
        protected void onPreExecute() {
            // UI Thread
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Upload");
            builder.setMessage("Uploading...");
            alert = builder.create();
            alert.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            // Code de thread secondaire (background)
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // problem: pas d'acces a l'interface graphique
            JSONParser parser = new JSONParser();
            JSONObject response = parser.makeHttpRequest(Config.url_add,
                    "POST",
                    params);

            try {
                int success = response.getInt("success");
                Log.e("response", "==" + success);
                if(success == 1 ){
                    Log.e("response", "===" + response);
                    binding.textNumero.setText("");
                    binding.textPseudo.setText("");
                    binding.textLongitude.setText("");
                    binding.textLatitude.setText("");
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            // UI Thread (Thread principal)
            super.onPostExecute(o);
            alert.dismiss();



        }
    }
}