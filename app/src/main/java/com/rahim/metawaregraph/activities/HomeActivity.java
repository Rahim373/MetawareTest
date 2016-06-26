package com.rahim.metawaregraph.activities;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.rahim.metawaregraph.R;
import com.rahim.metawaregraph.fragments.DeviceInformationFragment;
import com.rahim.metawaregraph.fragments.SensorsFragment;
import com.rahim.metawaregraph.utils.BluetoothHelper;
import com.rahim.metawaregraph.utils.Board;
import com.rahim.metawaregraph.utils.Preferences;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ServiceConnection {

    private Preferences preferences;
    private MetaWearBleService.LocalBinder serviceBinder;
    private MetaWearBoard metaware;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // My Code
        preferences = Preferences.getInstance(this);
        // Bind the service when the activity is created
        getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),
                this, Context.BIND_AUTO_CREATE);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setCheckedItem(0);
        setTitle(getString(R.string.device_information));
        getFragmentManager().beginTransaction().replace(R.id.mainBox, new DeviceInformationFragment()).commit();

        // Bind the service when the activity is created
     //   getApplicationContext().bindService(new Intent(this, MetaWearBleService.class),this, Context.BIND_AUTO_CREATE);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_disconnect) {
            preferences.setConnected(false);
            if(metaware.isConnected()){
                metaware.disconnect();
                unbindService((ServiceConnection) serviceBinder);
            }
            finish();
            startActivity(new Intent(HomeActivity.this, ScanActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        String title = "Metaware Graph";
        Fragment fragment = null;
        if (id == R.id.nav_home) {
            // Handle the camera action
            title = getString(R.string.device_information);
            fragment = new DeviceInformationFragment();
        } else if (id == R.id.nav_acc_gyro) {
            title = getString(R.string.acc_gyro);
            fragment = new SensorsFragment();
        }

        if(fragment!=null){
            getFragmentManager().beginTransaction().replace(R.id.mainBox, fragment).commit();
            setTitle(title);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }




    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // Typecast the binder to the service's LocalBinder class
        serviceBinder = (MetaWearBleService.LocalBinder) service;
        BluetoothDevice device = BluetoothHelper.getBtDevice(preferences.getMacAddress());
        metaware = serviceBinder.getMetaWearBoard(device);
        Board.getInstance().setMetaWearBoard(metaware);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
    }
}
