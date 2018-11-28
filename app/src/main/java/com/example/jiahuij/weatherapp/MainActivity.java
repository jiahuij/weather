package com.example.jiahuij.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.os.Looper;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.jiahuij.weatherapp.Adapter.ViewPagerAdapter;
import com.example.jiahuij.weatherapp.Retrofit.OpenWeatherMap;
import com.example.jiahuij.weatherapp.Retrofit.RetrofitClient;
import com.example.jiahuij.weatherapp.common.common;
import com.example.jiahuij.weatherapp.model.WeatherResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.sql.CommonDataSource;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageView img_weather;
    private TextView txt_city_name,txt_humidity,txt_sunrise,txt_sunset,txt_pressure,txt_temperature,txt_description,txt_date_time,txt_wind,txt_geo_coord;
    LinearLayout weather_panel;
    ProgressBar loading;

    CompositeDisposable compositeDisposable;
    OpenWeatherMap mService;

    private CoordinatorLayout coordinatorLayout;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.root_view);
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(OpenWeatherMap.class);

        img_weather = findViewById(R.id.img_weather);
        txt_city_name = findViewById(R.id.text_city_name);
        txt_humidity = findViewById(R.id.text_humidity);
        txt_sunrise = findViewById(R.id.text_sunrise);
        txt_sunset = findViewById(R.id.text_sunset);
        txt_pressure = findViewById(R.id.text_pressure);
        txt_temperature = findViewById(R.id.text_temperature);
        txt_description = findViewById(R.id.text_description);
        txt_date_time = findViewById(R.id.text_date_time);
        txt_wind = findViewById(R.id.text_wind);
        txt_geo_coord = findViewById(R.id.text_geo_coords);

        weather_panel =findViewById(R.id.weather_panel);
        loading = findViewById(R.id.loading);

        getWeatherInfoMation();



        //  toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            buildLocationRequest();
                            buildLocationCallBack();

                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this
                                    , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                return;
                            }
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.myLooper());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        Snackbar.make(coordinatorLayout,"Permission Denied",Snackbar.LENGTH_LONG)
                                .show();
                    }
                }).check();


    }

    private void getWeatherInfoMation() {
        compositeDisposable.add(mService.getWeatherByLatLng(String.valueOf(common.current_location.getLatitude()),
                String.valueOf(common.current_location.getLongitude()),
                common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        //Load image
                        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/w/")
                        .append(weatherResult.getWeather().get(0).getIcon())
                        .append(".png").toString()).into(img_weather);


                        //load Info
                        txt_city_name.setText(weatherResult.getName());

                        txt_description.setText(new StringBuilder("Weather in ")
                        .append(weatherResult.getName()).toString());

                        txt_temperature.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp()))
                        .append("°C").toString());

                        txt_date_time.setText(common.converUnixToDate(weatherResult.getDt()));

                        txt_pressure.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure()))
                        .append("hpa").toString());

                        txt_humidity.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity()))
                        .append(" %").toString());

                        txt_sunrise.setText(common.converUnixToHour(weatherResult.getSys().getSunrise()));

                        txt_sunset.setText(common.converUnixToHour(weatherResult.getSys().getSunset()));

                        txt_geo_coord.setText(new StringBuilder("[").append(weatherResult.getCoord().toString())
                        .append("]").toString());

                        //Display panel
                        weather_panel.setVisibility(View.VISIBLE);
                        loading.setVisibility(View.GONE);

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Toast.makeText(MainActivity.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
        );

    }

    private void buildLocationCallBack() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                common.current_location = locationResult.getLastLocation();

                viewPager = findViewById(R.id.view_pager);
                setupViewPager(viewPager);
                tabLayout = findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);

                //log
                Log.d("Localtion",locationResult.getLastLocation().getAltitude()+"/"+locationResult.getLastLocation().getLongitude());
            }
        };
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10.0f);
    }
}
