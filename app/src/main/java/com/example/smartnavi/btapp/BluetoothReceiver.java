package com.example.smartnavi.btapp;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;


public class BluetoothReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        //MainActivity mainActivity = ((MyApp) context.getApplicationContext()).mainActivity;

        String savedMac = getSavedMac(context);
        String action = intent.getAction();

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            Toast.makeText(context, "HOLA CARNAL", Toast.LENGTH_SHORT).show();

            String device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE).toString();
            //"58:C3:8B:63:D4:39"

            /*if(device.equals(savedMac)){
                Intent i = new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }*/

        } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            Toast.makeText(context, "HOLA COMPADRE", Toast.LENGTH_SHORT).show();
        }
    }

    public String getSavedMac(Context context){
        SharedPreferences settings = context.getSharedPreferences(MainActivity.MY_PREFS, 0);
        return settings.getString(MainActivity.SAVED_MAC, "");
    }
}