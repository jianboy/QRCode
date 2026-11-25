package me.yoqi.android.qrcode;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.decode.BeepManager;
import com.google.zxing.client.android.decode.CaptureActivityHandler;
import com.google.zxing.client.android.decode.InactivityTimer;
import com.google.zxing.client.android.decode.ViewfinderView;
import com.google.zxing.client.android.result.ResultHandlerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.yoqi.android.qrcode.decode.ImageQRDecoder;
import me.yoqi.android.qrcode.ui.ResultActivity;

/**
 * 识别二维码 CaptureActivity（支持相机实时扫描与图库选图识别）
 *
 * @author liuyuqi
 */
public final class CaptureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = CaptureActivity.class.getSimpleName();
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private Result savedResultToShow;
    private ViewfinderView viewfinderView;
    private boolean hasSurface;
    private Collection<BarcodeFormat> decodeFormats;
    private InactivityTimer inactivityTimer;
    private String characterSet;
    private BeepManager beepManager;
    private ExecutorService decodeExecutor;

    ImageView opreateView;
    private View btnPickGallery;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickVisualMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::handleGalleryUriResult);

    private final ActivityResultLauncher<String> getContentLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::handleGalleryUriResult);

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        setContentView(R.layout.capture);

        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        decodeExecutor = Executors.newSingleThreadExecutor();
        opreateView = findViewById(R.id.button_openorcloseClick);
        btnPickGallery = findViewById(R.id.btn_pick_gallery);

        applySystemBarInsets();

        opreateView.setOnClickListener(v -> toggleFlashlight());
        if (btnPickGallery != null) {
            btnPickGallery.setOnClickListener(v -> openGalleryPicker());
        }
    }

    private void applySystemBarInsets() {
        View root = findViewById(R.id.capture_root);
        View topControls = findViewById(R.id.top_controls);
        View noticeView = findViewById(R.id.barcode_notice);
        if (root == null) {
            return;
        }
        final int topPad = topControls != null ? topControls.getPaddingTop() : 0;
        final int bottomMargin = noticeView != null && noticeView.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams
                ? ((android.view.ViewGroup.MarginLayoutParams) noticeView.getLayoutParams()).bottomMargin
                : 0;

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            if (topControls != null) {
                topControls.setPadding(
                        topControls.getPaddingLeft(),
                        topPad + bars.top,
                        topControls.getPaddingRight(),
                        topControls.getPaddingBottom()
                );
            }
            if (noticeView != null && noticeView.getLayoutParams() instanceof android.view.ViewGroup.MarginLayoutParams) {
                android.view.ViewGroup.MarginLayoutParams lp =
                        (android.view.ViewGroup.MarginLayoutParams) noticeView.getLayoutParams();
                lp.bottomMargin = bottomMargin + bars.bottom;
                noticeView.setLayoutParams(lp);
            }
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void toggleFlashlight() {
        if (cameraManager != null && cameraManager.isOpen()) {
            Config.KEY_FRONT_LIGHT = !Config.KEY_FRONT_LIGHT;
            updateTorchIcon();
            cameraManager.setTorch(Config.KEY_FRONT_LIGHT);
        }
    }

    private void updateTorchIcon() {
        if (opreateView != null) {
            opreateView.setImageResource(
                    Config.KEY_FRONT_LIGHT ? R.drawable.mzw_camera_close : R.drawable.mzw_camera_open
            );
        }
    }

    private void openGalleryPicker() {
        try {
            pickVisualMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        } catch (ActivityNotFoundException e) {
            try {
                getContentLauncher.launch("image/*");
            } catch (ActivityNotFoundException ex) {
                Toast.makeText(this, R.string.decode_gallery_read_error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void handleGalleryUriResult(Uri uri) {
        if (uri == null || decodeExecutor == null || decodeExecutor.isShutdown()) {
            return;
        }
        decodeExecutor.execute(() -> {
            Result result = ImageQRDecoder.decodeFromUri(getApplicationContext(), uri);
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                if (result != null && result.getText() != null) {
                    handleDecode(result, null);
                } else {
                    Toast.makeText(CaptureActivity.this, R.string.decode_gallery_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraManager = new CameraManager(getApplication());
        viewfinderView = findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);

        handler = null;
        updateTorchIcon();

        SurfaceView surfaceView = findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
        }

        beepManager.updatePrefs();
        inactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        if (cameraManager != null) {
            cameraManager.closeDriver();
        }
        if (!hasSurface) {
            SurfaceView surfaceView = findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        if (cameraManager != null) {
            cameraManager.closeDriver();
        }
        if (!hasSurface) {
            SurfaceView surfaceView = findViewById(R.id.preview_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        if (decodeExecutor != null) {
            decodeExecutor.shutdownNow();
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return super.onKeyDown(keyCode, event);
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (cameraManager != null) {
                    Config.KEY_FRONT_LIGHT = false;
                    updateTorchIcon();
                    cameraManager.setTorch(false);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (cameraManager != null) {
                    Config.KEY_FRONT_LIGHT = true;
                    updateTorchIcon();
                    cameraManager.setTorch(true);
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        if (handler == null) {
            savedResultToShow = result;
        } else {
            if (result != null) {
                savedResultToShow = result;
            }
            if (savedResultToShow != null) {
                Message message = Message.obtain(handler,
                        R.id.decode_succeeded, savedResultToShow);
                handler.sendMessage(message);
            }
            savedResultToShow = null;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * A valid barcode has been found, so give an indication of success and show
     * the results.
     *
     * @param rawResult The contents of the barcode.
     * @param barcode   A greyscale bitmap of the camera data which was decoded (null for gallery decode).
     */
    public void handleDecode(Result rawResult, Bitmap barcode) {
        inactivityTimer.onActivity();
        ResultHandlerFactory.makeResultHandler(this, rawResult);

        beepManager.playBeepSoundAndVibrate();
        if (barcode != null && viewfinderView != null) {
            viewfinderView.drawResultBitmap(barcode);
        }

        String text = rawResult.getText();
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("text", text);
        startActivity(intent);
        if (cameraManager != null) {
            cameraManager.closeDriver();
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (!hasCameraPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            }
            return;
        }
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager == null) {
            return;
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler = new CaptureActivityHandler(this, viewfinderView,
                        decodeFormats, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            Toast.makeText(this, R.string.camera_problem, Toast.LENGTH_SHORT).show();
            finish();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            Toast.makeText(this, R.string.framwork_problem, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (hasSurface) {
                    SurfaceView surfaceView = findViewById(R.id.preview_view);
                    if (surfaceView != null) {
                        initCamera(surfaceView.getHolder());
                    }
                }
            } else {
                Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public void drawViewfinder() {
        if (viewfinderView != null) {
            viewfinderView.drawViewfinder();
        }
    }
}
