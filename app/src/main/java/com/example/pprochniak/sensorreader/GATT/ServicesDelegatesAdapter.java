package com.example.pprochniak.sensorreader.GATT;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.example.pprochniak.sensorreader.GATT.adapters.BatteryLevelItem;
import com.example.pprochniak.sensorreader.GATT.adapters.ServiceListItem;
import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.utils.Logger;
import com.example.pprochniak.sensorreader.utils.UUIDDatabase;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Henny on 2017-04-02.
 */

public class ServicesDelegatesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {
    private static final String TAG = "ServicesDelegatesAdapter";

    private final int BATTERY_SERVICE = 0;

    private ArrayList<HashMap<String, BluetoothGattService>> serviceList = new ArrayList<>();
    private ArrayList<ServiceListItem> gattListeners = new ArrayList<>();

    BroadcastReceiver getGattUpdateReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                final String action = intent.getAction();
                // GATT Data available
                if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    Logger.i("Data Available");
                    for (ServiceListItem listener : gattListeners) {
                        listener.updateItem(intent);
                    }

                }
//            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
//                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
//
//                if (state == BluetoothDevice.BOND_BONDING) {
//                    // Bonding...
//                    Logger.i("Bonding is in process....");
//                    Utils.bondingProgressDialog(getActivity(), mProgressDialog, true);
//                } else if (state == BluetoothDevice.BOND_BONDED) {
//                    String dataLog = getResources().getString(R.string.dl_commaseparator)
//                            + "[" + BluetoothLeService.getmBluetoothDeviceName() + "|"
//                            + BluetoothLeService.getmBluetoothDeviceAddress() + "]" +
//                            getResources().getString(R.string.dl_commaseparator) +
//                            getResources().getString(R.string.dl_connection_paired);
//                    Logger.datalog(dataLog);
//                    Utils.bondingProgressDialog(getActivity(), mProgressDialog, false);
//                    getGattData();
//
//                } else if (state == BluetoothDevice.BOND_NONE) {
//                    String dataLog = getResources().getString(R.string.dl_commaseparator)
//                            + "[" + BluetoothLeService.getmBluetoothDeviceName() + "|"
//                            + BluetoothLeService.getmBluetoothDeviceAddress() + "]" +
//                            getResources().getString(R.string.dl_commaseparator) +
//                            getResources().getString(R.string.dl_connection_unpaired);
//                    Logger.datalog(dataLog);
//                    Utils.bondingProgressDialog(getActivity(), mProgressDialog, false);
//                }
//            }

            }

        };
    }


    public ServicesDelegatesAdapter() {
        notifyServiceListChanged();
    }

    public void clearList() {
        serviceList.clear();
        notifyDataSetChanged();
    }

    public void notifyServiceListChanged() {
        ArrayList<HashMap<String, BluetoothGattService>> readServices = ServicesFragment.mGattServiceData;
        for (HashMap<String, BluetoothGattService> service : readServices) {
            if (service.get("UUID").getUuid().equals(UUIDDatabase.UUID_BATTERY_SERVICE)) {
                serviceList.add(service);
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case BATTERY_SERVICE:
                return new BatteryLevelItem(parent);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BluetoothGattService gattService = serviceList.get(position).get("UUID");
        if (holder instanceof ServiceListItem) {
            Logger.d(TAG, "Binding view with service instance id: \n" +gattService.getInstanceId());
            ((ServiceListItem)holder).bind(gattService, gattService.getInstanceId());
            registerItemAsGattListener((ServiceListItem)holder);
        } else {
            Logger.e("service list item was not ServiceListItem type");
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (recyclerView instanceof ServiceListItem) {
            if (gattListeners.contains(recyclerView)) {
                gattListeners.remove(recyclerView);
            }
        }
    }

    public void registerItemAsGattListener(ServiceListItem serviceListItem) {
        gattListeners.add(serviceListItem);
    }

    @Override
    public int getItemCount() {
        return serviceList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (UUIDDatabase.UUID_BATTERY_SERVICE.equals(serviceList.get(position).get("UUID").getUuid())) {
            return BATTERY_SERVICE;
        } else return -1;
    }
}
