package com.flork.tapper;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import java.io.DataOutputStream;

public class OverlayService extends Service {
    private WindowManager wm;
    private Button btn;
    private boolean isHolding = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int kX, kY, pX, pY;

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        SharedPreferences prefs = getSharedPreferences("MacroConfig", MODE_PRIVATE);
        kX = prefs.getInt("kickX", 800);
        kY = prefs.getInt("kickY", 1500);
        pX = prefs.getInt("powerX", 300);
        pY = prefs.getInt("powerY", 1500);

        createButton();
    }

    private void createButton() {
        btn = new Button(this);
        btn.setText("KICK+POWER");
        btn.setTextColor(Color.WHITE);
        btn.setTextSize(10);

        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#8000FF00"));
        bg.setStroke(3, Color.GREEN);
        btn.setBackground(bg);

        int layoutType = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                250, 250, layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.RIGHT;
        params.x = 100; params.y = 150;

        btn.setOnTouchListener(new View.OnTouchListener() {
            float initX, initY;
            float initTouchX, initTouchY;
            boolean isDragging = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = params.x; initY = params.y;
                        initTouchX = event.getRawX(); initTouchY = event.getRawY();
                        isHolding = true;
                        isDragging = false;
                        handler.postDelayed(holdRunnable, 100); // Segurar pra spam
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - initTouchX;
                        float dy = event.getRawY() - initTouchY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isDragging = true;
                            isHolding = false;
                            handler.removeCallbacks(holdRunnable);
                        }
                        params.x = (int) (initX - dx);
                        params.y = (int) (initY + dy);
                        wm.updateViewLayout(btn, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        handler.removeCallbacks(holdRunnable);
                        if (isHolding) {
                            isHolding = false;
                            if (!isDragging && (event.getEventTime() - event.getDownTime() < 100)) {
                                executeTap(); // Clique rapido
                            }
                        }
                        return true;
                }
                return false;
            }
        });

        wm.addView(btn, params);
    }

    private Runnable holdRunnable = new Runnable() {
        @Override
        public void run() {
            if (isHolding) {
                executeTap();
                handler.postDelayed(this, 50); // 50ms de delay pro spam
            }
        }
    };

    private void executeTap() {
        new Thread(() -> {
            try {
                Process su = Runtime.getRuntime().exec("su");
                DataOutputStream os = new DataOutputStream(su.getOutputStream());
                // Manda o KICK e o POWER KICK na mesma linha pra ser o mais rápido possível
                os.writeBytes("input tap " + kX + " " + kY + "; input tap " + pX + " " + pY + "\n");
                os.flush();
                os.close();
                su.waitFor();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (btn != null) wm.removeView(btn);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
