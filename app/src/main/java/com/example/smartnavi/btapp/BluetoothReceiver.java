package com.example.smartnavi.btapp;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Toast.makeText(context, "HOLA CARNAL", Toast.LENGTH_SHORT).show();
            Log.d("BLUETOOTH", intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE) + "");

            String device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE).toString();
            if(device.equals("58:C3:8B:63:D4:39")) {
                Intent i = new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
                MainActivity mainActivity = ((MyApp) context.getApplicationContext()).mainActivity;
                //mainActivity.connectToDevice();
            }
        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            Toast.makeText(context, "HOLA COMPADRE", Toast.LENGTH_SHORT).show();
        }
    }
}