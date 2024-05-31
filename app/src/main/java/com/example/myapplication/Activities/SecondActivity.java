package com.example.myapplication.Activities;

import static java.lang.Math.max;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.myapplication.Adapters.FutureAdapter;
import com.example.myapplication.Adapters.HourlyAdapters;
import com.example.myapplication.Domains.FutureDomain;
import com.example.myapplication.Domains.Hourly;
import com.example.myapplication.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {
    TextView tmrw, temp_tom, textView3;
    ImageView imageView, imageView3;
    private RecyclerView.Adapter adapterTomorrow;
    public RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageView = findViewById(R.id.imageView2);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        tmrw = findViewById(R.id.tomorrow);
        temp_tom = findViewById(R.id.temperature_tom);
        imageView3 = findViewById(R.id.imageView3);
        textView3 = findViewById(R.id.textView3);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        tmrw.setText(checker(month, day, year));
        String url = "https://api.open-meteo.com/v1/forecast?latitude=43.2567&longitude=76.9286&hourly=temperature_2m,relative_humidity_2m,rain,snowfall,weather_code,wind_speed_80m&timezone=GMT";
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject temperature = response.getJSONObject("hourly");
                            // String temp2 = temperature.getJSONArray("temperature_2m").getString(Math.abs(hour - 6));
                            int code = temperature.getJSONArray("weather_code").getInt(Math.abs(hour - 6));
                            int max1 = temperature.getJSONArray("temperature_2m").getInt(Math.abs(hour - 6));
                            int max2 = 0;
                            int min1 = temperature.getJSONArray("temperature_2m").getInt(Math.abs(hour - 6));
                            String weather = "";
                            for (int i = 24; i < 48; i++) {
                                max2 = temperature.getJSONArray("temperature_2m").getInt(Math.abs(i));
                                if (max1 < max2) {
                                    max1 = max2;
                                }
                                if (min1 > max2) {
                                    min1 = max2;
                                }

                                String kek = dodo(temperature.getJSONArray("weather_code").getInt(i));
                                if (kek.equals("storm")){
                                    imageView3.setImageResource(R.drawable.storm);
                                    weather = "Storm";
                                } else if (kek.equals("snowy") && !weather.equals("Storm")){
                                    imageView3.setImageResource(R.drawable.snowy);
                                    weather = "Snowy";
                                } else if (kek.equals("rainy") && !weather.equals("Snowy")){
                                    imageView3.setImageResource(R.drawable.rainy);
                                    weather = "Rainy";
                                }else if (kek.equals("cloudy_sunny") && !weather.equals("Rainy")){
                                    imageView3.setImageResource(R.drawable.cloudy_sunny);
                                    weather = "Cloudy";
                                }else if (kek.equals("sunny") && !weather.equals("Cloudy")){
                                    imageView3.setImageResource(R.drawable.sunny);
                                    weather = "sunny";
                                }

                            }
                            textView3.setText(weather);
                            int[] max_arr = new int[6];
                            int[] min_arr = new int[6];

                            int k = -1;
                            for(int i = 0; i < 6; i++){
                                min_arr[i] = temperature.getJSONArray("temperature_2m").getInt(Math.abs(i));
                            }

                            for(int i = 48; i < 24 * 7; i++){
                                if (i % 24 == 0){
                                    k += 1;
                                }
                                int g = temperature.getJSONArray("temperature_2m").getInt(Math.abs(i));
                                if (max_arr[k] < g){
                                    max_arr[k] = g;
                                }
                                if (min_arr[k] > g){
                                    min_arr[k] = g;
                                }
                            }

                            Toast.makeText(SecondActivity.this, String.valueOf(min_arr[1]), Toast.LENGTH_SHORT).show();
                            temp_tom.setText(String.valueOf(max1) + "°C");
                            ArrayList<FutureDomain> items = new ArrayList<>();
                            int week = calendar.get(Calendar.DAY_OF_WEEK);
                            String[] Week = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

                            for(int i = 0; i < 5; i++){
                                String kek = dodo(temperature.getJSONArray("weather_code").getInt((i + 1) * 24));
                                String week_str = Week[(week + i) % 7];
                                items.add(new FutureDomain(week_str, kek, kek, max_arr[i], min_arr[i]));
                            }
                            recyclerView = findViewById(R.id.view2);
                            recyclerView.setLayoutManager(new LinearLayoutManager(SecondActivity.this, LinearLayoutManager.VERTICAL, false));
                            adapterTomorrow = new FutureAdapter(items);
                            recyclerView.setAdapter(adapterTomorrow);

                        } catch (JSONException e) {
                            temp_tom.setText("Trying");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        temp_tom.setText("error");
                    }
                });
        RequestQueue requestqueue = Volley.newRequestQueue(this);
        requestqueue.add(request);
    }





    public String checker(int month, int day, int year) {
        String ans = "";
        String[] Month = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String vis = "";
        if (year % 400 == 0) {
            vis = "yes";
        } else if (year % 100 == 0) {
            vis = "no";
        } else if (year % 4 == 0) {
            vis = "yes";
        }
        if (month == 1 && vis.equals("yes")) {
            if (day + 1 <= 29) {
                ans += Month[month] + " " + String.valueOf(day + 1);
            } else {
                ans += Month[month + 1] + " " + String.valueOf(1);
            }
        } else if (month == 1 && vis.equals("no")) {
            if (day + 1 <= 28) {
                ans += Month[month] + " " + String.valueOf(day + 1);
            } else {
                ans += Month[month + 1] + " " + String.valueOf(1);
            }
        } else if (month < 7 && month % 2 == 0) {
            if (day + 1 <= 31) {
                ans += Month[month] + " " + String.valueOf(day + 1);
            } else {
                ans += Month[month + 1] + " " + String.valueOf(1);
            }
        } else if ((month < 7) && (month % 2 != 0)) {
            if (day + 1 <= 30) {
                ans += Month[month] + " " + String.valueOf(day + 1);
            } else {
                ans += Month[month + 1] + " " + String.valueOf(1);
            }
        } else if (month >= 7 && month % 2 == 0) {
            if (day + 1 <= 30) {
                ans += Month[month] + " " + String.valueOf(day + 1);
            } else {
                ans += Month[month + 1] + " " + String.valueOf(1);
            }
        } else if (month <= 7 && month % 2 != 0) {
            if (day + 1 <= 31) {
                ans += Month[month] + " " + String.valueOf(day + 1);
            } else {
                ans += Month[(month + 1) % 12] + " " + String.valueOf(1);
            }
        }
        return ans;
    }

    public static String dodo(int index) {
        if (index == 2) {
            return "cloudy_sunny";
        }
        if (0 <= index && index <= 3) {
            return "sunny";
        }
        if (60 <= index && index <= 69) {
            return "rainy";
        }
        if (index == 29) {
            return "storm";
        }
        if (index >= 70 && index <= 79) {
            return "snowy";
        }
        return "";
    }
}