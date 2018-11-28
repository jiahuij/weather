package com.example.jiahuij.weatherapp.Retrofit;

import com.example.jiahuij.weatherapp.model.WeatherResult;

import java.util.Observable;

import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherMap
{
    @GET("weather")
    io.reactivex.Observable<WeatherResult> getWeatherByLatLng(@Query("lat")String lat,
                                                              @Query("long")String lng,
                                                              @Query("id")String wid,
                                                              @Query("units")String unit);
}
