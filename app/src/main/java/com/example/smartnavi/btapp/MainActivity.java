package com.example.smartnavi.btapp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.UUID;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;

    public final static String STATE_RESOLVING_ERROR = "resolving_error";

    private boolean mResolvingError = false;
    // Creating a Location client to get the device current location
    //private LocationClient mLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    // Global variable to hold the current location
    Location mCurrentLocation;
    Location dest;

    LocationListener locationListener;

    public static final String SAVED_MAC = "Saved mac";
    public static final String MY_PREFS = "My Preferences";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1001;

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

    private String macToSave;

    private final static UUID MY_UUID = UUID.randomUUID();

    private BTService mBTService = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    BluetoothAdapter mBluetoothAdapter;

    private TextView macText;
    private Uri fileUri;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationRequest = new LocationRequest();

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the update interval to 5 seconds
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        // Set the fastest update interval to 1 second
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        SharedPreferences settings = getSharedPreferences(MY_PREFS, 0);
        macToSave = settings.getString(SAVED_MAC, "");

        //macText = (TextView) findViewById(R.id.mac_text);

        //macText.setText(macToSave);

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
        Log.d("MAC",macToSave);
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        }
        else {
            if (mBTService == null) startBluetoothService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        //if (mBTService != null) mBTService.stop();
    }

    private void startBluetoothService(){
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mBTService = new BTService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage(message);
                    }
                    return true;
                }
            };

    public void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mBTService.getState() != BTService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mBTService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    public final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    public final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BTService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            mConversationArrayAdapter.clear();
                            break;
                        case BTService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BTService.STATE_LISTEN:
                        case BTService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    mConversationArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    if(readMessage.equals("n1")) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        String phoneNumberKey = getString(R.string.pref_number_1_key);
                        String phoneNumber = sharedPref.getString(phoneNumberKey,getString(R.string.pref_number_default));
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                        startActivity(callIntent);
                    }
                    else if(readMessage.equals("n2")) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        String phoneNumberKey = getString(R.string.pref_number_2_key);
                        String phoneNumber = sharedPref.getString(phoneNumberKey,getString(R.string.pref_number_default));
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                        startActivity(callIntent);
                    }
                    else if(readMessage.equals("n3")) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        String phoneNumberKey = getString(R.string.pref_number_3_key);
                        String phoneNumber = sharedPref.getString(phoneNumberKey,getString(R.string.pref_number_default));
                        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
                        startActivity(callIntent);
                    }
                    else if (readMessage.equals("camara")){
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                        // start the image capture Intent
                        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                    }
                    else if (readMessage.equals("ans")){
                        Intent answerIntent = new Intent(MainActivity.this, AutoAnswerIntentService.class);
                        Log.d("test", "Llamar Intent");
                        startService(answerIntent);
                    }
                    else if (readMessage.equals("sms")){
                        mGoogleApiClient.connect();
                    }
                    break;

                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    void sendSMS(String messageText){
        try {

            String SENT = "sent";
            String DELIVERED = "delivered";

            Intent sentIntent = new Intent(SENT);
            PendingIntent sentPI = PendingIntent.getBroadcast(
                    getApplicationContext(), 0, sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Intent deliveryIntent = new Intent(DELIVERED);

            PendingIntent deliverPI = PendingIntent.getBroadcast(
                    getApplicationContext(), 0, deliveryIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String result = "";

                    switch (getResultCode()) {

                        case Activity.RESULT_OK:
                            result = "Mensaje enviado";
                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            result = "Falló al enviar mensaje";
                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            result = "Falló al enviar mensaje";
                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            result = "Falló al enviar mensaje";
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            result = "No hay señal";
                            break;
                    }

                    Toast.makeText(getApplicationContext(), result,
                            Toast.LENGTH_LONG).show();
                }

            }, new IntentFilter(SENT));

            registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    /*Toast.makeText(getApplicationContext(), "Mensaje Entregado",
                            Toast.LENGTH_LONG).show();*/
                }

            }, new IntentFilter(DELIVERED));

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String phoneNumberKey1 = getString(R.string.pref_number_1_key);
            String phoneNumber1 = sharedPref.getString(phoneNumberKey1,getString(R.string.pref_number_default));
            String phoneNumberKey2 = getString(R.string.pref_number_2_key);
            String phoneNumber2 = sharedPref.getString(phoneNumberKey2,getString(R.string.pref_number_default));
            String phoneNumberKey3 = getString(R.string.pref_number_3_key);
            String phoneNumber3 = sharedPref.getString(phoneNumberKey3,getString(R.string.pref_number_default));
            SmsManager smsManager = SmsManager.getDefault();
            if(phoneNumber1 != null && !phoneNumber1.equals("")) {
                smsManager.sendTextMessage(phoneNumber1, null, messageText, sentPI, deliverPI);
            }
            if(phoneNumber2 != null && !phoneNumber2.equals("")) {
                smsManager.sendTextMessage(phoneNumber2, null, messageText, sentPI, deliverPI);
            }
            if(phoneNumber3 != null && !phoneNumber3.equals("")) {
                smsManager.sendTextMessage(phoneNumber3, null, messageText, sentPI, deliverPI);
            }
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage(), Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
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
                //macText.setText(device);
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
        mBTService.connect(device, secure);
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        else if(id == R.id.action_search_device) {
            Intent searchIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(searchIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        dest = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        if (mCurrentLocation == null) {
           sendSMS("sin GPS");
        }
        else{
           sendSMS(String.valueOf(mCurrentLocation.getLatitude()) + " ; " + String.valueOf(mCurrentLocation.getLongitude()));
        }

        /*if(mLocationClient.isConnected()){
            mLocationClient.disconnect();
        }*/
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }
}