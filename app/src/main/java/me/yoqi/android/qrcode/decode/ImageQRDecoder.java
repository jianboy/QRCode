package me.yoqi.android.qrcode.decode;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/**
 * 从本地图片 URI / Bitmap 识别二维码与常见条码的工具类
 */
public final class ImageQRDecoder {

    private static final String TAG = "ImageQRDecoder";
    private static final int MAX_DECODE_DIMENSION = 1200;

    private ImageQRDecoder() {
    }

    /**
     * 从图片 Uri 中解码二维码/条码
     *
     * @param context 上下文
     * @param uri     图库或文件选择返回的 Uri
     * @return 解码成功返回 {@link Result}，未识别或失败返回 null
     */
    public static Result decodeFromUri(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        Bitmap bitmap = loadSampledBitmap(context.getContentResolver(), uri, MAX_DECODE_DIMENSION);
        if (bitmap == null) {
            return null;
        }
        try {
            return decodeFromBitmap(bitmap);
        } finally {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    /**
     * 从 Bitmap 中解码二维码/条码
     */
    public static Result decodeFromBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        MultiFormatReader reader = createReader();

        // 先尝试正常亮度和两种二值化算法
        Result result = decodeWithSource(reader, source);
        if (result != null) {
            return result;
        }

        // 再尝试反色图（兼容黑底白码等反色二维码）
        int[] invertedPixels = new int[pixels.length];
        for (int i = 0; i < pixels.length; i++) {
            invertedPixels[i] = pixels[i] ^ 0x00FFFFFF;
        }
        RGBLuminanceSource invertedSource = new RGBLuminanceSource(width, height, invertedPixels);
        return decodeWithSource(reader, invertedSource);
    }

    private static Result decodeWithSource(MultiFormatReader reader, LuminanceSource source) {
        Result result = tryDecode(reader, new BinaryBitmap(new HybridBinarizer(source)));
        if (result != null) {
            return result;
        }
        return tryDecode(reader, new BinaryBitmap(new GlobalHistogramBinarizer(source)));
    }

    private static Result tryDecode(MultiFormatReader reader, BinaryBitmap binaryBitmap) {
        try {
            return reader.decodeWithState(binaryBitmap);
        } catch (ReaderException ignored) {
            return null;
        } finally {
            reader.reset();
        }
    }

    private static MultiFormatReader createReader() {
        MultiFormatReader reader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        EnumSet<BarcodeFormat> formats = EnumSet.of(
                BarcodeFormat.QR_CODE,
                BarcodeFormat.DATA_MATRIX,
                BarcodeFormat.AZTEC,
                BarcodeFormat.PDF_417,
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E,
                BarcodeFormat.CODE_39,
                BarcodeFormat.CODE_93,
                BarcodeFormat.CODE_128,
                BarcodeFormat.ITF,
                BarcodeFormat.CODABAR
        );
        hints.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        reader.setHints(hints);
        return reader;
    }

    private static Bitmap loadSampledBitmap(ContentResolver resolver, Uri uri, int maxDimension) {
        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;

        try (InputStream is = resolver.openInputStream(uri)) {
            if (is == null) {
                return null;
            }
            BitmapFactory.decodeStream(is, null, boundsOptions);
        } catch (IOException | SecurityException e) {
            Log.w(TAG, "Failed to read image bounds", e);
            return null;
        }

        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
            return null;
        }

        int inSampleSize = 1;
        int longestSide = Math.max(boundsOptions.outWidth, boundsOptions.outHeight);
        while (longestSide / inSampleSize > maxDimension) {
            inSampleSize *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = inSampleSize;
        decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

        try (InputStream is = resolver.openInputStream(uri)) {
            if (is == null) {
                return null;
            }
            return BitmapFactory.decodeStream(is, null, decodeOptions);
        } catch (IOException | SecurityException | OutOfMemoryError e) {
            Log.w(TAG, "Failed to decode bitmap", e);
            return null;
        }
    }
}
