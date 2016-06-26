package com.rahim.metawaregraph.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * Created by rahim on 23-Jun-16.
 */
public class BluetoothHelper {
    public static BluetoothDevice getBtDevice(String address){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = adapter.getRemoteDevice(address);
        return device;

    }
}
