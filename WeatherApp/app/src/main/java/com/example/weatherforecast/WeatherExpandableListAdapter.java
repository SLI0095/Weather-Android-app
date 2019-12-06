package com.example.weatherforecast;

import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WeatherExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<Weather> mainWeather;
    private HashMap<Weather, List<Weather>> detail;

    public WeatherExpandableListAdapter(Context context, ArrayList<Weather> mainWeather, HashMap<Weather, List<Weather>> detail)
    {
        this.context = context;
        this.mainWeather = mainWeather;
        this.detail = detail;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public int getGroupCount() {
        return mainWeather.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.detail.get(this.mainWeather.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mainWeather.get(groupPosition);
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.detail.get(this.mainWeather.get(listPosition)).get(expandedListPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Weather weather = (Weather) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }

        ImageView icon = (ImageView)convertView.findViewById(R.id.iconImage);
        icon.setImageResource(nameOffile(weather.getIcon()));

        TextView temp = (TextView) convertView.findViewById(R.id.temp);
        String tempString = "" + (int) Math.round(weather.getTemp()) + "°C";
        temp.setText(tempString);

        TextView date = (TextView) convertView.findViewById(R.id.date);
        date.setText(weather.getDate());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final Weather weather = (Weather) getChild(groupPosition,childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_subitem, null);
        }
        TextView description = (TextView) convertView.findViewById(R.id.description);
        description.setText(weather.getDescription() + " (" + weather.getDetail() + ")");
        TextView humidity = (TextView) convertView.findViewById(R.id.humidity);
        humidity.setText("Humidity: " + weather.getHumidity() + "%");
        TextView pressure = (TextView) convertView.findViewById(R.id.pressure);
        pressure.setText("Pressure: " + weather.getPressure() + " hPa");

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
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
