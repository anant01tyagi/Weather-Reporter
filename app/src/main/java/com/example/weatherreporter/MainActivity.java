package com.example.weatherreporter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityName, tempTV, conditionTV;
    private RecyclerView forecastRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private int PERMISSION_CODE = 1;
    private String CityName;
    private int REQUEST_CHECK_CODE = 8989;
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);
        homeRL = findViewById(R.id.idRLhome);
        loadingPB = findViewById(R.id.idPB);
        cityName = findViewById(R.id.idTVCityname);
        tempTV = findViewById(R.id.idTVTemp);

        conditionTV = findViewById(R.id.idTVCondition);
        forecastRV = findViewById(R.id.idRVforecast);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVback);
        iconIV = findViewById(R.id.idIVicon);
        searchIV = findViewById(R.id.idIVsearch);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        forecastRV.setAdapter(weatherRVAdapter);

        //Check if location services are turned on, ask if not
//        LocationRequest request = new LocationRequest().setFastestInterval(1500).setInterval(3000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(request);
//        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
//
//
//        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
//            @Override
//            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
//                try {
//                    task.getResult(ApiException.class);
//                } catch (ApiException e) {
//                    switch(e.getStatusCode()){
//                        case LocationSettingsStatusCodes
//                                .RESOLUTION_REQUIRED:
//                            try {
//                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
//                                resolvableApiException.startResolutionForResult(MainActivity.this, REQUEST_CHECK_CODE);
//                            } catch (IntentSender.SendIntentException ex) {
//                                ex.printStackTrace();
//                            } catch (ClassCastException ex){}
//                            break;
//                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:{
//                            break;
//                        }
//                    }
//                    e.printStackTrace();
//                }
//            }
//        });


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
  //          Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(3000);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            Toast.makeText(MainActivity.this,"lat is: " + lat, Toast.LENGTH_SHORT).show();

                            CityName = getCityName(location.getLongitude(), location.getLatitude());
                            getWeatherInfo(CityName);
                        }
                    }
                }
            };
//            if (fusedLocationProviderClient != null) {
//                fusedLocationProviderClient.removeLocationUpdates(locationCallback);
 //           }
        }else{
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);

        }



        searchIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String city = Objects.requireNonNull(cityEdt.getText()).toString();
                if(city.isEmpty()){
                    Toast.makeText(MainActivity.this, "Please enter city name", Toast.LENGTH_SHORT).show();
                }else{
                    cityName.setText(CityName);
                    getWeatherInfo(city);

                }
            }
        });




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please grant permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude ){
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude, longitude, 10);

            for(Address adr : addresses){
                if(adr!=null){
                    String city = adr.getLocality();
                    if (city!=null && !city.equals("")){
                        cityName = city;
                    }else{
                        Log.d("Tag","CITY NOT FOUND");
                        Toast.makeText(this, "User city not found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }
        return cityName;
    }
    private void getWeatherInfo(String CityName){
        String url = "https://api.weatherapi.com/v1/forecast.json?key=f77d3d6dfc944ad0b8e132119220707&q=" + CityName + "&days=1&aqi=no&alerts=no";
        cityName.setText(CityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onResponse(JSONObject response) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();

                String Temperature = "";
                int isDay = 1;
                String condition = "";
                String conditionIcon = "";

                try {
                    Temperature = response.getJSONObject("current").getString("temp_c");
                    isDay = response.getJSONObject("current").getInt("is_day");
                    condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");


                    JSONObject forecastObj = response.getJSONObject("forecast");
                    JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                    JSONArray hourArray = Objects.requireNonNull(forecastO).getJSONArray("hour");


                    for(int i = 0; i< Objects.requireNonNull(hourArray).length(); i++){


                        JSONObject hourObj = hourArray.getJSONObject(i);
                        String time = Objects.requireNonNull(hourObj).getString("time");
                        String temper = hourObj.getString("temp_c");
                        String img = hourObj.getJSONObject("condition").getString("icon");
                        String wind = hourObj.getString("wind_kph");

                        weatherRVModalArrayList.add(new WeatherRVModal(time, temper, wind, img));

                    }
                    weatherRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                tempTV.setText(Temperature + "°C");
                Picasso.get().load("https:".concat(conditionIcon)).into(iconIV);
                conditionTV.setText(condition);
                if(isDay==1){
                    //morning
                    Picasso.get().load("https://images.unsplash.com/photo-1566228015668-4c45dbc4e2f5?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1887&q=80").into(backIV);
                }
                else{
                    //evening
                    Picasso.get().load("https://images.pexels.com/photos/574115/pexels-photo-574115.jpeg").into(backIV);
                }

               // Toast.makeText(MainActivity.this, "Response executed", Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Please enter valid city name.", Toast.LENGTH_SHORT ).show();
//                Log.e("VOLLEY", error.getMessage());

            }
        });

        requestQueue.add(jsonObjectRequest);
    }
}