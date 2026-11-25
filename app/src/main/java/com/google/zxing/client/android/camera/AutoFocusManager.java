package com.google.zxing.client.android.camera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import me.yoqi.android.qrcode.Config;

final class AutoFocusManager implements Camera.AutoFocusCallback {

    private static final String TAG = AutoFocusManager.class.getSimpleName();
    private static final long AUTO_FOCUS_INTERVAL_MS = 2500L;
    private static final Collection<String> FOCUS_MODES_CALLING_AF;

    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<>(2);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    private boolean active;
    private final boolean useAutoFocus;
    private final Camera camera;
    private final Handler handler;
    private final Runnable autoFocusRunnable = new Runnable() {
        @Override
        public void run() {
            synchronized (AutoFocusManager.this) {
                if (active) {
                    start();
                }
            }
        }
    };

    AutoFocusManager(Context context, Camera camera) {
        this.camera = camera;
        this.handler = new Handler(Looper.getMainLooper());
        String currentFocusMode = null;
        try {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters != null) {
                currentFocusMode = parameters.getFocusMode();
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Failed to read focus mode", e);
        }
        useAutoFocus = Config.AOTO_FOCUS && FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        Log.i(TAG, "Current focus mode '" + currentFocusMode + "'; use auto focus? " + useAutoFocus);
        start();
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera theCamera) {
        if (active) {
            handler.removeCallbacks(autoFocusRunnable);
            handler.postDelayed(autoFocusRunnable, AUTO_FOCUS_INTERVAL_MS);
        }
    }

    synchronized void start() {
        if (useAutoFocus) {
            active = true;
            try {
                camera.autoFocus(this);
            } catch (RuntimeException re) {
                Log.w(TAG, "Unexpected exception while focusing", re);
            }
        }
    }

    synchronized void stop() {
        active = false;
        handler.removeCallbacks(autoFocusRunnable);
        if (useAutoFocus) {
            try {
                camera.cancelAutoFocus();
            } catch (RuntimeException re) {
                Log.w(TAG, "Unexpected exception while cancelling focusing", re);
            }
        }
    }
}
