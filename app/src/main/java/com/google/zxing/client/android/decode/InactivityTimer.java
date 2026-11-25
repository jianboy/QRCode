package com.google.zxing.client.android.decode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.content.ContextCompat;

/**
 * Finishes an activity after a period of inactivity if the device is on battery power.
 */
public final class InactivityTimer {

    private static final String TAG = InactivityTimer.class.getSimpleName();
    private static final long INACTIVITY_DELAY_MS = 5 * 60 * 1000L;

    private final Activity activity;
    private final BroadcastReceiver powerStatusReceiver;
    private final Handler handler;
    private final Runnable finishRunnable;
    private boolean isReceiverRegistered;

    public InactivityTimer(Activity activity) {
        this.activity = activity;
        this.powerStatusReceiver = new PowerStatusReceiver();
        this.handler = new Handler(Looper.getMainLooper());
        this.finishRunnable = () -> {
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                Log.i(TAG, "Finishing activity due to inactivity");
                activity.finish();
            }
        };
        onActivity();
    }

    public synchronized void onActivity() {
        cancel();
        handler.postDelayed(finishRunnable, INACTIVITY_DELAY_MS);
    }

    public void onPause() {
        cancel();
        if (isReceiverRegistered) {
            try {
                activity.unregisterReceiver(powerStatusReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "unregisterReceiver failed", e);
            }
            isReceiverRegistered = false;
        }
    }

    public void onResume() {
        if (!isReceiverRegistered) {
            try {
                ContextCompat.registerReceiver(
                        activity,
                        powerStatusReceiver,
                        new IntentFilter(Intent.ACTION_BATTERY_CHANGED),
                        ContextCompat.RECEIVER_NOT_EXPORTED
                );
                isReceiverRegistered = true;
            } catch (Exception e) {
                Log.w(TAG, "registerReceiver failed", e);
            }
        }
        onActivity();
    }

    private synchronized void cancel() {
        handler.removeCallbacks(finishRunnable);
    }

    public void shutdown() {
        cancel();
    }

    private final class PowerStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                boolean onBatteryNow = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) <= 0;
                if (onBatteryNow) {
                    InactivityTimer.this.onActivity();
                } else {
                    InactivityTimer.this.cancel();
                }
            }
        }
    }
}
