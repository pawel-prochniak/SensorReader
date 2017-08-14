/*
 * Copyright Cypress Semiconductor Corporation, 2014-2014-2015 All rights reserved.
 * 
 * This software, associated documentation and materials ("Software") is
 * owned by Cypress Semiconductor Corporation ("Cypress") and is
 * protected by and subject to worldwide patent protection (UnitedStates and foreign), United States copyright laws and international
 * treaty provisions. Therefore, unless otherwise specified in a separate license agreement between you and Cypress, this Software
 * must be treated like any other copyrighted material. Reproduction,
 * modification, translation, compilation, or representation of this
 * Software in any other form (e.g., paper, magnetic, optical, silicon)
 * is prohibited without Cypress's express written permission.
 * 
 * Disclaimer: THIS SOFTWARE IS PROVIDED AS-IS, WITH NO WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * NONINFRINGEMENT, IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE. Cypress reserves the right to make changes
 * to the Software without notice. Cypress does not assume any liability
 * arising out of the application or use of Software or any product or
 * circuit described in the Software. Cypress does not authorize its
 * products for use as critical components in any products where a
 * malfunction or failure may reasonably be expected to result in
 * significant injury or death ("High Risk Product"). By including
 * Cypress's product in a High Risk Product, the manufacturer of such
 * system or application assumes all risk of such use and in doing so
 * indemnifies Cypress against all liability.
 * 
 * Use of this Software may be limited by and subject to the applicable
 * Cypress software license agreement.
 * 
 * 
 */

package com.example.pprochniak.sensorreader.ble;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.pprochniak.sensorreader.R;
import com.example.pprochniak.sensorreader.profileParsers.DescriptorParser;
import com.example.pprochniak.sensorreader.profileParsers.SensorHubParser;
import com.example.pprochniak.sensorreader.utils.Constants;
import com.example.pprochniak.sensorreader.utils.GattAttributes;
import com.example.pprochniak.sensorreader.utils.UUIDDatabase;
import com.example.pprochniak.sensorreader.utils.Utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server
 * hosted on a given BlueTooth LE device.
 */
public class BluetoothLeService extends Service {
    private static final String TAG = "BluetoothLeService";
    /**
     * GATT Status constants
     */
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_CONNECTING =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String ACTION_GATT_CHARACTERISTIC_ERROR =
            "com.example.bluetooth.le.ACTION_GATT_CHARACTERISTIC_ERROR";
    public final static String ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL =
            "com.example.bluetooth.le.ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL";
    public final static String ACTION_PAIR_REQUEST =
            "android.bluetooth.device.action.PAIRING_REQUEST";
    public final static String ACTION_WRITE_COMPLETED =
            "android.bluetooth.device.action.ACTION_WRITE_COMPLETED";
    public final static String ACTION_WRITE_FAILED =
            "android.bluetooth.device.action.ACTION_WRITE_FAILED";
    public final static String ACTION_WRITE_SUCCESS =
            "android.bluetooth.device.action.ACTION_WRITE_SUCCESS";
    public final static String DEVICE_ID = "android.bluetooth.device.ADDRESS";

    private final static String ACTION_GATT_DISCONNECTING =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTING";
    private final static String ACTION_PAIRING_REQUEST =
            "com.example.bluetooth.le.PAIRING_REQUEST";
    private static final int STATE_BONDED = 5;
     /**
     * BluetoothAdapter for handling connections
     */
    public static BluetoothAdapter mBluetoothAdapter;

    /**
     * Disable?enable notification
     */
    public static HashMap<String, List<BluetoothGattCharacteristic>> mEnabledCharacteristics =
            new HashMap<>();

    public static boolean mDisableNotificationFlag = false;

    private static int readingCharacteristicCount = 0;

    /**
     * Map of device address to Gatt
     */
    private static HashMap<String, BluetoothGatt> mGattDevices = new HashMap<>();
    private static Context mContext;

