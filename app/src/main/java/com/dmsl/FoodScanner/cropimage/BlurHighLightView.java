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
package com.dmsl.FoodScanner.cropimage;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class BlurHighLightView implements HighLightView {

    private final RectF mBlurredRegion;
    private final RectF mDrawRect = new RectF(); // in screen space
    private final Rect mViewDrawingRect = new Rect();
    private final Paint mOutlinePaint = new Paint();
    private final Paint mFocusPaint = new Paint();
    private final Rect mLeftRect = new Rect();
    private final Rect mRightRect = new Rect();
    private final Rect mTopRect = new Rect();
    private final Rect mBottomRect = new Rect();

    private Matrix mMatrix;

    BlurHighLightView(Rect blurredRegion, int progressColor, int edgeWidth, Matrix imageMatrix) {
        mBlurredRegion = new RectF(blurredRegion);
        mOutlinePaint.setARGB(0xFF, Color.red(progressColor), Color.green(progressColor), Color.blue(progressColor));
        mOutlinePaint.setStrokeWidth(edgeWidth);
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setAntiAlias(true);
        mMatrix = new Matrix(imageMatrix);
        mFocusPaint.setARGB(125, 50, 50, 50);
        mFocusPaint.setStyle(Paint.Style.FILL);


    }

    @Override
    public Matrix getMatrix() {
        return mMatrix;
    }

    @Override
    public Rect getDrawRect() {
        return mViewDrawingRect;
    }

    @Override
    public float centerY() {
        return mBlurredRegion.centerY();
    }

    @Override
    public float centerX() {
        return mBlurredRegion.centerX();
    }

    @Override
    public int getHit(float x, float y, float scale) {
        return GROW_NONE;
    }

    @Override
    public void handleMotion(int motionEdge, float dx, float dy) {

    }

    @Override
    public void draw(Canvas canvas) {
        //set draw rect by mapping the points
        mMatrix.mapRect(mDrawRect,mBlurredRegion);
        mViewDrawingRect.set((int) mDrawRect.left, (int) mDrawRect.top, (int) mDrawRect.right, (int) mDrawRect.bottom);
        canvas.drawRect(mDrawRect, mOutlinePaint);

        mTopRect.set(0, 0, canvas.getWidth(), (int) mDrawRect.top);
        mLeftRect.set(0, (int) mDrawRect.top, (int) mDrawRect.left, (int) mDrawRect.bottom);
        mRightRect.set((int) mDrawRect.right, (int) mDrawRect.top, canvas.getWidth(), (int) mDrawRect.bottom);
        mBottomRect.set(0, (int) mDrawRect.bottom, canvas.getWidth(), canvas.getHeight());


        canvas.drawRect(mTopRect, mFocusPaint);
        canvas.drawRect(mRightRect, mFocusPaint);
        canvas.drawRect(mLeftRect, mFocusPaint);
        canvas.drawRect(mBottomRect, mFocusPaint);

    }
}
