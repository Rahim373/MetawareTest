package com.rahim.metawaregraph.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.rahim.metawaregraph.R;
import com.rahim.metawaregraph.utils.Preferences;

/**
 * A simple {@link Fragment} subclass.
 */
public class SensorsFragment extends Fragment {

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

    public SensorsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_sensors, container, false);

        accelerationSpinner = (Spinner) view.findViewById(R.id.acceleration_spinner);
        rotationRangeSpinner = (Spinner) view.findViewById(R.id.gyro_rotation);
        samplingTime = (SeekBar) view.findViewById(R.id.samplingSeekBar);
        samplingTimeTV = (TextView) view.findViewById(R.id.frequencyTV);
        captureSwitch = (Switch) view.findViewById(R.id.captureSwitch);

        accRangeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, acc_range_display_data);
        gyroRangeAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, gyro_range_display_data);

        preferences = Preferences.getInstance(getActivity());

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
                Toast.makeText(getActivity(), samplingTimeValue + " Seconds", Toast.LENGTH_SHORT).show();
            }
        });

        captureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    preferences.setCaptureState(true);
                    // Sensor task
                }else{
                    // Sensor off
                }
            }
        });
        return view;
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

}
