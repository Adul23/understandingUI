package com.example.myapplication.Activities;

import static android.app.ProgressDialog.show;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
import com.example.myapplication.Adapters.HourlyAdapters;
import com.example.myapplication.Domains.Hourly;
import com.example.myapplication.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    String url;
    TextView txt, time, probrain, windspeed, humidity;
    TextView tempmax, next7;
    ImageView imageView;
    Integer[] code = new Integer[24];
    private RecyclerView.Adapter adapterHourly;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // initialize api
        url = "https://api.open-meteo.com/v1/forecast?latitude=43.2567&longitude=76.9286&hourly=temperature_2m,relative_humidity_2m,rain,snowfall,weather_code,wind_speed_80m&timezone=GMT";
        time = findViewById(R.id.TimeView);
        // for time

        // Get current time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int week = calendar.get(Calendar.DAY_OF_WEEK);
        String[] Week = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};


        String[] Month = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        String month1 = Month[month];
        String week1 = Week[week - 1];
        // Format time as a string
        String currentTime = String.format("%s, %d %s %02d:%02d", week1, day, month1, hour, minute);

        // Display the time
        time.setText(currentTime);
        // for temperature
        txt = findViewById(R.id.textview);
        probrain = findViewById(R.id.probrain);
        windspeed = findViewById(R.id.windvelocity);
        humidity = findViewById(R.id.humiditytxt);
        tempmax = findViewById(R.id.tempmax);
        imageView = findViewById(R.id.imageView);
        String[] temp_arr = new String[6];
        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject temperature = response.getJSONObject("hourly");
                            String temp2 = temperature.getJSONArray("temperature_2m").getString(Math.abs(hour - 6));

                            String rain2 = temperature.getJSONArray("rain").getString(Math.abs(hour - 5));
                            String windspeed2 = temperature.getJSONArray("wind_speed_80m").getString(Math.abs(hour + 6));
                            String rel_hum2 = temperature.getJSONArray("relative_humidity_2m").getString(Math.abs(hour - 2));
                            Integer index = temperature.getJSONArray("weather_code").getInt(Math.abs(hour - 2));

                            // int n = temperature.getJSONArray("temperature_2m").length();
                            // int max1 = temperature.getJSONArray("temperature_2m").getInt(0), min1 = temperature.getJSONArray("temperature_2m").getInt(0);
                            // set the highest and lowest throughout the DAY
                            int max1 = temperature.getJSONArray("temperature_2m").getInt(Math.abs(hour - 6));
                            int max2 = 0;
                            int min1 = temperature.getJSONArray("temperature_2m").getInt(Math.abs(hour - 6));
                            for(int i = 0; i < 24; i++){
                                max2 = temperature.getJSONArray("temperature_2m").getInt(Math.abs(i));
                                if (max1 < max2){
                                    max1 = max2;
                                }
                                if (min1 > max2){
                                    min1 = max2;
                                }
                            }
                            tempmax.setText("H: " + String.valueOf(max1) + " L: " + String.valueOf(min1));
                            txt.setText(temp2 + "°C");
                            probrain.setText(rain2 + "%");
                            windspeed.setText(windspeed2 + "km/h");
                            humidity.setText(rel_hum2 + "%");
                            if (0 <= index && index <= 3) {
                                imageView.setImageResource(R.drawable.sunny);
                            }
                            if (60 <= index && index <= 69) {
                                imageView.setImageResource(R.drawable.rainy);
                            }
                            if (index == 29) {
                                imageView.setImageResource(R.drawable.storm);
                            }
                            if (index >= 70 && index <= 79) {
                                imageView.setImageResource(R.drawable.snowy);
                            }
                            ArrayList<Hourly> items = new ArrayList<>();
                            Calendar calendar = Calendar.getInstance();
                            int hour = calendar.get(Calendar.HOUR_OF_DAY);
                            // add pm and am for every item and then make a link to another page
                            String[] hour2 = new String[24];
                            Integer[] temp3 = new Integer[24];
                            for (int i = 0; i < 24; i++) {
                                hour2[i] = maketime(hour + i + 1);
                                temp3[i] = temperature.getJSONArray("temperature_2m").getInt(Math.abs(hour - 6 + i + 1));
                                code[i] = temperature.getJSONArray("weather_code").getInt(Math.abs(hour - 2 + i + 1));


                            }
                            for(int i = 0; i < 24; i++){
                                items.add(new Hourly(hour2[i], temp3[i], dodo(code[i])));
                            }

                            recyclerView = findViewById(R.id.view1);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
                            adapterHourly = new HourlyAdapters(items);
                            recyclerView.setAdapter(adapterHourly);


                            // Toast.makeText(MainActivity.this, String.format("%s", String.valueOf(code)),Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            txt.setText("trying");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        txt.setText("error");
                    }
                });
        next7 = findViewById(R.id.next7);
        next7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
        RequestQueue requestqueue = Volley.newRequestQueue(this);
        requestqueue.add(request);
    }
    public static String dodo(int index){
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
    public static String maketime(int hour) {
        hour = hour % 24;
        if (hour < 12) {
            return String.valueOf(hour) + "am";
        }
        if (hour == 12) {
            return String.valueOf(hour) + "pm";
        }

        return String.valueOf(hour % 12) + "pm";
    }

}