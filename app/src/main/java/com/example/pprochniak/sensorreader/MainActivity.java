package com.example.pprochniak.sensorreader;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.pprochniak.sensorreader.GATT.ServicesFragment;
import com.example.pprochniak.sensorreader.ble.BluetoothLeService;
import com.example.pprochniak.sensorreader.deviceDiscovery.DevicesFragment;
import com.example.pprochniak.sensorreader.deviceDiscovery.DevicesFragment_;
import com.example.pprochniak.sensorreader.utils.Constants;
import com.example.pprochniak.sensorreader.utils.DrawerController;
import com.example.pprochniak.sensorreader.utils.Logger;
import com.example.pprochniak.sensorreader.utils.Utils;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

@EActivity
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    public static boolean isAppInBackground = false;

    String attachmentFileName = "attachment.sensorread";
    //Upgrade file catch
    private InputStream attachment = null;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_STORAGE = 2;

    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.drawer) ListView drawerList;
    @Bean DrawerController drawerController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupDrawer();

        DevicesFragment devicesFragment = new DevicesFragment_();

        // start BLE service
        Intent gattServiceIntent = new Intent(getApplicationContext(),
                BluetoothLeService.class);
        startService(gattServiceIntent);

        // set start fragment
        drawerController.setFragment(DrawerController.DEVICES_FRAGMENT);

        checkPermissions();

    }

    @Override
    protected void onResume() {
        try {
            catchUpgradeFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        isAppInBackground = false;

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_PAIR_REQUEST);
        registerReceiver(mBondStateReceiver, intentFilter);

        super.onResume();
    }

    @Override
    protected void onPause() {
        getIntent().setData(null);
        // Getting the current active fragment
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.main_container);
        if (currentFragment instanceof DevicesFragment) {
            Intent gattServiceIntent = new Intent(getApplicationContext(),
                    BluetoothLeService.class);
            stopService(gattServiceIntent);
        }
        unregisterReceiver(mBondStateReceiver);
        isAppInBackground = true;
        super.onPause();
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Logger.v("newIntent");
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onBackPressed() {
        // Getting the current active fragment
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.main_container);

        if (currentFragment instanceof ServicesFragment) {
            if (BluetoothLeService.getConnectionState() == 2 ||
                    BluetoothLeService.getConnectionState() == 1 ||
                    BluetoothLeService.getConnectionState() == 4) {
                BluetoothLeService.disconnect();
                Toast.makeText(this,
                        getResources().getString(R.string.alert_message_bluetooth_disconnect),
                        Toast.LENGTH_SHORT).show();

                Intent intent = getIntent();
                finish();
                startActivity(intent);
                super.onBackPressed();
            }
        } else super.onBackPressed();

    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check location permission (needed for BLE)
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
            // Check if we have write permission
            int writePermission = this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                // We don't have permission so prompt the user
                requestPermissions(new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_STORAGE
                );
            }
        }
    }

    private void setupDrawer() {
        drawerController.setupDrawer(drawerLayout, drawerList);
    }

    /**
     * Broadcast receiver for getting the bonding information
     */
    private BroadcastReceiver mBondStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //Received when the bond state is changed
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

                if (state == BluetoothDevice.BOND_BONDING) {
                    // Bonding...
                    String dataLog2 = getResources().getString(R.string.dl_commaseparator)
                            + "[" + DevicesFragment.mDeviceName + "|"
                            + DevicesFragment.mDeviceAddress + "] " +
                            getResources().getString(R.string.dl_connection_pairing_request);
                    Logger.datalog(dataLog2);

                } else if (state == BluetoothDevice.BOND_BONDED) {
                    Logger.e("HomepageActivity--->Bonded");
                    Utils.stopDialogTimer();
                    // Bonded...

                    String dataLog = getResources().getString(R.string.dl_commaseparator)
                            + "[" + DevicesFragment.mDeviceName + "|"
                            + DevicesFragment.mDeviceAddress + "] " +
                            getResources().getString(R.string.dl_connection_paired);
                    Logger.datalog(dataLog);

                } else if (state == BluetoothDevice.BOND_NONE) {
                    // Not bonded...
                    Logger.e("HomepageActivity--->Not Bonded");
                    Utils.stopDialogTimer();

                    String dataLog = getResources().getString(R.string.dl_commaseparator)
                            + "[" + DevicesFragment.mDeviceName + "|"
                            + DevicesFragment.mDeviceAddress + "] " +
                            getResources().getString(R.string.dl_connection_pairing_unsupported);
                    Logger.datalog(dataLog);
                } else {
                    Logger.e("Error received in pair-->" + state);
                }
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Logger.i("BluetoothAdapter.ACTION_STATE_CHANGED.");
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) ==
                        BluetoothAdapter.STATE_OFF) {
                    Logger.i("BluetoothAdapter.STATE_OFF");


                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) ==
                        BluetoothAdapter.STATE_ON) {
                    Logger.i("BluetoothAdapter.STATE_ON");

                }

            } else if (action.equals(BluetoothLeService.ACTION_PAIR_REQUEST)) {
                Logger.e("Pair request received");
                Logger.e("HomepageActivity--->Pair Request");
                Utils.stopDialogTimer();
            }

        }
    };



    // Get intent, action and MIME type
    private void catchUpgradeFile() throws IOException, NullPointerException {
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();

        File targetLocationparent = new File("/storage/emulated/0/CySmart");

        if (Intent.ACTION_VIEW.equalsIgnoreCase(action) && data != null) {
            if (intent.getScheme().compareTo("content") == 0) {
                try {
                    Cursor c = getContentResolver().query(
                            intent.getData(), null, null, null, null);
                    c.moveToFirst();
                    final int fileNameColumnId = c.getColumnIndex(
                            MediaStore.MediaColumns.DISPLAY_NAME);
                    if (fileNameColumnId >= 0)
                        attachmentFileName = c.getString(fileNameColumnId);
                    Logger.e("Filename>>>" + attachmentFileName);
                    // Fetch the attachment
                    attachment = getContentResolver().openInputStream(data);
                    if (attachment == null) {
                        Logger.e("onCreate" + "cannot access mail attachment");
                    } else {
                        if (fileExists(attachmentFileName, targetLocationparent)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage(getResources().getString(R.string.alert_message_file_copy))
                                    .setCancelable(false)
                                    .setTitle(getResources().getString(R.string.app_name))
                                    .setPositiveButton(
                                            getResources()
                                                    .getString(R.string.alert_message_yes),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    try {
                                                        FileOutputStream tmp = new FileOutputStream("/storage/emulated/0/CySmart" + File.separator + attachmentFileName);
                                                        byte[] buffer = new byte[1024];
                                                        int bytes = 0;
                                                        while ((bytes = attachment.read(buffer)) > 0)
                                                            tmp.write(buffer, 0, bytes);
                                                        tmp.close();
                                                        attachment.close();
                                                        getIntent().setData(null);
                                                    } catch (IOException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            })
                                    .setNegativeButton(
                                            getResources().getString(
                                                    R.string.alert_message_no),
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    // Cancel the dialog box
                                                    dialog.cancel();
                                                    getIntent().setData(null);
                                                }
                                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        } else {
                            try {
                                FileOutputStream tmp = new FileOutputStream("/storage/emulated/0/CySmart" + File.separator + attachmentFileName);
                                byte[] buffer = new byte[1024];
                                int bytes = 0;
                                while ((bytes = attachment.read(buffer)) > 0)
                                    tmp.write(buffer, 0, bytes);
                                tmp.close();
                                attachment.close();
                                Toast.makeText(this, getResources().getString(R.string.toast_file_copied), Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                String sourcePath = data.getPath();
                Logger.e("Action>>>" + action + "Uri" + data.toString() + "Source path>>" + sourcePath);

                final File sourceLocation = new File(sourcePath);
                String sourceFileName = sourceLocation.getName();

                final File targetLocation = new File("/storage/emulated/0/CySmart" + File.separator + sourceFileName);

                if (fileExists(sourceFileName, targetLocationparent)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this);
                    builder.setMessage(getResources().getString(R.string.alert_message_file_copy))
                            .setCancelable(false)
                            .setTitle(getResources().getString(R.string.app_name))
                            .setPositiveButton(
                                    getResources()
                                            .getString(R.string.alert_message_yes),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            try {
                                                copyDirectory(sourceLocation, targetLocation);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    })
                            .setNegativeButton(
                                    getResources().getString(
                                            R.string.alert_message_no),
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Cancel the dialog box
                                            dialog.cancel();
                                            getIntent().setData(null);
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    try {
                        copyDirectory(sourceLocation, targetLocation);
                        Toast.makeText(this, getResources().getString(R.string.toast_file_copied), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /*
    Checks whether a file exists in the folder specified
     */
    public boolean fileExists(String name, File file) {
        File[] list = file.listFiles();
        if (list != null)
            for (File fil : list) {
                if (fil.isDirectory()) {
                    fileExists(name, fil);
                } else if (name.equalsIgnoreCase(fil.getName())) {
                    Logger.e("File>>" + fil.getName());
                    return true;
                }
            }
        return false;
    }

    // If targetLocation does not exist, it will be created.
    public void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation.getAbsolutePath());
            OutputStream out = new FileOutputStream(targetLocation.getAbsolutePath());

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            getIntent().setData(null);
        }
    }

}
