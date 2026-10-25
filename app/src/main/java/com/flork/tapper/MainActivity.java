package com.flork.tapper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);

        TextView title = new TextView(this);
        title.setText("Configurar Coordenadas do Macro");
        title.setTextSize(20);
        layout.addView(title);

        SharedPreferences prefs = getSharedPreferences("MacroConfig", MODE_PRIVATE);

        layout.addView(new TextView(this));
        layout.addView(new TextView(this)); layout.getChildAt(layout.getChildCount()-1).toString();
        TextView t1 = new TextView(this); t1.setText("KICK (X, Y):"); layout.addView(t1);
        
        EditText kickX = new EditText(this); kickX.setHint("Ex: 500"); kickX.setText(String.valueOf(prefs.getInt("kickX", 800))); layout.addView(kickX);
        EditText kickY = new EditText(this); kickY.setHint("Ex: 1500"); kickY.setText(String.valueOf(prefs.getInt("kickY", 1500))); layout.addView(kickY);

        layout.addView(new TextView(this));
        TextView t2 = new TextView(this); t2.setText("POWER KICK (X, Y):"); layout.addView(t2);
        
        EditText powerX = new EditText(this); powerX.setHint("Ex: 300"); powerX.setText(String.valueOf(prefs.getInt("powerX", 300))); layout.addView(powerX);
        EditText powerY = new EditText(this); powerY.setHint("Ex: 1500"); powerY.setText(String.valueOf(prefs.getInt("powerY", 1500))); layout.addView(powerY);

        Button saveBtn = new Button(this);
        saveBtn.setText("SALVAR E ABRIR BOTAO FLUTUANTE");
        saveBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("kickX", Integer.parseInt(kickX.getText().toString()));
            editor.putInt("kickY", Integer.parseInt(kickY.getText().toString()));
            editor.putInt("powerX", Integer.parseInt(powerX.getText().toString()));
            editor.putInt("powerY", Integer.parseInt(powerY.getText().toString()));
            editor.apply();

            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 123);
            } else {
                startService(new Intent(this, OverlayService.class));
                Toast.makeText(this, "Botao flutuante ativado!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        layout.addView(saveBtn);

        setContentView(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 123 && Settings.canDrawOverlays(this)) {
            startService(new Intent(this, OverlayService.class));
            finish();
        }
    }
}
