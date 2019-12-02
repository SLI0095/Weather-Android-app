package com.example.weatherforecast;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Response;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private SubMenu FavouriteCitiesMenu;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText search_bar;
    private ArrayList<Weather> weatherList;
    private ListView listView;
    private WeatherAdapter Adapter;
    public String actualWeatherJSON = "", forecastJSON = "";
    public double longitude = -1, latitude = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weatherList = new ArrayList<Weather>();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        FavouriteCitiesMenu =  navigationView.getMenu().addSubMenu("Favourite cities");

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Search bar init
        search_bar = findViewById(R.id.search_bar);
        search_bar.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event != null &&
                                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            if (event == null || !event.isShiftPressed()) {
                                httpRequestForCity(search_bar.getText().toString());
                                search_bar.setText("");
                                search_bar.clearFocus();
                            }
                        }
                        return false;
                    }
                }
        );

        // Location manager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            longitude = location.getLongitude();
                            latitude = location.getLatitude();
                            httpRequestForLocation();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Could not get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        addFavouriteCity("Ostrava");
        addFavouriteCity("Praha");
        listView = (ListView)findViewById(R.id.weather_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        listView = (ListView)findViewById(R.id.weather_list);

        //Get location on start which call httprequest for location
        fusedLocationClient.getLastLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void addFavouriteCity(final String cityName)
    {
        FavouriteCitiesMenu.add(cityName).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                httpRequestForCity(cityName);
                return false;
            }
        });
        navigationView.invalidate();
    }

    public void httpRequestForCity(String cityName)
    {
        //final TextView txtView = findViewById(R.id.text_home);

        RequestQueue queue = Volley.newRequestQueue(this);

        //Actual weather request
        String url ="http://api.openweathermap.org/data/2.5/weather?q=" + cityName +"&APPID=4aa7f805e66520625f6b4017c52f4c83&units=metric";
        StringRequest stringRequestActual = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        actualWeatherJSON = response;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();

            }
        });

        //Forecast request
        url = "http://api.openweathermap.org/data/2.5/forecast?q=" + cityName +"&APPID=4aa7f805e66520625f6b4017c52f4c83&units=metric";
        StringRequest stringRequestForecast = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        forecastJSON = response;
                        DecodeJson();
                        //txtView.setText(actualWeatherJSON + '\n' + forecastJSON);
                        //TODO process JSON and show forecast on screen
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        //Adding requests to que
        queue.add(stringRequestActual);
        queue.add(stringRequestForecast);
    }

    public void httpRequestForLocation() {

        //final TextView txtView = findViewById(R.id.text_home);

        RequestQueue queue = Volley.newRequestQueue(this);

        //Actual weather request
        String url ="http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude +"&APPID=4aa7f805e66520625f6b4017c52f4c83&units=metric";
        StringRequest stringRequestActual = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        actualWeatherJSON = response;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + latitude + "&lon=" + longitude +"&APPID=4aa7f805e66520625f6b4017c52f4c83&units=metric";
        StringRequest stringRequestForecast = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        forecastJSON = response;
                        DecodeJson();
                        //txtView.setText(actualWeatherJSON + '\n' + forecastJSON);
                        //TODO process JSON and show forecast on screen
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequestActual);
        queue.add(stringRequestForecast);
    }

    public void DecodeJson()
    {
        //final TextView txtView = findViewById(R.id.text_home);
        try {
            listView = (ListView)findViewById(R.id.weather_list);
            weatherList = new ArrayList<Weather>();
            JSONObject root = new JSONObject(actualWeatherJSON);
            String cityname = root.getString("name");
            JSONArray weather = root.getJSONArray("weather");
            JSONObject weatherobj = weather.getJSONObject(0);
            String main = weatherobj.getString("main");
            String description = weatherobj.getString("description");
            String icon = weatherobj.getString("icon");
            JSONObject weatherMain = root.getJSONObject("main");
            double temp = weatherMain.getDouble("temp");
            int pressure = weatherMain.getInt("pressure");
            int humidity = weatherMain.getInt("humidity");

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String date = df.format(c);

            weatherList.add(new Weather(cityname,main,description,icon,temp,pressure,humidity,date));

            root = new JSONObject(forecastJSON);
            JSONArray forecasts = root.getJSONArray("list");
            for(int i = 0; i < forecasts.length(); i++)
            {
                weatherobj = forecasts.getJSONObject(i);
                String checkdate = weatherobj.getString("dt_txt");
                if(checkdate.contains("12:00") && !checkdate.contains(date))
                {
                    date = checkdate.substring(0,10);
                    weatherMain = weatherobj.getJSONObject("main");
                    temp = weatherMain.getDouble("temp");
                    pressure = weatherMain.getInt("pressure");
                    humidity = weatherMain.getInt("humidity");
                    weather = weatherobj.getJSONArray("weather");
                    JSONObject weatherDesc = weather.getJSONObject(0);
                    main = weatherDesc.getString("main");
                    description = weatherDesc.getString("description");
                    icon = weatherDesc.getString("icon");

                    weatherList.add(new Weather(cityname,main,description,icon,temp,pressure,humidity,date));
                }
            }
            weatherList.remove(weatherList.size()-1);
            Adapter = new WeatherAdapter(this,weatherList);
            listView.setAdapter(Adapter);

            search_bar.setText(cityname);
            //txtView.setText(cityname);

            //TODO save weatherList into file


            //txtView.setText(cityname + '\n' + main + '\n' + description + '\n' + icon + '\n' + temp + '\n' + pressure + '\n' + humidity + '\n' + date);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
