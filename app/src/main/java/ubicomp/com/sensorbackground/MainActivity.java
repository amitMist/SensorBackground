package ubicomp.com.sensorbackground;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ubicomp.com.db.entities.SensorRecord;
import ubicomp.com.db.entities.SugarDumpIntentService;

public class MainActivity extends AppCompatActivity{


    private TextView textView;
    private Button startButton,stopButton;
    private EditText activityName;

    LoggerService loggerService=null;
    private String[] mPermissionArrays = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textView = (TextView) findViewById(R.id.tvData);
        startButton = (Button) findViewById(R.id.buttonStart);
        stopButton = (Button) findViewById(R.id.buttonStop);
        activityName = (EditText) findViewById(R.id.activityName);


        stopButton.setEnabled(false);
        startButton.setEnabled(false);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveActivityLabel();
                launchService();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e("Print", "stopButton onClick :");
                terminateService();

                SharedPreferences preferences = getSharedPreferences("ubcicomp.sensor", MODE_PRIVATE);
                String activityTitle = preferences.getString("activityTitle", System.currentTimeMillis() + "");

                SugarDumpIntentService.startActionExport(getApplicationContext(), activityTitle + ".csv", SensorRecord.class);

            }
        });

        checkRunTimePermission();;

    }

    private void checkRunTimePermission() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(mPermissionArrays, 11111);
        } else {
            // if already permition granted
            startButton.setEnabled(true);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean openActivityOnce = true;
        boolean openDialogOnce = true;
        if (requestCode == 11111) {
            boolean isPermitted=false;
            for (int i = 0; i < grantResults.length; i++) {
                String permission = permissions[i];

                isPermitted = grantResults[i] == PackageManager.PERMISSION_GRANTED;

                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // user rejected the permission
                    boolean showRationale = shouldShowRequestPermissionRationale(permission);
                    if (!showRationale) {
                        //execute when 'never Ask Again' tick and permission dialog not show
                    } else {
                        if (openDialogOnce) {
                            alertView();
                        }
                    }
                }
            }

            if (isPermitted){
                startButton.setEnabled(true);
            }

        }
    }
    private void alertView() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        dialog.setTitle("Permission Denied")
                .setInverseBackgroundForced(true)
                .setMessage("Without those permission the app is unable to collect & save data.Are you sure you want to deny this permission?")

                .setNegativeButton("I'M SURE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                    }
                })
                .setPositiveButton("RE-TRY", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialoginterface, int i) {
                        dialoginterface.dismiss();
                        checkRunTimePermission();

                    }
                }).show();
    }

    private void launchService(){
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        SensorRecord.deleteAll(SensorRecord.class);
        Intent intent = new Intent(getApplicationContext(), LoggerService.class);
        startService(intent);

    }

    private void terminateService(){
        Intent intent = new Intent(getApplicationContext(), LoggerService.class);
        stopService(intent);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        activityName.setEnabled(true);
    }


    private void saveActivityLabel(){
        SharedPreferences preferences = getSharedPreferences("ubcicomp.sensor",MODE_PRIVATE);
        SharedPreferences.Editor editor=  preferences.edit();
        String name= activityName.getText().toString()+"-"+System.currentTimeMillis();
        editor.putString("activityTitle", name);

        Log.e("amit_ubicomp", activityName.getText().toString());
        editor.commit();
        activityName.setText(name);
        activityName.setEnabled(false);
    }

    @Override
    protected void onDestroy() {


        super.onDestroy();
    }


}
