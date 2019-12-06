package com.example.weatherforecast;

import java.io.Serializable;

public class Weather implements Serializable {
    private String cityname;
    private String description;
    private String detail;
    private String icon;
    private double temp;
    private int pressure;
    private int humidity;
    private String date;

    public Weather(String cityname, String description, String detail, String icon, double temp, int pressure, int humidity, String date)
    {
        this.cityname = cityname;
        this.description = description;
        this.detail = detail;
        this.icon = icon;
        this.temp = temp;
        this.pressure = pressure;
        this.humidity = humidity;
        this.date = date;
    }
    public double getTemp()
    {
        return this.temp;
    }


    public String getDate() {
        return date;
    }

    public String getIcon() {
        return icon;
    }
    public String getDescription()
    {
        return this.description;
    }
    public String getDetail()
    {
        return this.detail;
    }
    public int getPressure()
    {
        return this.pressure;
    }
    public int getHumidity()
    {
        return  this.humidity;
    }

}
