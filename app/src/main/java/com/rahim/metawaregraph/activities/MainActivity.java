package com.rahim.metawaregraph.activities;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.Gyro;
import com.rahim.metawaregraph.R;
import com.rahim.metawaregraph.utils.IOUtils;
import com.rahim.metawaregraph.utils.Preferences;

public class MainActivity extends AppCompatActivity implements ServiceConnection{
    private static final String MW_MAC_ADDRESS = "D4:3F:E6:76:9E:EA", ACCEL_DATA = "accel_data", GYRO_DATA = "gyro_data";
    String ax = "", ay = "", az = "", gx = "", gy = "", gz = "";
    private static final String TAG = "Metaware Data";
    private Spinner accelerationSpinner;
    private Spinner rotationRangeSpinner;
    private SeekBar samplingTime;
    private TextView samplingTimeTV;
    private Switch captureSwitch;

    private float[] acc_range_data = {2.0f, 4.0f, 8.0f, 16.0f};
    private float[] gyro_range_data = {125.0f, 250.0f, 500.0f, 1000.0f, 2000.0f};
    private String[] acc_range_display_data = {"±2g", "±4g", "±8g", "±16g"};
    private String[] gyro_range_display_data = {"125°/s", "250°/s", "500°/s", "1000°/s", "2000°/s"};

    private float samplingTimeValue = 1.000f;

    private ArrayAdapter<String> accRangeAdapter;
    private ArrayAdapter<String> gyroRangeAdapter;

    private Preferences preferences;
    private MetaWearBleService.LocalBinder serviceBinder;
    private MetaWearBoard mwBoard;
    private Accelerometer accelerometer;
    private Gyro gyro;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class), this, Context.BIND_AUTO_CREATE);



        accelerationSpinner = (Spinner) findViewById(R.id.acceleration_spinner);
        rotationRangeSpinner = (Spinner) findViewById(R.id.gyro_rotation);
        samplingTime = (SeekBar) findViewById(R.id.samplingSeekBar);
        samplingTimeTV = (TextView) findViewById(R.id.frequencyTV);
        captureSwitch = (Switch) findViewById(R.id.captureSwitch);

        accRangeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, acc_range_display_data);
        gyroRangeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, gyro_range_display_data);

        preferences = Preferences.getInstance(this);

        accelerationSpinner.setAdapter(accRangeAdapter);
        rotationRangeSpinner.setAdapter(gyroRangeAdapter);

        getSpinnersDataAndLoad();



        // Listeners
        accelerationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float acc_range = acc_range_data[position];
                preferences.setAccRange(acc_range);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        rotationRangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                float gyro_range = gyro_range_data[position];
                preferences.setGyroRange(gyro_range);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        samplingTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateFrequencyAndPrint();
                preferences.setSamplingFrequencyinHz(progress+1);
                preferences.setSamplingFrequencyinSecond(samplingTimeValue);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(MainActivity.this, samplingTimeValue + " Seconds", Toast.LENGTH_SHORT).show();
            }
        });

        captureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    preferences.setCaptureState(true);
                    t1.start();
                }else{
                    // Sensor off
                    accelerometer.stop();
                    gyro.stop();
                }
            }
        });
    }




    private void getSpinnersDataAndLoad() {
        int index1 = 0;
        float acc_data = preferences.getAccRange();
        for(int i = 0 ; i < acc_range_data.length; i++){
            if(acc_range_data[i] == acc_data){
                index1 = i;
                break;
            }
        }
        accelerationSpinner.setSelection(index1);


        int index2 = 0;
        float gyro_data = preferences.getGyroRange();
        for(int i = 0 ; i < gyro_range_data.length; i++){
            if(gyro_range_data[i] == gyro_data){
                index2 = i;
                break;
            }
        }
        rotationRangeSpinner.setSelection(index2);

        int data = preferences.getSamplingFrequencyInHz();
        samplingTimeTV.setText("("+data + "Hz)");
        samplingTimeValue = (float)1 / data ;
        samplingTime.setProgress(data-1);


        if(preferences.isCapturing()) captureSwitch.setChecked(true);
        else captureSwitch.setChecked(false);
    }

    private void updateFrequencyAndPrint() {
        int data = samplingTime.getProgress()+1;
        samplingTimeTV.setText("("+data + "Hz)");
        samplingTimeValue = 1 / (float)data ;
    }

    Thread t1 = new Thread() {
        public void run() {
            try {
                accelerometer.enableAxisSampling();
                accelerometer.start();
                gyro.start();
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

    };

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;
        connectDevice();
    }


    private void connectDevice() {
            final BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            final BluetoothDevice remoteDevice = btManager.getAdapter().getRemoteDevice("D4:3F:E6:76:9E:EA");

            // Create a MetaWear board object for the Bluetooth Device
            mwBoard = serviceBinder.getMetaWearBoard(remoteDevice);
            mwBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
                @Override
                public void connected() {
                    super.connected();
                    Log.d("Metawae", "Connected");

                    try {
                        accelerometer = mwBoard.getModule(Accelerometer.class);
                        accelerometer.setOutputDataRate(preferences.getAccRange());
                        // Set the measurement range to +/- 4g, or closet valid range
                        accelerometer.setAxisSamplingRange(preferences.getSamplingFrequencyInHz());
                        // enable axis sampling
                        accelerometer.enableAxisSampling();
                        accelerometer.routeData().fromAxes().stream(ACCEL_DATA).commit().onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                            @Override
                            public void success(RouteManager result) {
                                super.success(result);
                                result.subscribe(ACCEL_DATA, new RouteManager.MessageHandler() {
                                    @Override
                                    public void process(Message message) {
                                        Log.d("Metaware", (message.getData(CartesianFloat.class).toString()));
                                        try {
                                            String accelerometerData = message.getData(CartesianFloat.class).toString();
                                            accelerometerData.replace("(", "");
                                            accelerometerData.replace(")", "");
                                            String [] data = accelerometerData.split(",");
                                            ax = data[0];
                                            ay = data[1];
                                            az = data[2];

                                            //    IOUtils.appendData(message.getData(CartesianFloat.class).toString());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }

                            @Override
                            public void failure(Throwable error) {
                                super.failure(error);
                            }
                        });
                    } catch (UnsupportedModuleException e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }

                    try {
                        gyro =mwBoard.getModule(Gyro.class);
                        gyro.setAngularRateRange(preferences.getGyroRange());
                        gyro.setOutputDataRate(preferences.getSamplingFrequencyInHz());
                        gyro.routeData().fromAxes().stream(GYRO_DATA).commit()
                                .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                                    @Override
                                    public void success(RouteManager result) {
                                        result.subscribe(GYRO_DATA, new RouteManager.MessageHandler() {
                                            @Override
                                            public void process(Message message) {
                                                try {
                                                    String gyroData = message.getData(CartesianFloat.class).toString();
                                                    gyroData.replace("(", "");
                                                    gyroData.replace(")", "");
                                                    String [] data = gyroData.split(",");
                                                    gx = data[0];
                                                    gy = data[1];
                                                    gz = data[2];
                                                    IOUtils.appendData(ax+","+ay+","+az+","+gx+","+gy +","+ gz);
                                                } catch (Exception e) {
                                                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });

                    } catch (UnsupportedModuleException e){
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void disconnected() {
                    super.disconnected();
                    Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void failure(int status, Throwable error) {
                    super.failure(status, error);
                }
            });
            mwBoard.connect();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }
}
