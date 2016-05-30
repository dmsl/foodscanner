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

import com.ds.documentview.TopDialogFragment;
import com.ds.FoodScanner.R;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class ImageBlurredDialog extends TopDialogFragment implements DialogInterface.OnClickListener, DialogInterface.OnCancelListener {

    public static final String TAG = ImageBlurredDialog.class.getSimpleName();

    private final static String EXTRA_BLURRINES = "extra_blurrines";

    @Override
    public void onClick(DialogInterface dialog, int which) {
        BlurDialogClickListener listener = (BlurDialogClickListener) getActivity();
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                listener.onNewImageClicked();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                listener.onContinueClicked();
                break;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        BlurDialogClickListener listener = (BlurDialogClickListener) getActivity();
        listener.onContinueClicked();

    }

    interface BlurDialogClickListener {
        void onContinueClicked();

        void onNewImageClicked();
    }

    public static ImageBlurredDialog newInstance(float blurrines) {
        Bundle extra = new Bundle();
        extra.putFloat(EXTRA_BLURRINES, blurrines);
        final ImageBlurredDialog dialog = new ImageBlurredDialog();
        dialog.setArguments(extra);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity(), R.style.DialogSlideAnim);
        builder.setCancelable(true);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_blur_warning, null);
        final float blurriness = getArguments().getFloat(EXTRA_BLURRINES);
        TextView titleTextView = (TextView) view.findViewById(R.id.blur_warning_title);
        if (blurriness > .75) {
            titleTextView.setText(R.string.text_is_very_blurry);
        } else {
            titleTextView.setText(R.string.text_is_blurry);
        }
        builder.setView(view);
        builder.setOnCancelListener(this);
        builder.setNegativeButton(R.string.continue_ocr, this);
        builder.setPositiveButton(R.string.new_image, this);
        final AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);
        setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        positionDialogAtTop(alertDialog);
        return alertDialog;
    }


}