package com.oussamaaouina.mybestlocation.ui.home;
import com.oussamaaouina.mybestlocation.MapsActivity;
import com.oussamaaouina.mybestlocation.R;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;
import org.json.JSONObject;
import com.oussamaaouina.mybestlocation.Config;
import com.oussamaaouina.mybestlocation.JSONParser;
import com.oussamaaouina.mybestlocation.Position;
import com.oussamaaouina.mybestlocation.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    ArrayList<Position> data = new ArrayList<>();
    ArrayAdapter<Position> adapter;
    ListView listView;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize adapter here
        adapter = new ArrayAdapter<Position>(getActivity(),
                R.layout.item_position,
                R.id.text_pseudo,
                data) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Position pos = getItem(position);

                TextView coordsView = view.findViewById(R.id.text_coordinates);
                TextView numberView = view.findViewById(R.id.text_number);
                Button deleteBtn = view.findViewById(R.id.btn_delete);
                Button mapBtn = view.findViewById(R.id.btn_map);
                Button msgBtn = view.findViewById(R.id.btn_sms);

                coordsView.setText("Lat: " + pos.latitude + ", Long: " + pos.longitude);
                numberView.setText("Number: " + pos.numero);

                deleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Delete Position")
                                .setMessage("Are you sure you want to delete this position?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        new DeletePosition(pos.id, position).execute();
                                    }
                                })
                                .setNegativeButton("No", null)
                                .show();
                    }
                });

                mapBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), MapsActivity.class);
        intent.putExtra("latitude", pos.latitude);
        intent.putExtra("longitude", pos.longitude);
        startActivityForResult(intent, 1);
    }
});


                mapBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), MapsActivity.class);


        intent.putExtra("latitude", Double.valueOf(pos.latitude));
        intent.putExtra("longitude", Double.valueOf(pos.longitude));


        startActivity(intent);
    }
});
                msgBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {

                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(pos.numero, null, "I'm at " + pos.latitude + ", " + pos.longitude, null, null);
                        } catch (Exception e) {
                            Log.d(e.getMessage(), "Error sending SMS: " + e.getMessage());
                            Toast.makeText(getActivity(),
                                    "Error sending SMS: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
});
                return view;
            }
        };

        listView = binding.listLocations;
        listView.setAdapter(adapter);  // Set the adapter immediately

        binding.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Download d = new Download();
                d.execute();
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

    private class DeletePosition extends AsyncTask<Void, Void, Boolean> {
        private int id;
        private int position;
        private AlertDialog progressDialog;

        public DeletePosition(int id, int position) {
            this.id = id;
            this.position = position;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new AlertDialog.Builder(getActivity())
                    .setMessage("Deleting...")
                    .setCancelable(false)
                    .show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("id", id);

                JSONParser parser = new JSONParser();
                JSONObject response = parser.makeDeleteRequest(Config.url_delete, dataToSend);

                return response != null && response.getInt("success") == 1;
            } catch (Exception e) {
                Log.e("Delete Position", "Error: " + e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();

            if (success) {
                data.remove(position);
                adapter.notifyDataSetChanged();  // This will now work correctly
                Toast.makeText(getActivity(),
                        "Position deleted successfully",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(),
                        "Failed to delete position",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    class Download extends AsyncTask {
        @Override
        protected void onPreExecute() {
            data.clear();  // Clear existing data before downloading
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Download");
            builder.setMessage("Downloading...");
            alert = builder.create();
            alert.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                Thread.sleep(2000);
                JSONParser parser = new JSONParser();
                JSONObject response = parser.makeRequest(Config.url_getAll);

                int success = response.getInt("success");
                Log.e("response", "==" + success);
                if(success == 1) {
                    JSONArray positions = response.getJSONArray("positions");
                    Log.e("response", "==" + response);
                    for (int i = 0; i < positions.length(); i++) {
                        JSONObject obj = positions.getJSONObject(i);
                        int id = obj.getInt("id");
                        String pseudo = obj.getString("pseudo");
                        String longitude = obj.getString("longitude");
                        String latitude = obj.getString("latitude");
                        String numero = obj.getString("num");
                        Position p = new Position(id, pseudo, longitude, latitude, numero);
                        data.add(p);
                    }
                }
            } catch (Exception e) {
                Log.e("Download Error", "Error: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            alert.dismiss();
            adapter.notifyDataSetChanged();  // Update the existing adapter
        }
    }
}