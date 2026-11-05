package com.example.buttonremappertv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Spinner buttonSpinner, gestureSpinner, actionSpinner;
    private Button btnSave, btnAccessibility;
    private SharedPreferences preferences;
    private List<String> appPackageNames = new ArrayList<>();
    private List<String> appDisplayNames = new ArrayList<>();

    @Resource
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSpinner = findViewById(R.id.buttonSpinner);
        gestureSpinner = findViewById(R.id.gestureSpinner);
        actionSpinner = findViewById(R.id.actionSpinner);
        btnSave = findViewById(R.id.btnSave);
        btnAccessibility = findViewById(R.id.btnAccessibility);

        preferences = getSharedPreferences("ButtonMapConfig", Context.MODE_PRIVATE);

        // Botones exactos del control remoto de Chromecast
        String[] buttons = {
            "Subir Volumen (Vol +)", 
            "Bajar Volumen (Vol -)", 
            "Silenciar (Mute)", 
            "Botón Home (Casita)", 
            "Botón Back (Atrás)", 
            "Asistente de Google", 
            "Botón Encendido (Power)",
            "Botón Netflix",
            "Botón YouTube"
        };
        ArrayAdapter<String> btnAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, buttons);
        buttonSpinner.setAdapter(btnAdapter);

        // Gestos avanzados soportados
        String[] gestures = {
            "Doble Toque (2 Toques rápidos)", 
            "Triple Toque (3 Toques rápidos)", 
            "Pulsación Sostenida (Mantener presionado)"
        };
        ArrayAdapter<String> gestureAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, gestures);
        gestureSpinner.setAdapter(gestureAdapter);

        loadInstalledAppsAndActions();

        btnSave.setOnClickListener(v -> {
            int btnIdx = buttonSpinner.getSelectedItemPosition();
            int gesIdx = gestureSpinner.getSelectedItemPosition();
            int actIdx = actionSpinner.getSelectedItemPosition();

            String key = "MAP_" + btnIdx + "_" + gesIdx;
            
            if (actIdx < 4) {
                preferences.edit().putString(key, "SYS_" + actIdx).apply();
            } else {
                int appRealIdx = actIdx - 4;
                String packageName = appPackageNames.get(appRealIdx);
                preferences.edit().putString(key, "APP_" + packageName).apply();
            }

            Toast.makeText(MainActivity.this, "¡Mapeo guardado con éxito!", Toast.LENGTH_SHORT).show();
        });

        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }

    private void loadInstalledAppsAndActions() {
        List<String> actionsList = new ArrayList<>();
        // Acciones generales del sistema útiles
        actionsList.add("Ir al Inicio (Home)");
        actionsList.add("Alternar Silencio (Mute)");
        actionsList.add("Abrir Ajustes del Sistema");
        actionsList.add("Mostrar Aplicaciones Recientes");

        // Cargar todas las apps instaladas en el Chromecast
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> allApps = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo ri : allApps) {
            String appName = ri.loadLabel(pm).toString();
            String packageName = ri.activityInfo.packageName;
            appDisplayNames.add("Abrir App: " + appName);
            appPackageNames.add(packageName);
        }

        actionsList.addAll(appDisplayNames);

        ArrayAdapter<String> actionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, actionsList);
        actionSpinner.setAdapter(actionAdapter);
    }
}
