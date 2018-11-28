package com.example.jiahuij.weatherapp.common;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

public class common {

    public static final String APP_ID = "ea94297dc255123f480d386d32fd4f4c";
    public static Location current_location=null;

    public static String converUnixToDate(long dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm EEE MMM yyyy");
        String formatted = sdf.format(date);
        return formatted;
    }

    public static String converUnixToHour(long dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String formatted = sdf.format(date);
        return formatted;
    }
}
