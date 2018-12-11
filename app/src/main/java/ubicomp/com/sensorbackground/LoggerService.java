package ubicomp.com.sensorbackground;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.orm.SugarRecord;

import java.util.ArrayList;
import java.util.List;

import ubicomp.com.db.entities.SensorRecord;

/**
 * Created by amit on 3/14/16.
 */
public class LoggerService extends Service implements SensorEventListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private static final String DEBUG_TAG = "BaroLoggerService";
    private static final long BACKGROUND_INTERVAL = 20 * 1000; // in millis

    private SensorManager sensorManager = null;
    private Sensor sensorAccelerometer = null;
    private Sensor sensorGyroscope = null;

    private List<SensorRecord> sensorRecords = new ArrayList<SensorRecord>();


    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    SharedPreferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        mPreferences = getSharedPreferences("ubcicomp.sensor", MODE_PRIVATE);
        buildGoogleApiClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, sensorAccelerometer,SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorGyroscope,SensorManager.SENSOR_DELAY_NORMAL);


        return START_STICKY;
    }

    private void buildGoogleApiClient(){
        //show error dialog if GoolglePlayServices not available
        if (isGooglePlayServicesAvailable()) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(BACKGROUND_INTERVAL);
            mLocationRequest.setFastestInterval(BACKGROUND_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            //mLocationRequest.setSmallestDisplacement(10.0f);  /* min dist for location change, here it is 10 meter */
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            mGoogleApiClient.connect();
        }
    }
    //Check Google play is available or not
    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        return ConnectionResult.SUCCESS == status;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {

        sensorManager.unregisterListener(this,sensorAccelerometer);
        sensorManager.unregisterListener(this,sensorGyroscope);
        mGoogleApiClient.disconnect();

        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // grab the values and timestamp



        String activityTitle= mPreferences.getString("activityTitle", System.currentTimeMillis() + "");

        //Log.e("Print", "acceleroMeterRecord saved :" + event.values[0] + "," + event.values[1] + "," + event.values[2] + "");
        SensorRecord sensorRecord = new SensorRecord (event.values[0]+"", event.values[1]+"", event.values[2]+"", event.timestamp,activityTitle,event.sensor.getStringType());

        sensorRecords.add(sensorRecord);

        saveRecords();
        //acceleroMeterRecord.save();


        //if(acceleroMeterRecord.getId()%500==0){
            //Log.e("Print","acceleroMeterRecord saved :"+acceleroMeterRecord.getId());
        //}

    }

    private void saveRecords(){
        if(sensorRecords.size()==500){
            SugarRecord.saveInTx(sensorRecords);
            sensorRecords.clear();
        }
    }
    private void startLocationUpdates() {
        try {
            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        } catch (IllegalStateException ex) {
            Log.e(DEBUG_TAG,"startLocationUpdates: "+ex.getMessage(),ex);
        }catch (SecurityException ex){
            Log.e(DEBUG_TAG,"startLocationUpdates: "+ex.getMessage(),ex);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(DEBUG_TAG, "onConnected");
        startLocationUpdates();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.d(DEBUG_TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(DEBUG_TAG, "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(DEBUG_TAG, "onLocationChanged");
        String activityTitle= mPreferences.getString("activityTitle", System.currentTimeMillis() + "");
        SensorRecord sensorRecord = new SensorRecord (location.getLatitude()+"", location.getLongitude()+"", location.getAccuracy()+"", location.getTime(),activityTitle,"gps");

        sensorRecords.add(sensorRecord);
    }
}
