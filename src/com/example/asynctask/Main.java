package com.example.asynctask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Main extends Activity {
    /**
     * Called when the activity is first created.
     */
    public TextView latitude, longitude, speed;
    LocationManager locationManager;
    Button startBtn;
    Button stopBtn;
    public String flag="false";
    private Task task = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        latitude = (TextView) findViewById(R.id.latitude);
        longitude = (TextView) findViewById(R.id.longitude);
        speed = (TextView) findViewById(R.id.speed);
        startBtn = (Button) findViewById(R.id.btnStart);
        stopBtn = (Button) findViewById(R.id.btnStop);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    if (task == null) {
                        task = new Task();
                        task.execute();
                        flag="true";
                    }
                } else {
                    showSettingsAlert();
                }

                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (task != null) {
                    task.stop();
                    task.cancel(true);
                    task = null;
                    flag="false";
                }
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });


        try {
            if (savedInstanceState == null)
                return;

            String lat = savedInstanceState.getString("lat");
            String longi = savedInstanceState.getString("long");
            String speede = savedInstanceState.getString("speed");
            flag=savedInstanceState.getString("flag");

            latitude.setText(lat);
            longitude.setText(longi);
            speed.setText(speede);

        } catch (Exception e) {
        }
    }


    protected void onSaveInstanceState(Bundle bd) {

        try {
            if (latitude.getText().toString() != "Info Unavailable") {
                bd.putString("lat", latitude.getText().toString());
                bd.putString("long", longitude.getText().toString());
                bd.putString("speed", speed.getText().toString());
                bd.putString("flag",flag);
            }

            super.onSaveInstanceState(bd);
        } catch (Exception e) {

        }
    }


    public void showSettingsAlert() {
        final Context mContext = Main.this;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if((task==null)&&(!flag.contentEquals("false")))
        {
            task=new Task();
            task.execute();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (task != null) {
            task.stop();
            task.cancel(true);
            task = null;
        }
    }

    private class Task extends AsyncTask<Void, Location, Void> {
        private LocationListener locationListener;
        private boolean running = true;
        Double lat = 0.0;
        Double lon = 0.0;
        Double spe = 0.0;
        String result = "";

        @Override
        protected void onPreExecute() {

            locationListener = new MyLocationListener();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, locationListener);
        }

        @Override
        protected Void doInBackground(Void... params) {

            while (running) {


                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(location != null)
                {   lat = location.getLatitude();
                    lon = location.getLongitude();
                    spe = (double)location.getSpeed();
                    this.publishProgress(location);
                    HttpResponse response;

                    ArrayList<NameValuePair> postParams = new ArrayList<NameValuePair>();
                    postParams.add(new BasicNameValuePair("latitude",lat.toString()));
                    postParams.add(new BasicNameValuePair("longitude",lon.toString()));
                    postParams.add(new BasicNameValuePair("speed",spe.toString()));
                    HttpClient client = new DefaultHttpClient();
                    //HttpPost post = new HttpPost("http://192.168.1.78:3000/track/addLocation");
                    HttpPost post = new HttpPost("http://position-notifier.herokuapp.com/track/addLocation");
                    UrlEncodedFormEntity formEntity = null;
                    try {
                        formEntity = new UrlEncodedFormEntity(postParams);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                    post.setEntity(formEntity);


                    try {

                        response = client.execute(post);
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Location... location) {
            if (!running) {
                return;
            }

            if (location[0] != null) {
                latitude.setText(String.valueOf(location[0].getLatitude()));
                longitude.setText(String.valueOf(location[0].getLongitude()));
                speed.setText(String.valueOf(location[0].getSpeed()));

//                lat = location[0].getLatitude();
//                lon = location[0].getLongitude();
//                spe = (double)location[0].getSpeed();

                result = "Your Location is - \nLatitude: " + lat + "\nLongitude: " + lon + "\nSpeed: " + spe;

                File f = new File(Environment.getExternalStorageDirectory() + "/logger.log");
                try {
                    FileWriter fw = new FileWriter(f, true);
                    fw.write(result);
                    fw.write("\n");
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }



        }

        public void stop() {
            running = false;
            if (locationManager != null) {
                locationManager.removeUpdates(locationListener);
            }
        }

        private class MyLocationListener implements LocationListener {

            @Override
            public void onLocationChanged(Location location) {
                //To change body of implemented methods use File | Settings | File Templates.

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onProviderEnabled(String provider) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void onProviderDisabled(String provider) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        }
    }


}
