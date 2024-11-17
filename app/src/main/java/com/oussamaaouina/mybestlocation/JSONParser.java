package com.oussamaaouina.mybestlocation;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class JSONParser {

    String charset = "UTF-8";
    HttpURLConnection conn;
    DataOutputStream wr;
    StringBuilder result;
    URL urlObj;
    JSONObject jObj = null;
    StringBuilder sbParams;
    String paramsString;

    // Add a convenience method for delete requests
    public JSONObject makeDeleteRequest(String url, JSONObject jsonData) {
        try {
            urlObj = new URL(url);
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // Write JSON data to the connection
            if (jsonData != null) {
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.writeBytes(jsonData.toString());
                os.flush();
                os.close();
            }

            // Read the response
            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                Log.d("JSON Parser", "result: " + result.toString());
            }

            // Parse the response
            try {
                jObj = new JSONObject(result.toString());
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

        } catch (Exception e) {
            Log.e("JSON Parser", "Error in delete request: " + e.toString());
            // Create error JSON object
            try {
                jObj = new JSONObject();
                jObj.put("success", 0);
                jObj.put("message", "Error: " + e.getMessage());
            } catch (JSONException je) {
                Log.e("JSON Parser", "Error creating error object: " + je.toString());
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return jObj;
    }

    public  JSONObject makeRequest(String url)
    {

        try {
            urlObj = new URL(url);


        conn = (HttpURLConnection) urlObj.openConnection();
        } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (IOException e) {
            e.printStackTrace();
        }
        try {
                //Receive the response from the server
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                Log.d("JSON Parser", "result: " + result.toString());

            } catch (IOException e) {
                e.printStackTrace();
            }

            conn.disconnect();

            // try parse the string to a JSON object
            try {
                jObj = new JSONObject(result.toString());
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

            // return JSON Object
            return jObj;
        }
    public JSONObject makeHttpRequest(String url, String method, HashMap<String, String> params) {
        sbParams = new StringBuilder();
        if (params != null) {
            int i = 0;
            for (String key : params.keySet()) {
                try {
                    if (i != 0) {
                        sbParams.append("&");
                    }
                    sbParams.append(key).append("=")
                            .append(URLEncoder.encode(params.get(key), charset));

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                i++;
            }
        }

        try {
            urlObj = new URL(url);
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setRequestProperty("Accept-Charset", charset);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(10000);

            switch (method) {
                case "POST":
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    paramsString = sbParams.toString();
                    if (params != null) {
                        wr = new DataOutputStream(conn.getOutputStream());
                        wr.writeBytes(paramsString);
                        wr.flush();
                        wr.close();
                    }
                    break;

                case "GET":
                    conn.setDoOutput(false);
                    conn.setRequestMethod("GET");
                    if (sbParams.length() != 0) {
                        url += "?" + sbParams.toString();
                    }
                    break;

                case "DELETE":
                    conn.setDoOutput(false);
                    conn.setRequestMethod("DELETE");
                    break;
            }

            conn.connect();

            try (InputStream in = new BufferedInputStream(conn.getInputStream());
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                Log.d("JSON Parser", "result: " + result.toString());
            }

            try {
                jObj = new JSONObject(result.toString());
            } catch (JSONException e) {
                Log.e("JSON Parser", "Error parsing data " + e.toString());
            }

        } catch (Exception e) {
            Log.e("JSON Parser", "Error in http request: " + e.toString());
            try {
                jObj = new JSONObject();
                jObj.put("success", 0);
                jObj.put("message", "Error: " + e.getMessage());
            } catch (JSONException je) {
                Log.e("JSON Parser", "Error creating error object: " + je.toString());
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return jObj;
    }
}
