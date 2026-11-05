package com.example.buttonremappertv;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ButtonAccessibilityService extends AccessibilityService {

    private long lastPressTime = 0;
    private int pressCount = 0;
    private int lastKeyCode = -1;
    private static final long MULTI_PRESS_TIMEOUT = 500;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        int btnIndex = getButtonIndex(keyCode);
        if (btnIndex == -1) {
            return super.onKeyEvent(event);
        }

        if (action == KeyEvent.ACTION_DOWN) {
            if (event.isLongPress()) {
                executeAction(btnIndex, 2); // Gesto 2: Pulsación Sostenida
                return true;
            }

            long currentTime = System.currentTimeMillis();
            if (keyCode == lastKeyCode && (currentTime - lastPressTime < MULTI_PRESS_TIMEOUT)) {
                pressCount++;
            } else {
                pressCount = 1;
                lastKeyCode = keyCode;
            }
            lastPressTime = currentTime;

            if (pressCount == 3) {
                pressCount = 0;
                executeAction(btnIndex, 1); // Gesto 1: Triple Toque
                return true;
            }
        } else if (action == KeyEvent.ACTION_UP) {
            long currentTime = System.currentTimeMillis();
            if (pressCount == 2 && (currentTime - lastPressTime >= MULTI_PRESS_TIMEOUT)) {
                pressCount = 0;
                executeAction(btnIndex, 0); // Gesto 0: Doble Toque
                return true;
            }
        }

        return super.onKeyEvent(event);
    }

    private int getButtonIndex(int keyCode) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP: return 0;
            case KeyEvent.KEYCODE_VOLUME_DOWN: return 1;
            case KeyEvent.KEYCODE_VOLUME_MUTE: return 2;
            case KeyEvent.KEYCODE_HOME: return 3;
            case KeyEvent.KEYCODE_BACK: return 4;
            case KeyEvent.KEYCODE_ASSIST: return 5;
            case KeyEvent.KEYCODE_POWER: return 6;
            case KeyEvent.KEYCODE_TV: return 7; // Mapeo común para botones dedicados
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: return 8;
            default: return -1;
        }
    }

    private void executeAction(int btnIndex, int gestureIndex) {
        SharedPreferences preferences = getSharedPreferences("ButtonMapConfig", Context.MODE_PRIVATE);
        String key = "MAP_" + btnIndex + "_" + gestureIndex;
        String actionConfig = preferences.getString(key, null);

        if (actionConfig == null) return;

        if (actionConfig.startsWith("SYS_")) {
            int sysAction = Integer.parseInt(actionConfig.replace("SYS_", ""));
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            switch (sysAction) {
                case 0:
                    performGlobalAction(GLOBAL_ACTION_HOME);
                    break;
                case 1:
                    if (audioManager != null) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, 0);
                    }
                    break;
                case 2:
                    Intent settIntent = new Intent(Settings.ACTION_SETTINGS);
                    settIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(settIntent);
                    break;
                case 3:
                    performGlobalAction(GLOBAL_ACTION_RECENTS);
                    break;
            }
        } else if (actionConfig.startsWith("APP_")) {
            String packageName = actionConfig.replace("APP_", "");
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
            } else {
                Toast.makeText(this, "La aplicación seleccionada no está disponible", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
