package com.example.pprochniak.sensorreader.deviceDiscovery;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pprochniak.sensorreader.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Henny on 2017-07-04.
 */

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {
    private static final String TAG = "DeviceListAdapter";

    ArrayList<BluetoothDevice> deviceList;
    Map<String, Integer> rssiList;
    DevicesFragment fragment;


    public DeviceListAdapter(DevicesFragment fragment) {
        this.fragment = fragment;
        deviceList = new ArrayList<>();
        rssiList = new HashMap<>();
    }

    public void addDevice(BluetoothDevice device, int rssi) {
        if (!deviceList.contains(device)) {
            deviceList.add(device);
            rssiList.put(device.getAddress(), rssi);
        } else {
            rssiList.put(device.getAddress(), rssi);
        }
    }

    public void clear() {
        deviceList.clear();
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        final BluetoothDevice device = deviceList.get(position);
        holder.bind(device, rssiList.get(device.getAddress()));
        // TODO show icon with connected device
        holder.itemView.setOnClickListener((view) -> {
                    fragment.scanLeDevice(false);
                    fragment.connectDevice(device, true);
                }
        );
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.device_name) TextView deviceName;
        @BindView(R.id.device_address) TextView deviceAddress;
        @BindView(R.id.device_rssi) TextView deviceRssi;

        DeviceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bind(BluetoothDevice device, int rssi) {
            deviceName.setText(device.getName());
            deviceAddress.setText(device.getAddress());
            deviceRssi.setText(String.valueOf(rssi));
        }

    }
}