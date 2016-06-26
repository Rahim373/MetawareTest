package com.rahim.metawaregraph.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by rahim on 22-Jun-16.
 */
public class Preferences {
    private static final String METAWARE = "metaware";
    private static final String MAC_ADDRESS = "mac_address";
    private static final String CONNECTED = "connected";
    private static final String ACC_RANGE = "acc_range";
    private static final String GYRO_RANGE = "gyro_range";
    private static final String SAMPLING_FREQUENCY_SEC = "sampling_sec";
    private static final String SAMPLING_FREQUENCY_HZ = "sampling_hz";
    private static final String STATE = "state";

    private static Preferences preferences;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Preferences(Context context){
        sharedPreferences = context.getSharedPreferences(METAWARE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static Preferences getInstance(Context context){
        if(preferences==null) preferences = new Preferences(context);
        return preferences;
    }

    public void setMac(String macAddress){
        editor.putString(MAC_ADDRESS, macAddress);
        editor.commit();
    }

    public String getMacAddress(){
        return sharedPreferences.getString(MAC_ADDRESS, "");
    }

    public void setConnected(boolean state){
        editor.putBoolean(CONNECTED, state);
        editor.commit();
    }

    public boolean isConnected(){
        return sharedPreferences.getBoolean(CONNECTED, false);
    }

    public void setAccRange(float accRange) {
        editor.putFloat(ACC_RANGE, accRange);
        editor.commit();
    }

    public float getAccRange(){
        return sharedPreferences.getFloat(ACC_RANGE, 2.0f);
    }


    public void setGyroRange(float gyroRange) {
        editor.putFloat(GYRO_RANGE, gyroRange);
        editor.commit();
    }

    public float getGyroRange(){
        return sharedPreferences.getFloat(GYRO_RANGE, 150.0f);
    }

    public void setSamplingFrequencyinSecond(float samplingTimeValue) {
        editor.putFloat(SAMPLING_FREQUENCY_SEC, samplingTimeValue);
        editor.commit();
    }

    public float getSamplingFrequencyInSecond(){
        return sharedPreferences.getFloat(SAMPLING_FREQUENCY_SEC, 1);
    }

    public void setSamplingFrequencyinHz(int samplingTimeValue) {
        editor.putInt(SAMPLING_FREQUENCY_HZ, samplingTimeValue);
        editor.commit();
    }

    public int getSamplingFrequencyInHz(){
        return sharedPreferences.getInt(SAMPLING_FREQUENCY_HZ, 1);
    }

    public void setCaptureState(boolean state) {
        editor.putBoolean(STATE, state);
        editor.commit();
    }

    public boolean isCapturing(){
        return sharedPreferences.getBoolean(STATE, false);
    }
}
