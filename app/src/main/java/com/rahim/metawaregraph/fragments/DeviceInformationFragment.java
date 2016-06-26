package com.rahim.metawaregraph.fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.module.Settings;
import com.rahim.metawaregraph.R;
import com.rahim.metawaregraph.utils.Board;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceInformationFragment extends Fragment {
    private MetaWearBoard metaWearBoard;
    private TextView batteryLevelTV;


    public DeviceInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_information, container, false);
        metaWearBoard = Board.getInstance().getMetaWearBoard();


        batteryLevelTV = (TextView) view.findViewById(R.id.batteryLevelTV);

        Settings settingsModule= null;
        try {
            settingsModule = metaWearBoard.getModule(Settings.class);
        } catch (UnsupportedModuleException e) {
            e.printStackTrace();
        }

        settingsModule.routeData().fromBattery().stream("battery_state").commit()
                .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                    @Override
                    public void success(RouteManager result) {
                        result.subscribe("battery_state", new RouteManager.MessageHandler() {
                            @Override
                            public void process(Message msg) {
                                batteryLevelTV.setText(msg.getData(Settings.BatteryState.class) + "");
                            }
                        });
                    }
                });
        settingsModule.readBatteryState();
        return view;
    }

}