    /**
     * Implements callback methods for GATT events that the app cares about. For
     * example,connection change and services discovered.
     */
    private final static BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {

            Log.i(TAG, "onConnectionStateChange");
            String intentAction;
            BluetoothDevice device = gatt.getDevice();
            String deviceAddress = device.getAddress();
            // GATT Server connected
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mGattDevices.put(deviceAddress, gatt);
                broadcastConnectionUpdate(intentAction, deviceAddress);
            }
            // GATT Server disconnected
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mGattDevices.remove(deviceAddress);
                if (mEnabledCharacteristics.containsKey(deviceAddress)) {
                    mEnabledCharacteristics.remove(deviceAddress);
                }
                broadcastConnectionUpdate(intentAction, deviceAddress);
            }
            // GATT Server Connecting
            if (newState == BluetoothProfile.STATE_CONNECTING) {
                intentAction = ACTION_GATT_CONNECTING;
                broadcastConnectionUpdate(intentAction, deviceAddress);
            }
            // GATT Server disconnected
            else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                intentAction = ACTION_GATT_DISCONNECTING;
                broadcastConnectionUpdate(intentAction, deviceAddress);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            // GATT Services discovered
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastConnectionUpdate(ACTION_GATT_SERVICES_DISCOVERED,
                        gatt.getDevice().getAddress());
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION ||
                    status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {
                bondDevice(gatt.getDevice().getAddress());
                broadcastConnectionUpdate(ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL,
                        gatt.getDevice().getAddress());
            } else {
                broadcastConnectionUpdate(ACTION_GATT_SERVICE_DISCOVERY_UNSUCCESSFUL,
                        gatt.getDevice().getAddress());
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                      int status) {
            String serviceUUID = descriptor.getCharacteristic().getService().getUuid().toString();
            String serviceName = GattAttributes.lookupUUID(descriptor.getCharacteristic().
                    getService().getUuid(), serviceUUID);

            String characteristicUUID = descriptor.getCharacteristic().getUuid().toString();
            String characteristicName = GattAttributes.lookupUUID(descriptor.getCharacteristic().
                    getUuid(), characteristicUUID);

            String descriptorUUID = descriptor.getUuid().toString();
            String descriptorName = GattAttributes.lookupUUID(descriptor.getUuid(), descriptorUUID);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Intent intent = new Intent(ACTION_WRITE_SUCCESS);
                mContext.sendBroadcast(intent);
                if (descriptor.getValue() != null)
                    addRemoveData(gatt.getDevice().getAddress(), descriptor);
                if (mDisableNotificationFlag) {
                    disableAllEnabledCharacteristics();
                }
            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
                    || status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {
                bondDevice(gatt.getDevice().getAddress());
                Intent intent = new Intent(ACTION_WRITE_FAILED);
                mContext.sendBroadcast(intent);
            } else {
                mDisableNotificationFlag = false;
                Intent intent = new Intent(ACTION_WRITE_FAILED);
                mContext.sendBroadcast(intent);
            }
        }


        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                     int status) {
            String serviceUUID = descriptor.getCharacteristic().getService().getUuid().toString();
            String serviceName = GattAttributes.lookupUUID(descriptor.getCharacteristic().getService().getUuid(), serviceUUID);
            int serviceInstance = descriptor.getCharacteristic().getService().getInstanceId();

            String characteristicUUID = descriptor.getCharacteristic().getUuid().toString();
            String characteristicName = GattAttributes.lookupUUID(descriptor.getCharacteristic().getUuid(), characteristicUUID);

            String descriptorUUIDText = descriptor.getUuid().toString();
            String descriptorName = GattAttributes.lookupUUID(descriptor.getUuid(), descriptorUUIDText);

            String descriptorValue = " " + Utils.ByteArraytoHex(descriptor.getValue()) + " ";
            if (status == BluetoothGatt.GATT_SUCCESS) {
                UUID descriptorUUID = descriptor.getUuid();
                final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
                Bundle mBundle = new Bundle();
                // Putting the byte value read for GATT Db
                mBundle.putByteArray(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE,
                        descriptor.getValue());
                mBundle.putInt(Constants.EXTRA_BYTE_DESCRIPTOR_INSTANCE_VALUE,
                        descriptor.getCharacteristic().getInstanceId());
                mBundle.putString(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE_UUID,
                        descriptor.getUuid().toString());
                mBundle.putString(Constants.EXTRA_DESCRIPTOR_BYTE_VALUE_CHARACTERISTIC_UUID,
                        descriptor.getCharacteristic().getUuid().toString());
                if (descriptorUUID.equals(UUIDDatabase.UUID_CLIENT_CHARACTERISTIC_CONFIG)) {
                    String valueReceived = DescriptorParser
                            .getClientCharacteristicConfiguration(descriptor, mContext);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, valueReceived);
                } else if (descriptorUUID.equals(UUIDDatabase.UUID_CHARACTERISTIC_EXTENDED_PROPERTIES)) {
                    HashMap<String, String> receivedValuesMap = DescriptorParser
                            .getCharacteristicExtendedProperties(descriptor, mContext);
                    String reliableWriteStatus = receivedValuesMap.get(Constants.FIRST_BIT_KEY_VALUE);
                    String writeAuxillaryStatus = receivedValuesMap.get(Constants.SECOND_BIT_KEY_VALUE);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, reliableWriteStatus + "\n"
                            + writeAuxillaryStatus);
                } else if (descriptorUUID.equals(UUIDDatabase.UUID_CHARACTERISTIC_USER_DESCRIPTION)) {
                    String description = DescriptorParser
                            .getCharacteristicUserDescription(descriptor);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, description);
                } else if (descriptorUUID.equals(UUIDDatabase.UUID_SERVER_CHARACTERISTIC_CONFIGURATION)) {
                    String broadcastStatus = DescriptorParser.
                            getServerCharacteristicConfiguration(descriptor, mContext);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, broadcastStatus);
                } else if (descriptorUUID.equals(UUIDDatabase.UUID_REPORT_REFERENCE)) {
                    ArrayList<String> reportReferencealues = DescriptorParser.getReportReference(descriptor);
                    String reportReference;
                    String reportReferenceType;
                    if (reportReferencealues.size() == 2) {
                        reportReference = reportReferencealues.get(0);
                        reportReferenceType = reportReferencealues.get(1);
                        mBundle.putString(Constants.EXTRA_DESCRIPTOR_REPORT_REFERENCE_ID, reportReference);
                        mBundle.putString(Constants.EXTRA_DESCRIPTOR_REPORT_REFERENCE_TYPE, reportReferenceType);
                        mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE, reportReference + "\n" +
                                reportReferenceType);
                    }

                } else if (descriptorUUID.equals(UUIDDatabase.UUID_CHARACTERISTIC_PRESENTATION_FORMAT)) {
                    String value = DescriptorParser.getCharacteristicPresentationFormat(descriptor, mContext);
                    mBundle.putString(Constants.EXTRA_DESCRIPTOR_VALUE,
                            value);
                }
                intent.putExtras(mBundle);
                /**
                 * Sending the broad cast so that it can be received on
                 * registered receivers
                 */

                mContext.sendBroadcast(intent);
            } else {
                Log.e(TAG, "DescriptorRead failed");
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic
                characteristic, int status) {
            String serviceUUID = characteristic.getService().getUuid().toString();
            String serviceName = GattAttributes.lookupUUID(characteristic.getService().getUuid(), serviceUUID);

            String characteristicUUID = characteristic.getUuid().toString();
            String characteristicName = GattAttributes.lookupUUID(characteristic.getUuid(), characteristicUUID);

            String dataLog = "";
            if (status == BluetoothGatt.GATT_SUCCESS) {
                dataLog = mContext.getResources().getString(R.string.dl_commaseparator) +
                        "[" + serviceName + "|" + characteristicName + "] " +
                        mContext.getResources().getString(R.string.dl_characteristic_write_request_status)
                        + mContext.getResources().getString(R.string.dl_status_success);

                //timeStamp("OTA WRITE RESPONSE TIMESTAMP ");

                Log.d(TAG, dataLog);
            } else {
                dataLog = mContext.getResources().getString(R.string.dl_commaseparator) +
                        "[" + serviceName + "|" + characteristicName + "] " +
                        mContext.getResources().getString(R.string.dl_characteristic_write_request_status) +
                        mContext.getResources().
                                getString(R.string.dl_status_failure) + status;
                Intent intent = new Intent(ACTION_GATT_CHARACTERISTIC_ERROR);
                intent.putExtra(Constants.EXTRA_CHARACTERISTIC_ERROR_MESSAGE, "" + status);
                mContext.sendBroadcast(intent);
                Log.d(TAG, dataLog);
            }


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            readingCharacteristicCount--;
            String deviceAddress = gatt.getDevice().getAddress();
            String serviceUUID = characteristic.getService().getUuid().toString();
            String serviceName = GattAttributes.lookupUUID(characteristic.getService().getUuid(), serviceUUID);
            int serviceId = characteristic.getService().getInstanceId();

            String characteristicUUID = characteristic.getUuid().toString();
            String characteristicName = GattAttributes.lookupUUID(characteristic.getUuid(), characteristicUUID);

            String characteristicValue = " " + Utils.ByteArraytoHex(characteristic.getValue()) + " ";
            // GATT Characteristic read
            if (status == BluetoothGatt.GATT_SUCCESS) {
                String dataLog = "Service UUID " +serviceUUID+mContext.getResources().getString(R.string.dl_commaseparator) +
                        "[" + serviceName + ":" + serviceId + "|" + characteristicName + "] " +
                        mContext.getResources().getString(R.string.dl_characteristic_read_response) +
                        mContext.getResources().getString(R.string.dl_commaseparator) +
                        "[" + characteristicValue + "]";
                Log.d(TAG, dataLog);
                broadcastNotifyUpdate(characteristic, gatt.getDevice().getAddress());
            } else {
                String dataLog = mContext.getResources().getString(R.string.dl_commaseparator)
                        + "[" + deviceAddress + "]" +
                        mContext.getResources().getString(R.string.dl_characteristic_read_request_status) +
                        mContext.getResources().
                                getString(R.string.dl_status_failure) + status;
                Log.d(TAG, dataLog);
                if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION
                        || status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {
                    bondDevice(gatt.getDevice().getAddress());
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            String serviceUUID = characteristic.getService().getUuid().toString();
            String serviceName = GattAttributes.lookupUUID(characteristic.getService().getUuid(), serviceUUID);

            String characteristicUUID = characteristic.getUuid().toString();
            String characteristicName = GattAttributes.lookupUUID(characteristic.getUuid(), characteristicUUID);

            String characteristicValue = Utils.ByteArraytoHex(characteristic.getValue());

            broadcastNotifyUpdate(characteristic, gatt.getDevice().getAddress());
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Resources res = mContext.getResources();
            String dataLog = String.format(
                    res.getString(R.string.exchange_mtu_rsp),
                    gatt.getDevice().getAddress(),
                    res.getString(R.string.exchange_mtu),
                    mtu,
                    status);

            Log.d(TAG, dataLog);
        }
    };

    @TargetApi(21)
    public static void exchangeGattMtu(String deviceAddress, int mtu) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        if (gatt == null) {
            Log.e(TAG, "exchangeGattMtu: no device found!");
            return;
        }
        int retry = 5;
        boolean status = false;
        while (!status && retry > 0) {
            status = gatt.requestMtu(mtu);
            retry--;
        }

        Resources res = mContext.getResources();
        String dataLog = String.format(
                res.getString(R.string.exchange_mtu_request),
                deviceAddress,
                res.getString(R.string.exchange_mtu),
                mtu,
                status ? 0x00 : 0x01);

        Log.d(TAG, dataLog);
    }

    public static HashMap<String, BluetoothGatt> getConnectedGattServices() {
        return mGattDevices;
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * BlueTooth manager for handling connections
     */
    private BluetoothManager mBluetoothManager;

    private static void broadcastConnectionUpdate(final String action, final String deviceAddress) {
        Log.i(TAG, "action :" + action);
        final Intent intent = new Intent(action);
        intent.putExtra(Constants.DEVICE_ADDRESS, deviceAddress);
        mContext.sendBroadcast(intent);
    }

    private static void broadcastWriteStatusUpdate(final String action) {
        final Intent intent = new Intent((action));
        mContext.sendBroadcast(intent);
    }

    private static void broadcastNotifyUpdate(final BluetoothGattCharacteristic characteristic, final String deviceAddress) {
        final Intent intent = new Intent(BluetoothLeService.ACTION_DATA_AVAILABLE);
        Bundle mBundle = new Bundle();
        // Putting the byte value read for GATT Db\
        mBundle.putString(Constants.DEVICE_ADDRESS, deviceAddress);
        mBundle.putByteArray(Constants.EXTRA_BYTE_VALUE,
                characteristic.getValue());
        mBundle.putString(Constants.EXTRA_BYTE_UUID_VALUE,
                characteristic.getUuid().toString());
        mBundle.putInt(Constants.EXTRA_BYTE_INSTANCE_VALUE,
                characteristic.getInstanceId());
        mBundle.putString(Constants.EXTRA_BYTE_SERVICE_UUID_VALUE,
                characteristic.getService().getUuid().toString());
        mBundle.putInt(Constants.EXTRA_BYTE_SERVICE_INSTANCE_VALUE,
                characteristic.getService().getInstanceId());

        if (characteristic.getUuid().equals(UUIDDatabase.UUID_ACC_X)) {
            float val = SensorHubParser.getAcceleroMeterXYZReading(characteristic);
            mBundle.putFloat(Constants.EXTRA_ACC_X_VALUE, val);
        } else if (characteristic.getUuid().equals(UUIDDatabase.UUID_ACC_Y)) {
            float val = SensorHubParser.getAcceleroMeterXYZReading(characteristic);
            mBundle.putFloat(Constants.EXTRA_ACC_Y_VALUE, val);
        } else if (characteristic.getUuid().equals(UUIDDatabase.UUID_ACC_Z)) {
            float val = SensorHubParser.getAcceleroMeterXYZReading(characteristic);
            mBundle.putFloat(Constants.EXTRA_ACC_Z_VALUE, val);
        }

        intent.putExtras(mBundle);

        /*
         * Sending the broad cast so that it can be received on registered
         * receivers
         */
        mContext.sendBroadcast(intent);
    }

    /**
     * Connects to the GATT server hosted on the BlueTooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public static void connect(final String address, Context context) {
        Log.d(TAG, "Connecting to device");
        mContext = context;
        if (mBluetoothAdapter == null || address == null) {
            return;
        }

        BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(address);
        if (device == null) {
            return;
        }
        // We want to directly connect to the device, so we are setting the
        // autoConnect parameter to false.
        BluetoothGatt gatt = device.connectGatt(context, false, mGattCallback);
        //Clearing Bluetooth cache before disconnecting to the device
        if (Utils.getBooleanSharedPreference(mContext, Constants.PREF_PAIR_CACHE_STATUS)) {
            //Log.e(TAG, getActivity().getClass().getName() + "Cache cleared on disconnect!");
            BluetoothLeService.refreshDeviceCache(gatt);
        }

        String dataLog = mContext.getResources().getString(R.string.dl_commaseparator)
                + "[" + address + "] " +
                mContext.getResources().getString(R.string.dl_connection_request);
        Log.d(TAG, dataLog);
    }

    /**
     * Reconnect method to connect to already connected device
     */
    public static void reconnect(String deviceAddress) {
        Log.e(TAG, "<--Reconnecting device-->");
        BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(deviceAddress);
        if (device == null) {
            return;
        }
        device.connectGatt(mContext, false, mGattCallback);
        /**
         * Adding data to the data logger
         */
        String dataLog = mContext.getResources().getString(R.string.dl_commaseparator)
                + "[" + deviceAddress + "] " +
                mContext.getResources().getString(R.string.dl_connection_request);
        Log.d(TAG, dataLog);
    }

    /**
     * Reconnect method to connect to already connected device
     */
    public static void reDiscoverServices(String deviceAddress) {
        Log.e(TAG, "<--Rediscovering services-->");
        BluetoothDevice device = mBluetoothAdapter
                .getRemoteDevice(deviceAddress);
        if (device == null) {
            return;
        }
        /**
         * Disconnecting the device
         */
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        if (gatt != null)
            gatt.disconnect();

        device.connectGatt(mContext, false, mGattCallback);
        /**
         * Adding data to the data logger
         */
        String dataLog = mContext.getResources().getString(R.string.dl_commaseparator)
                + "[" + deviceAddress + "] " +
                mContext.getResources().getString(R.string.dl_connection_request);
        Log.d(TAG, dataLog);
    }

    /**
     * Method to clear the device cache
     *
     * @param gatt
     * @return boolean
     */
    public static boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh");
            if (localMethod != null) {
                return (Boolean) localMethod.invoke(localBluetoothGatt);
            }
        } catch (Exception localException) {
            Log.i(TAG, "An exception occured while refreshing device");
        }
        return false;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public static void disconnect(String deviceAddress) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        Log.i(TAG, "disconnect called");
        if (mBluetoothAdapter != null && gatt != null)  {
            BluetoothLeService.refreshDeviceCache(gatt);
            if (mEnabledCharacteristics.containsKey(deviceAddress)) {
                mEnabledCharacteristics.remove(deviceAddress);
            }
            String dataLog = mContext.getResources().getString(R.string.dl_commaseparator)
                    + "[" + deviceAddress + "] " +
                    mContext.getResources().getString(R.string.dl_disconnection_request);
            Log.d(TAG, dataLog);
            close(gatt.getDevice().getAddress());
            broadcastConnectionUpdate(ACTION_GATT_DISCONNECTED, deviceAddress);
        }

    }

    public static void discoverAllServices() {
        for (String device : mGattDevices.keySet()) {
            mGattDevices.get(device).discoverServices();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "discoverAllServices: could not sleep");
            }
        }
    }

    public static void discoverServices(String deviceAddress) {
        // Log.d(TAG, mContext.getResources().getString(R.string.dl_service_discover_request));
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        if (mBluetoothAdapter == null || gatt == null) {
            return;
        } else {
            gatt.discoverServices();
            String dataLog = mContext.getResources().getString(R.string.dl_commaseparator)
                    + "[" + deviceAddress + "] " +
                    mContext.getResources().getString(R.string.dl_service_discovery_request);
            Log.d(TAG, dataLog);
        }
    }

    public static void subscribeToSensorNotifications(String deviceAddress) {
        List<BluetoothGattService> services = BluetoothLeService.getSupportedGattServices(deviceAddress);

        if (services == null) {
            Log.e(TAG, "subscribeToSensorNotifications: no services found");
            return;
        }
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : services) {
            UUID uuid = gattService.getUuid();
            if (UUIDDatabase.UUID_SENSOR_READ_SERVICE.equals(uuid)) {
                // Auto set notify as TRUE
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    if (Utils.checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(), BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
                        BluetoothLeService.setCharacteristicNotification(deviceAddress,gattCharacteristic, true);
                    } else {
                        Log.d(TAG, "subscribeToSensorNotifications: no notify characteristic available");
                    }
                }
            }
        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read
     * result is reported asynchronously through the
     * {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public static void readCharacteristic(String deviceAddress,
            BluetoothGattCharacteristic characteristic) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        String serviceUUID = characteristic.getService().getUuid().toString();
        String serviceName = GattAttributes.lookupUUID(characteristic.getService().getUuid(), serviceUUID);

        String characteristicUUID = characteristic.getUuid().toString();
        String characteristicName = GattAttributes.lookupUUID(characteristic.getUuid(), characteristicUUID);
        if (mBluetoothAdapter == null || gatt == null) {
            return;
        }
        if (readingCharacteristicCount > 0) {
            new Handler().postDelayed((() -> gatt.readCharacteristic(characteristic)), 200) ;
        } else {
            gatt.readCharacteristic(characteristic);
        }
        readingCharacteristicCount++;
        String dataLog = mContext.getResources().getString(R.string.dl_commaseparator) +
                "[" + serviceName + "|" + characteristicName + "] " +
                mContext.getResources().getString(R.string.dl_characteristic_read_request);
        Log.d(TAG, dataLog);
    }

    /**
     * Request a read on a given {@code BluetoothGattDescriptor }.
     *
     * @param descriptor The descriptor to read from.
     */
    public static void readDescriptor(String deviceAddress,
            BluetoothGattDescriptor descriptor) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        String serviceUUID = descriptor.getCharacteristic().getService().getUuid().toString();
        String serviceName = GattAttributes.lookupUUID(descriptor.getCharacteristic().getService().getUuid(), serviceUUID);

        String characteristicUUID = descriptor.getCharacteristic().getUuid().toString();
        String characteristicName = GattAttributes.lookupUUID(descriptor.getCharacteristic().getUuid(), characteristicUUID);
        if (mBluetoothAdapter == null || gatt == null) {
            return;
        }
        //Log.d(TAG, mContext.getResources().getString(R.string.dl_descriptor_read_request));
        gatt.readDescriptor(descriptor);
        String dataLog = mContext.getResources().getString(R.string.dl_commaseparator) +
                "[" + serviceName + "|" + characteristicName + "] " +
                mContext.getResources().getString(R.string.dl_characteristic_read_request);
        Log.d(TAG, dataLog);
    }

    /**
     * Request a write with no response on a given
     * {@code BluetoothGattCharacteristic}.
     *
     * @param characteristic
     * @param byteArray      to write
     */
    public static void writeCharacteristicNoresponse(String deviceAddress,
            BluetoothGattCharacteristic characteristic, byte[] byteArray) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        String serviceUUID = characteristic.getService().getUuid().toString();
        String serviceName = GattAttributes.lookupUUID(characteristic.getService().getUuid(), serviceUUID);

        String characteristicUUID = characteristic.getUuid().toString();
        String characteristicName = GattAttributes.lookupUUID(characteristic.getUuid(), characteristicUUID);

        String characteristicValue = Utils.ByteArraytoHex(byteArray);
        if (mBluetoothAdapter == null || gatt == null) {
            return;
        } else {
            byte[] valueByte = byteArray;
            characteristic.setValue(valueByte);
            gatt.writeCharacteristic(characteristic);
            String dataLog = mContext.getResources().getString(R.string.dl_commaseparator) +
                    "[" + serviceName + "|" + characteristicName + "] " +
                    mContext.getResources().getString(R.string.dl_characteristic_write_request) +
                    mContext.getResources().getString(R.string.dl_commaseparator) +
                    "[ " + characteristicValue + " ]";
            Log.d(TAG, dataLog);

        }
    }


    private static String getHexValue(byte[] array) {
        StringBuffer sb = new StringBuffer();
        for (byte byteChar : array) {
            sb.append(String.format("%02x", byteChar));
        }
        return sb.toString();
    }

    /**
     * Request a write on a given {@code BluetoothGattCharacteristic}.
     *
     * @param characteristic
     * @param byteArray
     */

    public static void writeCharacteristicGattDb(String deviceAddress,
            BluetoothGattCharacteristic characteristic, byte[] byteArray) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        String serviceUUID = characteristic.getService().getUuid().toString();
        String serviceName = GattAttributes.lookupUUID(characteristic.getService().getUuid(), serviceUUID);

        String characteristicUUID = characteristic.getUuid().toString();
        String characteristicName = GattAttributes.lookupUUID(characteristic.getUuid(), characteristicUUID);

        String characteristicValue = Utils.ByteArraytoHex(byteArray);
        if (mBluetoothAdapter == null || gatt == null) {
            return;
        } else {
            byte[] valueByte = byteArray;
            characteristic.setValue(valueByte);
            gatt.writeCharacteristic(characteristic);
            String dataLog = mContext.getResources().getString(R.string.dl_commaseparator) +
                    "[" + serviceName + "|" + characteristicName + "] " +
                    mContext.getResources().getString(R.string.dl_characteristic_write_request) +
                    mContext.getResources().getString(R.string.dl_commaseparator) +
                    "[ " + characteristicValue + " ]";
            Log.d(TAG, dataLog);
        }

    }

    /**
     * Writes the characteristic value to the given characteristic.
     *
     * @param characteristic the characteristic to write to
     * @return true if request has been sent
     */
    public static final boolean writeCharacteristic(String deviceAddress,
                                                    final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        if (gatt == null || characteristic == null)
            return false;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0)
            return false;

        Log.d(TAG, "Writing characteristic " + characteristic.getUuid());
        Log.d(TAG, "gatt.writeCharacteristic(" + characteristic.getUuid() + ")");
        return gatt.writeCharacteristic(characteristic);
    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification. False otherwise.
     */
    public static void setCharacteristicNotification(String deviceAddress,
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);

        String serviceUUID = characteristic.getService().getUuid().toString();
        String serviceName = GattAttributes.lookupUUID(characteristic.getService().getUuid(), serviceUUID);

        String characteristicUUID = characteristic.getUuid().toString();
        String characteristicName = GattAttributes.lookupUUID(characteristic.getUuid(), characteristicUUID);

        String descriptorUUID = GattAttributes.CLIENT_CHARACTERISTIC_CONFIG;
        String descriptorName = GattAttributes.lookupUUID(UUIDDatabase.
                UUID_CLIENT_CHARACTERISTIC_CONFIG, descriptorUUID);
        if (mBluetoothAdapter == null || gatt == null) {
            return;
        }
        if (characteristic.getDescriptor(UUID
                .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG)) != null) {
            if (enabled) {
                BluetoothGattDescriptor descriptor = characteristic
                        .getDescriptor(UUID
                                .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                descriptor
                        .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                Log.d(TAG, "setCharacteristicNotification: service: " + serviceName + " ");
            } else {
                BluetoothGattDescriptor descriptor = characteristic
                        .getDescriptor(UUID
                                .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                descriptor
                        .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
        gatt.setCharacteristicNotification(characteristic, enabled);
        if (enabled) {
            Log.d(TAG, "setCharacteristicNotification: enabled");
        } else {
            Log.d(TAG, "setCharacteristicNotification: disabled");

        }

    }

    /**
     * Enables or disables indications on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable indications. False otherwise.
     */
    public static void setCharacteristicIndication(String deviceAddress,
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        String serviceUUID = characteristic.getService().getUuid().toString();
        String serviceName = GattAttributes.lookupUUID(characteristic.getService().getUuid(),
                serviceUUID);

        String characteristicUUID = characteristic.getUuid().toString();
        String characteristicName = GattAttributes.lookupUUID(characteristic.getUuid(),
                characteristicUUID);

        String descriptorUUID = GattAttributes.CLIENT_CHARACTERISTIC_CONFIG;
        String descriptorName = GattAttributes.lookupUUID(UUIDDatabase.
                UUID_CLIENT_CHARACTERISTIC_CONFIG, descriptorUUID);
        if (mBluetoothAdapter == null || gatt == null) {
            return;
        }

        if (characteristic.getDescriptor(UUID
                .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG)) != null) {
            if (enabled == true) {
                BluetoothGattDescriptor descriptor = characteristic
                        .getDescriptor(UUID
                                .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                descriptor
                        .setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            } else {
                BluetoothGattDescriptor descriptor = characteristic
                        .getDescriptor(UUID
                                .fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
                descriptor
                        .setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }
        gatt.setCharacteristicNotification(characteristic, enabled);
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This
     * should be invoked only after {@code BluetoothGatt#discoverServices()}
     * completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public static List<BluetoothGattService> getSupportedGattServices(String deviceAddress) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        if (gatt == null)
            return null;

        return gatt.getServices();
    }

    public static void bondDevice(String deviceAddress) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        try {
            Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
            Method createBondMethod = class1.getMethod("createBond");
            Boolean returnValue = (Boolean) createBondMethod.invoke(gatt.getDevice());
            Log.e(TAG, "Pair initates status-->" + returnValue);
        } catch (Exception e) {
            Log.e(TAG, "Exception Pair" + e.getMessage());
        }

    }

    public static void addRemoveData(String deviceAddress, BluetoothGattDescriptor descriptor) {
        switch (descriptor.getValue()[0]) {
            case 0:
                //Disabled notification and indication
                removeEnabledCharacteristic(deviceAddress, descriptor.getCharacteristic());
                Log.e(TAG, "Removed characteristic");
                break;
            case 1:
                //Enabled notification
                Log.e(TAG, "added notify characteristic");
                addEnabledCharacteristic(deviceAddress, descriptor.getCharacteristic());
                break;
            case 2:
                //Enabled indication
                Log.e(TAG, "added indicate characteristic");
                addEnabledCharacteristic(deviceAddress, descriptor.getCharacteristic());
                break;
        }
    }

    public static void addEnabledCharacteristic(String deviceAddress,
                                                BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (!mEnabledCharacteristics.containsKey(deviceAddress)) {
            mEnabledCharacteristics.put(deviceAddress, new ArrayList<>());
        }
        List<BluetoothGattCharacteristic> characteristics = mEnabledCharacteristics.get(deviceAddress);
        if (!characteristics.contains(bluetoothGattCharacteristic)) {
            characteristics.add(bluetoothGattCharacteristic);
        }
    }

    public static void removeEnabledCharacteristic(String deviceAddress,
                                                   BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (mEnabledCharacteristics.containsKey(deviceAddress)) {
            List<BluetoothGattCharacteristic> characteristics = mEnabledCharacteristics.get(deviceAddress);
            if (characteristics.contains(bluetoothGattCharacteristic))
                characteristics.remove(bluetoothGattCharacteristic);
        }
    }

    public static void disableAllEnabledCharacteristics() {
        if (mEnabledCharacteristics.size() > 0) {
            Set<String> addresses = mEnabledCharacteristics.keySet();
            for (String address : addresses) {
                for (BluetoothGattCharacteristic characteristic : mEnabledCharacteristics.get(address)) {
                    Log.e(TAG, "Disabling characteristic--" + characteristic.getUuid());
                    setCharacteristicNotification(address, characteristic, false);
                    Timer timer = new Timer();
                    try {
                        timer.wait(20);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "disableAllEnabledCharacteristics: ", e);
                    }
                }
            }
            mDisableNotificationFlag = true;
        } else {
            mDisableNotificationFlag = false;
        }

    }


    /**
     * After using a given BLE device, the app must call this method to ensure
     * resources are released properly.
     */
    public static void close(String deviceAddress) {
        BluetoothGatt gatt = mGattDevices.get(deviceAddress);
        gatt.close();
        mGattDevices.remove(deviceAddress);
    }

    public static void closeAll() {
        Set<String> deviceAddresses = mGattDevices.keySet();
        for (String address : deviceAddresses) {
            close(address);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        closeAll();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local BlueTooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        return mBluetoothAdapter != null;

    }

    @Override
    public void onCreate() {
        // Initializing the service
        if (!initialize()) {
            Log.d(TAG, "Service not initialized");
        }
    }

    /**
     * Local binder class
     */
    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

}