package com.example.weatherforecast;

import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.baoyz.widget.PullRefreshLayout;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ExpandableListView;
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
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;
    private SubMenu FavouriteCitiesMenu;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText search_bar;
    private List<Weather> weatherList;
    private HashMap<Weather, List<Weather>> expandableListData;
    private ExpandableListView listView;
    private WeatherExpandableListAdapter Adapter;
    private Menu menu;
    private String actualcity;
    public String actualWeatherJSON = "", forecastJSON = "";
    public double longitude = -1, latitude = -1;
    private DatabaseHelper databaseHelper;
    private PullRefreshLayout pullHook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Shared preferences loading saved JSONs
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("JSONs", Context.MODE_PRIVATE);
        if(sharedPref.contains("weather"))
        {
            this.actualWeatherJSON = sharedPref.getString("weather", "");
        }
        if(sharedPref.contains("forecast"))
        {
            this.forecastJSON = sharedPref.getString("forecast", "");
        }


        expandableListData = new HashMap<Weather, List<Weather>>();
        weatherList = new ArrayList<Weather>();
        databaseHelper = new DatabaseHelper(this);
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
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        if (location != null) {
//                            longitude = location.getLongitude();
//                            latitude = location.getLatitude();
//                            httpRequestForLocation();
//                            pullHook.setRefreshing(false);
//                            pullHook.setRefreshing(false);
//                        }
//                        else {
//                            Toast.makeText(MainActivity.this, "Could not get location, loading last forecast", Toast.LENGTH_SHORT).show();
//                            DecodeJson();
//                            pullHook.setRefreshing(false);
//                        }
//                    }
//                });


        //fill saved favourite cities

        final Cursor data = databaseHelper.getData();
        while (data.moveToNext()) {
            final String city = data.getString(1);
            FavouriteCitiesMenu.add(city).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    httpRequestForCity(city);
                    return false;
                }
            });
        }


        listView = (ExpandableListView) findViewById(R.id.weather_list);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Pull refresh setting
        pullHook = findViewById(R.id.swipeRefreshLayout);
        pullHook.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLocation();
                pullHook.setRefreshing(false);
            }
        });

        listView = (ExpandableListView) findViewById(R.id.weather_list);

        getLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.addCity:
                addFavouriteCity(actualcity);
                return true;
            case R.id.removeCity:
                removeFavouriteCity(actualcity);
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void addFavouriteCity(final String cityName)
    {
        if(databaseHelper.addData(cityName)) {
            FavouriteCitiesMenu.add(cityName).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    httpRequestForCity(cityName);
                    return false;
                }
            });
            navigationView.invalidate();
            setMenuItemVisibility(cityName);
        }
    }
    public void removeFavouriteCity(String cityName)
    {
        if(databaseHelper.delete(cityName)) {
            FavouriteCitiesMenu.clear();
            final Cursor data = databaseHelper.getData();
            while (data.moveToNext()) {
                final String city = data.getString(1);
                FavouriteCitiesMenu.add(city).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        httpRequestForCity(city);
                        return false;
                    }
                });
            }
        }
        setMenuItemVisibility(cityName);
    }

    public void httpRequestForCity(String cityName)
    {
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
                Toast.makeText(MainActivity.this, "City not found or you are offline, loading last forecast", Toast.LENGTH_SHORT).show();
                DecodeJson();

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
                        saveWeather();
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
                        saveWeather();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequestActual);
        queue.add(stringRequestForecast);
    }

    public void setMenuItemVisibility(String city)
    {
        if(databaseHelper.exists(city))
        {
            menu.findItem(R.id.addCity).setVisible(false);
            menu.findItem(R.id.removeCity).setVisible(true);
        }
        else
        {
            menu.findItem(R.id.addCity).setVisible(true);
            menu.findItem(R.id.removeCity).setVisible(false);
        }
    }

    public void DecodeJson()
    {
        try {
            expandableListData = new HashMap<Weather, List<Weather>>();
            ArrayList<Weather> keyset = new ArrayList<>();
            listView = (ExpandableListView) findViewById(R.id.weather_list);
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
            actualcity = cityname;

            Date c = Calendar.getInstance().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            String date = df.format(c);

            Weather detail = new Weather(cityname,main,description,icon,temp,pressure,humidity,date);
            weatherList = new ArrayList<Weather>();
            weatherList.add(detail);
            keyset.add(detail);
            expandableListData.put(detail,weatherList);

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

                    detail = new Weather(cityname,main,description,icon,temp,pressure,humidity,date);
                    weatherList = new ArrayList<Weather>();
                    weatherList.add(detail);
                    keyset.add(detail);
                    expandableListData.put(detail,weatherList);
                }
            }

            Adapter = new WeatherExpandableListAdapter(this, keyset,expandableListData);



            listView.setAdapter(Adapter);

            search_bar.setText(cityname);
            setMenuItemVisibility(cityname);

            pullHook.setRefreshing(false);



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void saveWeather()
    {
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences("JSONs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("weather", actualWeatherJSON);
        editor.putString("forecast", forecastJSON);
        editor.commit();
    }

    public void getLocation()
    {
        GPSTracker gps = new GPSTracker(MainActivity.this);
        if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            httpRequestForLocation();
        }else{
            Toast.makeText(MainActivity.this, "Could not get location, loading last forecast", Toast.LENGTH_SHORT).show();
        }
    }

}
