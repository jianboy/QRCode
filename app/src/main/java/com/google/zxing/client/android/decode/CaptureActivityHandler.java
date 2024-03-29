/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android.decode;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;

import java.util.Collection;

import me.yoqi.android.qrcode.CaptureActivity;
import me.yoqi.android.qrcode.R;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler
{

    private static final String   TAG = CaptureActivityHandler.class.getSimpleName ();

    private final CaptureActivity activity;
    private final DecodeThread    decodeThread;
    private State                 state;
    private final CameraManager   cameraManager;

    private enum State
    {
        PREVIEW, SUCCESS, DONE
    }

    public CaptureActivityHandler(CaptureActivity activity,ViewfinderView viewFinderView, Collection<BarcodeFormat> decodeFormats, String characterSet, CameraManager cameraManager)
    {
        this.activity = activity;
        decodeThread = new DecodeThread (activity,decodeFormats,characterSet,new ViewfinderResultPointCallback (viewFinderView));
        decodeThread.start ();
        state = State.SUCCESS;

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview ();
        restartPreviewAndDecode ();
    }

    @Override
    public void handleMessage(Message message){
        int what = message.what;
        if(what == R.id.restart_preview){
            restartPreviewAndDecode ();
        }
        else if(what == R.id.decode_succeeded){
            state = State.SUCCESS;
            Bundle bundle = message.getData ();
            Bitmap barcode = bundle == null ? null : (Bitmap) bundle.getParcelable (DecodeThread.BARCODE_BITMAP);
            activity.handleDecode ((Result) message.obj, barcode);
        }
        else if(what == R.id.decode_failed){
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame (decodeThread.getHandler (), R.id.decode);
        }
        else if(what == R.id.return_scan_result){
            activity.setResult (Activity.RESULT_OK, (Intent) message.obj);
            activity.finish ();
        }
        else if(what == R.id.launch_product_query){
            Log.d (TAG, "Got product query message");
            String url = (String) message.obj;

            Intent intent = new Intent (Intent.ACTION_VIEW);
            intent.addFlags (Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            intent.setData (Uri.parse (url));

            ResolveInfo resolveInfo = activity.getPackageManager ().resolveActivity (intent, PackageManager.MATCH_DEFAULT_ONLY);
            String browserPackageName = null;
            if (resolveInfo.activityInfo != null)
            {
                browserPackageName = resolveInfo.activityInfo.packageName;
                Log.d (TAG, "Using browser in package " + browserPackageName);
            }

            // Needed for default Android browser only apparently
            if ("com.android.browser".equals (browserPackageName))
            {
                intent.setPackage (browserPackageName);
                intent.addFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra (Browser.EXTRA_APPLICATION_ID, browserPackageName);
            }

            try
            {
                activity.startActivity (intent);
            } catch (ActivityNotFoundException anfe)
            {
                Log.w (TAG, "Can't find anything to handle VIEW of URI " + url);
            }
        }
    }

    public void quitSynchronously(){
        state = State.DONE;
        cameraManager.stopPreview ();
        Message quit = Message.obtain (decodeThread.getHandler (), R.id.quit);
        quit.sendToTarget ();
        try
        {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join (500L);
        } catch (InterruptedException e)
        {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages (R.id.decode_succeeded);
        removeMessages (R.id.decode_failed);
    }

    private void restartPreviewAndDecode(){
        if (state == State.SUCCESS)
        {
            state = State.PREVIEW;
            cameraManager.requestPreviewFrame (decodeThread.getHandler (), R.id.decode);
            activity.drawViewfinder ();
        }
    }

}
