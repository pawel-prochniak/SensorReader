package com.example.pprochniak.sensorreader.GATT;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.pprochniak.sensorreader.GATT.operations.GattCharacteristicReadOperation;
import com.example.pprochniak.sensorreader.GATT.operations.GattDescriptorReadOperation;
import com.example.pprochniak.sensorreader.GATT.operations.GattOperation;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Henny on 2017-03-29.
 */

@EBean(scope = EBean.Scope.Singleton)
public class GattController {
    private static final String TAG = GattController.class.getSimpleName();

    @RootContext Context context;
    
    private ConcurrentLinkedQueue<GattOperation> mQueue;
    private ConcurrentHashMap<String, BluetoothGatt> mGatts;
    private GattOperation mCurrentOperation;
    private HashMap<UUID, ArrayList<CharacteristicChangeListener>> mCharacteristicChangeListeners;
    private AsyncTask<Void, Void, Void> mCurrentOperationTimeout;
    
    public GattController() {
        mQueue = new ConcurrentLinkedQueue<>();
        mGatts = new ConcurrentHashMap<>();
        mCurrentOperation = null;
        mCharacteristicChangeListeners = new HashMap<>();
    }

    public synchronized void cancelCurrentOperationBundle() {
        Log.d(TAG, "Cancelling current operation. Queue size before: " + mQueue.size());
        if(mCurrentOperation != null && mCurrentOperation.getBundle() != null) {
            for(GattOperation op : mCurrentOperation.getBundle().getOperations()) {
                mQueue.remove(op);
            }
        }
        Log.d(TAG, "Queue size after: " + mQueue.size());
        mCurrentOperation = null;
        drive();
    }

    public synchronized void queue(GattOperation gattOperation) {
        mQueue.add(gattOperation);
        Log.d(TAG, "Queueing Gatt operation, size will now become: " + mQueue.size());
        drive();
    }

    private synchronized void drive() {
        if(mCurrentOperation != null) {
            Log.e(TAG, "tried to drive, but currentOperation was not null, " + mCurrentOperation);
            return;
        }
        if( mQueue.size() == 0) {
            Log.d(TAG, "Queue empty, drive loop stopped.");
            mCurrentOperation = null;
            return;
        }

        final GattOperation operation = mQueue.poll();
        Log.d(TAG, "Driving Gatt queue, size will now become: " + mQueue.size());
        setCurrentOperation(operation);


        if(mCurrentOperationTimeout != null) {
            mCurrentOperationTimeout.cancel(true);
        }
        mCurrentOperationTimeout = new AsyncTask<Void, Void, Void>() {
            @Override
            protected synchronized Void doInBackground(Void... voids) {
                try {
                    Log.d(TAG, "Starting to do a background timeout");
                    wait(operation.getTimoutInMillis());
                } catch (InterruptedException e) {
                    Log.d(TAG, "was interrupted out of the timeout");
                }
                if(isCancelled()) {
                    Log.d(TAG, "The timeout was cancelled, so we do nothing.");
                    return null;
                }
                Log.d(TAG, "Timeout ran to completion, time to cancel the entire operation bundle. Abort, abort!");
                cancelCurrentOperationBundle();
                return null;
            }

            @Override
            protected synchronized void onCancelled() {
                super.onCancelled();
                notify();
            }
        }.execute();

        final BluetoothDevice device = operation.getDevice();
        if(mGatts.containsKey(device.getAddress())) {
            execute(mGatts.get(device.getAddress()), operation);
        } else {
            device.connectGatt(context, true, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);

//                    EventBus.postEvent(Trigger.TRIGGER_CONNECTION_STATE_CHANGED,
//                            new ConnectionStateChangedBundle(
//                                    device.getAddress(),
//                                    newState));

                    if (status == 133) {
                        Log.e(TAG, "Got the status 133 bug, closing gatt");
                        gatt.close();
                        mGatts.remove(device.getAddress());
                        return;
                    }

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "Gatt connected to device " + device.getAddress());
                        mGatts.put(device.getAddress(), gatt);
                        gatt.discoverServices();
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.i(TAG, "Disconnected from gatt server " + device.getAddress() + ", newState: " + newState);
                        mGatts.remove(device.getAddress());
                        setCurrentOperation(null);
                        gatt.close();
                        drive();
                    }
                }

                @Override
                public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);
                    ((GattDescriptorReadOperation) mCurrentOperation).onRead(descriptor);
                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorWrite(gatt, descriptor, status);
                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicRead(gatt, characteristic, status);
                    ((GattCharacteristicReadOperation) mCurrentOperation).onRead(characteristic);
                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    super.onServicesDiscovered(gatt, status);
                    Log.d(TAG, "services discovered, status: " + status);
                    execute(gatt, operation);
                }


                @Override
                public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    super.onCharacteristicWrite(gatt, characteristic, status);
                    Log.d(TAG, "Characteristic " + characteristic.getUuid() + "written to on device " + device.getAddress());
                    setCurrentOperation(null);
                    drive();
                }

                @Override
                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    super.onCharacteristicChanged(gatt, characteristic);
                    Log.e(TAG, "Characteristic " + characteristic.getUuid() + "was changed, device: " + device.getAddress());
                    if (mCharacteristicChangeListeners.containsKey(characteristic.getUuid())) {
                        for (CharacteristicChangeListener listener : mCharacteristicChangeListeners.get(characteristic.getUuid())) {
                            listener.onCharacteristicChanged(device.getAddress(), characteristic);
                        }
                    }
                }
            });
        }
    }

    private void execute(BluetoothGatt gatt, GattOperation operation) {
        if(operation != mCurrentOperation) {
            return;
        }
        operation.execute(gatt);
        if(!operation.hasAvailableCompletionCallback()) {
            setCurrentOperation(null);
            drive();
        }
    }

    public synchronized void setCurrentOperation(GattOperation currentOperation) {
        mCurrentOperation = currentOperation;
    }

    public BluetoothGatt getGatt(BluetoothDevice device) {
        return mGatts.get(device);
    }

    public void addCharacteristicChangeListener(UUID characteristicUuid, CharacteristicChangeListener characteristicChangeListener) {
        if(!mCharacteristicChangeListeners.containsKey(characteristicUuid)) {
            mCharacteristicChangeListeners.put(characteristicUuid, new ArrayList<CharacteristicChangeListener>());
        }
        mCharacteristicChangeListeners.get(characteristicUuid).add(characteristicChangeListener);
    }

    public void queue(GattOperationBundle bundle) {
        for(GattOperation operation : bundle.getOperations()) {
            queue(operation);
        }
    }

    public class ConnectionStateChangedBundle {
        public final int mNewState;
        public final String mAddress;

        public ConnectionStateChangedBundle(String address, int newState) {
            mAddress = address;
            mNewState = newState;
        }
    }
}
