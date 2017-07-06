//package com.example.pprochniak.sensorreader.GATT.adapters;
//
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattService;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//
//import com.example.pprochniak.sensorreader.R;
//import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
//import com.example.pprochniak.sensorreader.utils.Constants;
//import com.example.pprochniak.sensorreader.utils.GattAttributes;
//import com.example.pprochniak.sensorreader.utils.Logger;
//
//import java.util.List;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//
///**
// * Created by Henny on 2017-04-02.
// */
//
//public class BatteryLevelItem extends RecyclerView.ViewHolder implements ServiceListItem {
//    private static final String TAG = "BatteryLevelItem";
//
//    @BindView(R.id.battery_level) TextView batteryLevelTV;
//    @BindView(R.id.battery_level_read_button) Button readButton;
//
//    // Service and characteristics
//    private BluetoothGattService mService;
//    private int serviceInstanceId;
//    private BluetoothGattCharacteristic mReadCharacteristic;
//    private BluetoothGattCharacteristic mNotifyCharacteristic;
//    private Boolean mNotifyCharacteristicEnabled = false;
//
//
//    /**
//     * Preparing Broadcast receiver to broadcast read characteristics
//     *
//     * @param deviceAddress
//     * @param gattCharacteristic
//     */
//    void prepareBroadcastDataRead(String deviceAddress, BluetoothGattCharacteristic gattCharacteristic) {
//        if (checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(),
//                BluetoothGattCharacteristic.PROPERTY_READ)) {
//            BluetoothLeService.readCharacteristic(deviceAddress, gattCharacteristic);
//        }
//    }
//
//    public BatteryLevelItem(ViewGroup parent) {
//        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.service_item_battery, parent, false));
//        ButterKnife.bind(this, itemView);
//    }
//
//    @Override
//    public void bind(BluetoothGattService service, int instanceId) {
//        mService = service;
//        serviceInstanceId = instanceId;
//        getGattData();
//        readButton.setOnClickListener((v) -> {
//            if (mReadCharacteristic != null) {
//                int charactId = mReadCharacteristic.getInstanceId();
//                Logger.d("onClick for characteristics \nid:" + charactId
//                        + "\nservice id:"+mReadCharacteristic.getService().getInstanceId()
//                        + "\n expected service id:"+serviceInstanceId);
//                prepareBroadcastDataRead(mReadCharacteristic);
//            }
//        });
//    }
//
//    @Override
//    public void updateItem(Intent intent) {
//        final String action = intent.getAction();
//        Bundle extras = intent.getExtras();
//        // GATT Data available
//        if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//            int broadcastServiceInstanceId = extras.getInt(Constants.EXTRA_BYTE_SERVICE_INSTANCE_VALUE, -1);
//            boolean constainsBtl = extras.containsKey(Constants.EXTRA_BTL_VALUE);
//            Logger.d(TAG, "Received intent of instance: "+broadcastServiceInstanceId);
//            // Check for battery information
//            if (broadcastServiceInstanceId == serviceInstanceId && constainsBtl) {
//                String received_btl_data = intent
//                        .getStringExtra(Constants.EXTRA_BTL_VALUE);
//                Logger.i("received_btl_data " + received_btl_data);
//                if (!received_btl_data.equalsIgnoreCase(" ")) {
//                    setBatteryLevel(received_btl_data);
//                }
//            }
//        }
//    }
//
//    private void setBatteryLevel(String level) {
//        batteryLevelTV.setText(level);
//    }
//
//    /**
//     * Method to get required characteristics from service
//     */
//    void getGattData() {
//        List<BluetoothGattCharacteristic> gattCharacteristics = mService
//                .getCharacteristics();
//        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//            String uuidchara = gattCharacteristic.getUuid().toString();
//            if (uuidchara.equalsIgnoreCase(GattAttributes.BATTERY_LEVEL)) {
//                Logger.d(TAG, "Characteristics Instance id: " + gattCharacteristic.getInstanceId()
//                        + "\nService id: "+ mService.getInstanceId()
//                        + "\nExpected service id: " + serviceInstanceId);
//                mReadCharacteristic = gattCharacteristic;
//                mNotifyCharacteristic = gattCharacteristic;
//
//                /**
//                 * Checking the various GattCharacteristics and listing in the ListView
//                 */
//                if (checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(),
//                        BluetoothGattCharacteristic.PROPERTY_READ)) {
//                    readButton.setVisibility(View.VISIBLE);
//                }
////                if (checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(),
////                        BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
////                    mNotifyButton.setVisibility(View.VISIBLE);
////                }
//                prepareBroadcastDataRead(gattCharacteristic);
//                break;
//            }
//        }
//    }
//
//    // Return the properties of mGattCharacteristics
//    boolean checkCharacteristicsPropertyPresence(int characteristics,
//                                                 int characteristicsSearch) {
//        return (characteristics & characteristicsSearch) == characteristicsSearch;
//    }
//
//    /**
//     * Preparing Broadcast receiver to broadcast notify characteristics
//     *
//     * @param gattCharacteristic
//     */
//    void prepareBroadcastDataNotify(
//            BluetoothGattCharacteristic gattCharacteristic) {
//        Logger.i("Notify called");
//        if (checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(),
//                BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
//            BluetoothLeService.setCharacteristicNotification(gattCharacteristic,
//                    true);
//        }
//    }
//
//    /**
//     * Stopping Broadcast receiver to broadcast notify characteristics
//     *
//     * @param gattCharacteristic
//     */
//    void stopBroadcastDataNotify(
//            BluetoothGattCharacteristic gattCharacteristic) {
//        if (checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(),
//                BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
//            if (gattCharacteristic != null) {
//                BluetoothLeService.setCharacteristicNotification(
//                        gattCharacteristic, false);
//            }
//
//        }
//
//    }
//
//}
