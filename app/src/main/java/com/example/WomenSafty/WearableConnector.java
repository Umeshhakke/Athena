package com.example.WomenSafty;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Set;

public class WearableConnector {

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private static final int PERMISSION_BLUETOOTH_CONNECT = 200;

    public WearableConnector(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect() {
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for Android 12+ runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    (android.app.Activity) context,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    PERMISSION_BLUETOOTH_CONNECT
            );
            return;
        }

        showBluetoothDevicesDialog();
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private void showBluetoothDevicesDialog() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        ArrayList<String> deviceNames = new ArrayList<>();
        ArrayList<BluetoothDevice> deviceList = new ArrayList<>();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceNames.add(device.getName() + "\n" + device.getAddress());
                deviceList.add(device);
            }
        } else {
            deviceNames.add("No paired devices found");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Bluetooth Device");
        builder.setItems(deviceNames.toArray(new CharSequence[deviceNames.size()]), new DialogInterface.OnClickListener() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (pairedDevices.size() == 0) return;
                BluetoothDevice selectedDevice = deviceList.get(which);
                Toast.makeText(context, "Selected: " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Add connection logic here
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
