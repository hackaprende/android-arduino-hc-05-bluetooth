package com.example.smartnavi.btapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.UUID;

public class MainActivity extends Activity {

    public static final String SAVED_MAC = "Saved mac";
    public static final String MY_PREFS = "My Preferences";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    BTService mBTService;

    private String macToSave;

    private final static UUID MY_UUID = UUID.randomUUID();

    BluetoothAdapter mBluetoothAdapter;

    private TextView macText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences(MY_PREFS, 0);
        macToSave = settings.getString(SAVED_MAC, "");


        macText = (TextView) findViewById(R.id.mac_text);

        macText.setText(macToSave);

        Log.d("APP","APP INICIADA: " + macToSave);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        MyApp myApplication = (MyApp) this.getApplicationContext();
        myApplication.mainActivity = this;

        /*IntentFilter buttonFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        MediaButtonIntentReceiver mediaButtonIntentReceiver = new MediaButtonIntentReceiver();
        buttonFilter.setPriority(2147483647);
        registerReceiver(mediaButtonIntentReceiver,buttonFilter);*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONNECT_DEVICE_SECURE) {
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String device = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                SharedPreferences settings = getSharedPreferences(MY_PREFS, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(SAVED_MAC, device);
                editor.apply();
                macText.setText(device);
                connectDevice(data, true);
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        //mBTService.connect(device, secure);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id == R.id.action_search_device) {
            Intent searchIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(searchIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

    /*public void buttonClicked() {
        Toast.makeText(this,"BOTON OPRIMIDO",Toast.LENGTH_SHORT).show();
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 1000;
        float x = 250.0f;
        float y = 250.0f;
        int metaState = 0;
        MotionEvent motionEvent = MotionEvent.obtain(
                downTime,
                eventTime,
                MotionEvent.ACTION_UP,
                x,
                y,
                metaState
        );

        dispatchTouchEvent(motionEvent);
    }*/

