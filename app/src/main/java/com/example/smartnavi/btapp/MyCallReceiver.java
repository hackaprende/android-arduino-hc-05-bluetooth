package com.example.smartnavi.btapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class MyCallReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        MainActivity mainActivity = ((MyApp) context.getApplicationContext()).mainActivity;
        if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_RINGING)){
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            //if(incomingNumber.equals("6671422952")){
                Toast.makeText(context,"Llamada entrante bato: " + incomingNumber, Toast.LENGTH_LONG).show();
                //mainActivity.sendMessage("Llamada entrante bato: " + incomingNumber);
            //}
        }
        else if(intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_IDLE) || intent.getStringExtra(TelephonyManager.EXTRA_STATE).equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
            Toast.makeText(context,"Se ha colgado el teléfono", Toast.LENGTH_SHORT).show();
        }
    }
}