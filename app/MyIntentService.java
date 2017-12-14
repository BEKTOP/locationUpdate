package in.androize.pragati.locationapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import static android.content.ContentValues.TAG;

/**
 * Created by kushagra on 10/10/17.
 */

public class MyIntentService extends IntentService  {
    private static final String TAG = "codeoHand";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;

    private String lat;
    private String token;
    private String longitude;



    public MyIntentService() {
        super("service");

    }


    public MyIntentService(String name) {
        super(name);
    }
    // Internal Class

    public class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);


            Log.d("codeoHand",mLastLocation.getLatitude()+" ,"+mLastLocation );
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            lat= String.valueOf(location.getLatitude());
            longitude= String.valueOf(location.getLongitude());
            sendData();
            mLastLocation.set(location);

        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
            if(provider.equals(LocationManager.GPS_PROVIDER )){
//                initializeLocationManager();

            }
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
//            initializeLocationManager();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences(
                "myapp", Context.MODE_PRIVATE);

        token = prefs.getString("token",null);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d("codeoHand", "service intent invoked");

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }

        final Handler handler = new Handler();
        final int delay = 1000*20; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                //do something
                handler.postDelayed(this, delay);

                    Log.d("codeoHand","call google api");
                    callGoogleApi();

            }
        }, delay);
//        handleLocation();



    }

    public void handleLocation() {
//        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) | mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            //invoke location from phone
//            Log.d("codeoHand", "init loc man");
//
//            try {
//                mLocationManager.requestLocationUpdates(
//                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
//                        mLocationListeners[1]);
//
//
//            } catch (java.lang.SecurityException ex) {
//                Log.i(TAG, "fail to request location update, ignore", ex);
//            } catch (IllegalArgumentException ex) {
//                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
//            }
//            try {
//                mLocationManager.requestLocationUpdates(
//                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
//                        mLocationListeners[0]);
//            } catch (java.lang.SecurityException ex) {
//                Log.i(TAG, "fail to request location update, ignore", ex);
//            } catch (IllegalArgumentException ex) {
//                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
//            }
//
//        } else {


            Log.d("codeoHandIntent", "call handler");
            callGoogleApi();
//        }
    }


    public static void requestStoragePermissions(Activity activity, int PERMISSION_REQUEST_CODE) {
        java.lang.String[] perms = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION", "android.permission.READ_PHONE_STATE"};
        ActivityCompat.requestPermissions(activity, perms, PERMISSION_REQUEST_CODE);
    }

    public static boolean isLocationPermissionsAllowed(Activity activity) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            return activity.checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED
                    &&
                    activity.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_GRANTED
                    &&
                    activity.checkSelfPermission("android.permission.READ_PHONE_STATE") == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }


    // Call APIs Methods

    private void sendData() {

        JsonObject object = new JsonObject();

        object.addProperty("entity", "Location");
        object.addProperty("action", "Create");
        object.addProperty("lat", lat);
        object.addProperty("lng", longitude);

        Log.d("codeoHand", object.toString());


        try {

            Ion.with(this)
                    .load("http://web.demoplatform.simplifii.com/api/v1/cards")
                    .setHeader("Authorization", "bearer " + token)
                    .setJsonObjectBody(object)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if (result != null) {
                                try {
                                    JSONObject obj1 = null;
                                    obj1 = new JSONObject(result);
                                    String msg = String.valueOf(obj1.get("msg"));
                                    Toast.makeText(MyIntentService.this, msg, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e1) {
                                    Toast.makeText(MyIntentService.this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                Log.d("codeoHand", result);
                            }
                        }
                    });

            Toast.makeText(this, "result called", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "result exceptiom", Toast.LENGTH_SHORT).show();
            Log.d("codeoHand", "error");
        }
    }

    private void sendData(String lati, String lng) {


        JsonObject object = new JsonObject();

        object.addProperty("entity", "Location");
        object.addProperty("action", "Create");
        object.addProperty("lat", lati);
        object.addProperty("lng", lng);

        if (object.toString() != null) {
            Log.d("codeoHand", object.toString());

        }


        try {
            // String urlstring="10.1.1.1:8090/login.xml";
            Ion.with(this)
                    .load("http://web.demoplatform.simplifii.com/api/v1/cards")
                    .setHeader("Authorization", "bearer " + token)
                    .setJsonObjectBody(object)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if (result != null) {
                                try {
                                    JSONObject obj1 = null;
                                    obj1 = new JSONObject(result);
                                    String msg = String.valueOf(obj1.get("msg"));
                                    Toast.makeText(MyIntentService.this, msg, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e1) {
                                    Toast.makeText(MyIntentService.this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                                }

                                Log.d("codeoHand", result + " vfkhgbk");

                            }

//                            Log.d("codeoHand",e.getMessage()+ "  hjghubgk");
                        }
                    });

            Toast.makeText(this, "result called", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "result exceptiom", Toast.LENGTH_SHORT).show();
            Log.d("codeoHand", "error");
        }
    }


    //call google api to get location
    public void callGoogleApi() {
        Log.d("codeoHand", "get data called");
        int mcc = 0, mnc = 0, cid = 0, lac = 0;

        final TelephonyManager telephony = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephony.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM) {
            final GsmCellLocation location = (GsmCellLocation) telephony.getCellLocation();
            if (location != null) {
                cid = location.getCid();
                lac = location.getLac();
                Log.d("codeoHand", location.getCid() + "   " + location.getLac());

            }
            String networkOperator = telephony.getNetworkOperator();

            if (networkOperator != null) {
                mcc = Integer.parseInt(networkOperator.substring(0, 3));
                mnc = Integer.parseInt(networkOperator.substring(3));
            }
            Log.d("codeoHand", mcc + "  " + mnc);


        }
        JsonObject object = new JsonObject();
        JsonObject jsonArrayObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();


        jsonArrayObject.addProperty("cellId", cid);
        jsonArrayObject.addProperty("locationAreaCode", lac);
        jsonArrayObject.addProperty("mobileCountryCode", mcc);
        jsonArrayObject.addProperty("mobileNetworkCode", mnc);

        jsonArray.add(jsonArrayObject);

        object.add("cellTowers", jsonArray);

        if (jsonArrayObject.toString() != null) {
            Log.d("codeoHand", jsonArrayObject.toString());

        }


        try {
            Ion.with(this)
                    .load("https://www.googleapis.com/geolocation/v1/geolocate?key=AIzaSyBnF1iPIWboObGjMSXcLgZ9sPXsB5HPFHk")
                    .setJsonObjectBody(object)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            if (result != null) {
                                try {
                                    JSONObject obj1 = null;
                                    obj1 = new JSONObject(result);
                                    String msg = String.valueOf(obj1.get("msg"));
                                    Toast.makeText(MyIntentService.this, msg, Toast.LENGTH_SHORT).show();
                                } catch (JSONException e1) {
                                    Toast.makeText(MyIntentService.this, e1.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                Log.d("codeoHand", result);

                            }
                            JSONObject obj = null;
                            try {
                                obj = new JSONObject(result);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            try {
                                JSONObject location = obj.getJSONObject("location");
                                String latit = location.getString("lat");
                                String langi = location.getString("lng");

                                Log.d("codeoHand", latit + "  " + langi);

                                sendData(latit, langi);

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }

                        }
                    });

            Toast.makeText(this, "result called", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "result exceptiom", Toast.LENGTH_SHORT).show();
            Log.d("codeoHand", "error");
        }


    }




}
