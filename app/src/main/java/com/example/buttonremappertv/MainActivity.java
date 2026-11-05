package com.example.buttonremappertv;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Spinner actionSpinner;
    private Button btnSave, btnAccessibility;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionSpinner = findViewById(R.id.actionSpinner);
        btnSave = findViewById(R.id.btnSave);
        btnAccessibility = findViewById(R.id.btnAccessibility);

        preferences = getSharedPreferences("ButtonMapConfig", Context.MODE_PRIVATE);

        // Opciones de acciones disponibles
        String[] actions = {"Alternar Silencio (Mute)", "Establecer Volumen al 50%", "Abrir YouTube"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, actions);
        actionSpinner.setAdapter(adapter);

        // Cargar opción guardada anteriormente
        int savedAction = preferences.getInt("VOL_UP_3_TAP_ACTION", 0);
        actionSpinner.setSelection(savedAction);

        btnSave.setOnClickListener(v -> {
            int selectedPosition = actionSpinner.getSelectedItemPosition();
            preferences.edit().putInt("VOL_UP_3_TAP_ACTION", selectedPosition).apply();
            Toast.makeText(MainActivity.this, "Configuración guardada con éxito", Toast.LENGTH_SHORT).show();
        });

        btnAccessibility.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }
}
