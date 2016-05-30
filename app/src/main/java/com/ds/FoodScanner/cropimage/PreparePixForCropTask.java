/*
* FoodScanner: A free and open Food Analyzer (nutritional facts, allergens and chemicals)
*
* FoodScanner is a first-of-a-kind food analyzer offering valuable 
* information such as nutritional facts, allergens and 
* chemicals, about foods using ordinary smartphones.
*
* Authors: D. Stefanidis
* 
* Supervisor: Demetrios Zeinalipour-Yazti
*
* URL: http://foodscanner.cs.ucy.ac.cy
* Contact: foodscanner@cs.ucy.ac.cy
*
* Copyright (c) 2016, Data Management Systems Lab (DMSL), University of Cyprus.
* All rights reserved.
*
* Permission is hereby granted, free of charge, to any person obtaining a copy of
* this software and associated documentation files (the "Software"), to deal in the
* Software without restriction, including without limitation the rights to use, copy,
* modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
* and to permit persons to whom the Software is furnished to do so, subject to the
* following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
* DEALINGS IN THE SOFTWARE.
*
*/
package com.ds.FoodScanner.cropimage;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.WriteFile;
import com.ds.image_processing.Blur;
import com.ds.image_processing.BlurDetectionResult;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

class CropData {
    private final Bitmap mBitmap;
    private final CropImageScaler.ScaleResult mScaleResult;
    private final BlurDetectionResult mBlurriness;

    CropData(Bitmap bitmap, CropImageScaler.ScaleResult scaleFactor, BlurDetectionResult blurriness) {
        mBitmap = bitmap;
        this.mScaleResult = scaleFactor;
        this.mBlurriness = blurriness;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public CropImageScaler.ScaleResult getScaleResult() {
        return mScaleResult;
    }

    public BlurDetectionResult getBlurriness() {
        return mBlurriness;
    }

    public void recylce() {
        mScaleResult.getPix().recycle();
        mBlurriness.getPixBlur().recycle();
    }
}


public class PreparePixForCropTask extends AsyncTask<Void, Void, CropData> {
    private static final String TAG = PreparePixForCropTask.class.getName();


    private final Pix mPix;
    private final int mWidth;
    private final int mHeight;

    public PreparePixForCropTask(Pix pix, int width, int height) {
        if (width == 0 || height == 0) {
            Log.e(TAG, "scaling to 0 value: (" + width + "," + height + ")");
        }
        mPix = pix;
        mWidth = width;
        mHeight = height;
    }

    @Override
    protected void onCancelled(CropData cropData) {
        super.onCancelled(cropData);
        if (cropData != null) {
            cropData.recylce();
        }
    }

    @Override
    protected void onPostExecute(CropData cropData) {
        super.onPostExecute(cropData);
        de.greenrobot.event.EventBus.getDefault().post(cropData);
    }

    @Override
    protected CropData doInBackground(Void... params) {
        Log.d(TAG, "scaling to (" + mWidth + "," + mHeight + ")");
        BlurDetectionResult blurDetectionResult = Blur.blurDetect(mPix);
        CropImageScaler scaler = new CropImageScaler();
        CropImageScaler.ScaleResult scaleResult;
        // scale it so that it fits the screen
        if (isCancelled()) {
            return null;
        }
        scaleResult = scaler.scale(mPix, mWidth, mHeight);
        if (isCancelled()) {
            return null;
        }
        Bitmap bitmap = WriteFile.writeBitmap(scaleResult.getPix());
        if (isCancelled()) {
            return null;
        }
        if (bitmap != null) {
            Log.d(TAG, "scaling result (" + bitmap.getWidth() + "," + bitmap.getHeight() + ")");
        }
        return new CropData(bitmap, scaleResult, blurDetectionResult);

    }


}