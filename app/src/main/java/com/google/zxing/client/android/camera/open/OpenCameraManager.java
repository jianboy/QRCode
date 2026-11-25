package com.google.zxing.client.android.camera.open;

/**
 * Selects an appropriate implementation of {@link OpenCameraInterface}.
 */
public final class OpenCameraManager {

    public OpenCameraInterface build() {
        return new GingerbreadOpenCameraInterface();
    }
}
