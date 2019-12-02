package com.example.weatherforecast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class WeatherAdapter extends ArrayAdapter<Weather> {

    private Context mContext;
    private List<Weather> weatherList = new ArrayList<>();

    public WeatherAdapter(Context context, ArrayList<Weather> list)
    {
        super(context,0,list);
        mContext = context;
        weatherList = list;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if(listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        Weather currentWeather = weatherList.get(position);

        ImageView icon = (ImageView)listItem.findViewById(R.id.iconImage);
        icon.setImageResource(nameOffile(currentWeather.getIcon()));

        TextView temp = (TextView) listItem.findViewById(R.id.temp);
        String tempString = "" + currentWeather.getTemp() + "°C";
        temp.setText(tempString);

        TextView date = (TextView) listItem.findViewById(R.id.date);
        date.setText(currentWeather.getDate());

        return listItem;
    }

    public int nameOffile(String id)
    {
        switch(id)
        {
            case "01d":
                return R.drawable.day;
            case "01n":
                return R.drawable.night;
            case "02d":
                return R.drawable.fewcloudsday;
            case "02n":
                return R.drawable.fewcloudsnight;
            case "03d":
            case "03n":
                return R.drawable.clouds;
            case "04d":
            case "04n":
                return R.drawable.brokenclouds;
            case "09d":
            case "09n":
                return R.drawable.showerrain;
            case "10d":
                return R.drawable.rainday;
            case "10n":
                return R.drawable.rainnight;
            case "11d":
            case "11n":
                return R.drawable.storm;
            case "13d":
            case "13n":
                return R.drawable.snow;
            case "50d":
            case "50n":
                return R.drawable.mist;
        }
        return R.drawable.day;
    }


}
