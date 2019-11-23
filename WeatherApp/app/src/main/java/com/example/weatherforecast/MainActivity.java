package com.example.weatherforecast;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private SubMenu FavouriteCitiesMenu;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText search_bar;
    public String actualWeatherJSON = "", forecastJSON = "";
    public double longitude, latitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Could not get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        addFavouriteCity("Ostrava");
        addFavouriteCity("Praha");
    }

    @Override
    protected void onStart() {
        super.onStart();
        httpRequestForLocation();
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
        final TextView txtView = findViewById(R.id.text_home);

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

            }
        });

        //Forecast request
        url = "http://api.openweathermap.org/data/2.5/forecast?q=" + cityName +"&APPID=4aa7f805e66520625f6b4017c52f4c83&units=metric";
        StringRequest stringRequestForecast = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        forecastJSON = response;
                        txtView.setText(actualWeatherJSON + '\n' + forecastJSON);
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

        final TextView txtView = findViewById(R.id.text_home);
        //Getting location
        if(latitude == 0 && longitude == 0)
        {
            fusedLocationClient.getLastLocation();
        }

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
                        txtView.setText(actualWeatherJSON + '\n' + forecastJSON);
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
}
