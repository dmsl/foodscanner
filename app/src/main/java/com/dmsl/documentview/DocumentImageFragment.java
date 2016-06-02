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
package com.dmsl.documentview;

import com.dmsl.FoodScanner.R;
import com.dmsl.util.Util;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

public class DocumentImageFragment extends Fragment {

    private ImageView mImageView;
    private ViewSwitcher mViewSwitcher;
    private LoadImageAsyncTask mImageTask;

    public static DocumentImageFragment newInstance(final String imagePath) {
        DocumentImageFragment f = new DocumentImageFragment();
        // Supply text input as an argument.
        Bundle args = new Bundle();
        args.putString("image_path", imagePath);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageTask != null) {
            mImageTask.cancel(true);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final String imagePath = getArguments().getString("image_path");
        View view = inflater.inflate(R.layout.fragment_document_image, container, false);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        mViewSwitcher = (ViewSwitcher) view.findViewById(R.id.viewSwitcher);
        //use async loading

        mImageView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    @TargetApi(16)
                    public void onGlobalLayout() {
                        if (mImageTask != null) {
                            mImageTask.cancel(true);
                        }
                        mImageTask = new LoadImageAsyncTask(mImageView, mViewSwitcher);
                        mImageTask.execute(imagePath);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            mImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }

                });

        return view;
    }

    private static class LoadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {


        private final ImageView mImageView;
        private final ViewSwitcher mViewSwitcher;
        private final int mWidth, mHeight;

        private LoadImageAsyncTask(final ImageView imageView, ViewSwitcher viewSwitcher) {
            mImageView = imageView;
            mViewSwitcher = viewSwitcher;
            mHeight = mViewSwitcher.getHeight();
            mWidth = mViewSwitcher.getWidth();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            final String imagePath = params[0];
            return Util.decodeFile(imagePath, mWidth, mHeight);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mViewSwitcher.setDisplayedChild(0);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mImageView.setImageBitmap(bitmap);
            mViewSwitcher.setDisplayedChild(1);
        }
    }

}
