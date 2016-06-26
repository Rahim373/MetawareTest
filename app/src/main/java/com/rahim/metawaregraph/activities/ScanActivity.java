package com.rahim.metawaregraph.activities;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.rahim.metawaregraph.utils.Preferences;
import com.rahim.metawaregraph.R;

import java.util.ArrayList;

public class ScanActivity extends AppCompatActivity implements ServiceConnection {

    private BluetoothAdapter adapter;
    private ArrayList<BluetoothDevice> bluetoothDeviceArrayList;
    private ArrayList<String> displayList;
    private ListView bluetoothList;
    private ArrayAdapter<String> listAdapter;
    private Preferences preferences;
    private BluetoothReceiver receiver;

    public static int REQUEST_BLUETOOTH = 1;
    private MetaWearBleService.LocalBinder serviceBinder;
    private MetaWearBoard metaWearBoard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        preferences = Preferences.getInstance(this);

        adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, REQUEST_BLUETOOTH);
        }

        if(preferences.isConnected() && !preferences.getMacAddress().equalsIgnoreCase("")){
            connectToMetaware(preferences.getMacAddress());
        }

        receiver = new BluetoothReceiver();
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class), this, Context.BIND_AUTO_CREATE);

        bluetoothDeviceArrayList = new ArrayList<>();
        adapter.enable();
        adapter.startDiscovery();

        displayList = new ArrayList<>();
        bluetoothList = (ListView) findViewById(R.id.deviceList);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayList);

        bluetoothDeviceArrayList.addAll(adapter.getBondedDevices());
        bluetoothList.setAdapter(listAdapter);
        bluetoothList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ScanActivity.this, bluetoothDeviceArrayList.get(position).getAddress(), Toast.LENGTH_SHORT).show();
                preferences.setMac(bluetoothDeviceArrayList.get(position).getAddress());
                preferences.setConnected(true);
                finish();
                startActivity(new Intent(ScanActivity.this, HomeActivity.class));

            }
        });
    }


    private void connectToMetaware(final String mac_address){
        final BluetoothDevice device = adapter.getRemoteDevice(mac_address);

        final ProgressDialog dialog = new ProgressDialog(ScanActivity.this);
        dialog.setMessage("Connecting to " + device.getName());
        dialog.setCancelable(false);
        dialog.show();

        metaWearBoard = serviceBinder.getMetaWearBoard(device);

        // If already connected
        if(metaWearBoard.isConnected()){
            dialog.dismiss();
            finish();
            startActivity(new Intent(ScanActivity.this, HomeActivity.class));
        }


        // checking connection state
        metaWearBoard.setConnectionStateHandler(new MetaWearBoard.ConnectionStateHandler() {
            @Override
            public void connected() {
                super.connected();
                Toast.makeText(ScanActivity.this, "Connected to "+ device.getName(), Toast.LENGTH_SHORT).show();
                preferences.setConnected(true);
                preferences.setMac(mac_address);
                dialog.dismiss();
                finish();
                startActivity(new Intent(ScanActivity.this, HomeActivity.class));
            }

            @Override
            public void disconnected() {
                super.disconnected();
                Toast.makeText(ScanActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                metaWearBoard.connect();
            }

            @Override
            public void failure(int status, Throwable error) {
                super.failure(status, error);
                Toast.makeText(ScanActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                metaWearBoard.connect();
            }
        });
        metaWearBoard.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        adapter.enable();
        adapter.startDiscovery();
        bluetoothDeviceArrayList.addAll(adapter.getBondedDevices());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.scan_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_scan:
                adapter.enable();
                adapter.startDiscovery();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }


    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDeviceArrayList.add(device);
                displayList.add(device.getName() + "\n" +device.getAddress());
                device.getName();
                listAdapter.notifyDataSetChanged();
            }
        }
    }
}
