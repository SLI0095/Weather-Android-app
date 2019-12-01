package com.example.weatherforecast;

public class Weather {
    private String cityname;
    private String description;
    private String detail;
    private String icon;
    private double temp;
    private int pressure;
    private int humidity;

    public Weather(String cityname, String description, String detail, String icon, double temp, int pressure, int humidity)
    {
        this.cityname = cityname;
        this.description = description;
        this.detail = detail;
        this.icon = icon;
        this.temp = temp;
        this.pressure = pressure;
        this.humidity = humidity;
    }

}
