package com.example.buttonremappertv;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ButtonAccessibilityService extends AccessibilityService {

    private int volumeUpPressCount = 0;
    private long lastPressTime = 0;
    private static final long MULTI_PRESS_TIMEOUT = 500;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && action == KeyEvent.ACTION_DOWN) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastPressTime < MULTI_PRESS_TIMEOUT) {
                volumeUpPressCount++;
            } else {
                volumeUpPressCount = 1;
            }
            lastPressTime = currentTime;

            if (volumeUpPressCount == 3) {
                volumeUpPressCount = 0;
                executeConfiguredAction();
                return true; // Bloquea la acción por defecto para ejecutar la personalizada
            }
        }

        return super.onKeyEvent(event);
    }

    private void executeConfiguredAction() {
        SharedPreferences preferences = getSharedPreferences("ButtonMapConfig", Context.MODE_PRIVATE);
        int selectedAction = preferences.getInt("VOL_UP_3_TAP_ACTION", 0);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        switch (selectedAction) {
            case 0: // Alternar Silencio
                if (audioManager != null) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, 0);
                    Toast.makeText(this, "Acción: Mute alternado", Toast.LENGTH_SHORT).show();
                }
                break;
            case 1: // Volumen al 50%
                if (audioManager != null) {
                    int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVol / 2, AudioManager.FLAG_SHOW_UI);
                    Toast.makeText(this, "Acción: Volumen al 50%", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2: // Abrir YouTube
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.youtube.tv");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(this, "YouTube no está instalado", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
