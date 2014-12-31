package com.example.smartnavi.btapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class MediaButtonIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (Intent.ACTION_CALL_BUTTON.equals(intentAction)) {
            Toast.makeText(context, "BOTON OTRO OPRIMIDO", Toast.LENGTH_SHORT).show();
            Log.d("test", "Broadcast Media");
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            int action = event.getAction();
            int keycode = event.getKeyCode();

            if (action == KeyEvent.KEYCODE_BACK) {
                Toast.makeText(context, "BUTTON OTHER PRESSED!", Toast.LENGTH_SHORT).show();
            }
            abortBroadcast();
        } else if (Intent.EXTRA_KEY_EVENT.equals(intentAction)) {
            Log.d("test", "Key Event detected");
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            int action = event.getAction();
            int keycode = event.getKeyCode();

            if (action == KeyEvent.ACTION_DOWN) {
                switch (keycode) {
                    case KeyEvent.KEYCODE_1:
                        Log.d("test", "Broadcast Key 1");
                        break;
                    case KeyEvent.KEYCODE_BUTTON_START:
                        Log.d("test", "Broadcast Key start");
                        break;
                    case KeyEvent.KEYCODE_BUTTON_1:
                        Log.d("test", "Broadcast Button 1");
                        break;
                    case KeyEvent.KEYCODE_V:
                        Log.d("test", "Broadcast V");
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        Log.d("test", "Broadcast Media Paused");
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        Log.d("test", "Broadcast Media Next");
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        Log.d("test", "Broadcast Media Previous");
                        break;
                    default:
                        Log.d("test", "Broadcast Default");
                        break;
                }
            }
            abortBroadcast();
        } else if (Intent.ACTION_ANSWER.equals(intentAction)) {
            Log.d("test", "Broadcast Answer");
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                Toast.makeText(context, "ANSWER PRESSED!", Toast.LENGTH_SHORT).show();
            }
            abortBroadcast();
        }
    }

}
