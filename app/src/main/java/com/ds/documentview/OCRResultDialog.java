/*
* Food Scanner: A free and open Food Analyzer with superb accuracy!
*
* Food Scanner is a first-of-a-kind food analyzer offering valuable
* information about foods such as nutritional facts, allergens and
* chemicals, using ordinary smartphones.
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

package com.ds.documentview;

import com.ds.FoodScanner.R;
import com.ds.FoodScanner.help.HelpActivity;

import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.TextView;

public class OCRResultDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = OCRResultDialog.class.getSimpleName();

    private final static String EXTRA_ACCURACY = "extra_ocr_accuracy";
    public static final int LOW_ACCURACY = 75;
    public static final int MEDIUM_ACCURACY = 83;

    public static OCRResultDialog newInstance(int ocrAccuracy) {
        Bundle extra = new Bundle();
        extra.putInt(EXTRA_ACCURACY, ocrAccuracy);
        final OCRResultDialog ocrResultDialog = new OCRResultDialog();
        ocrResultDialog.setArguments(extra);
        return ocrResultDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setCancelable(true);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_ocr_result, null);
        TextView speech = (TextView) view.findViewById(R.id.help_header);
        final int accuracy = getArguments().getInt(EXTRA_ACCURACY);
        if (accuracy <= LOW_ACCURACY) {
            speech.setText(R.string.ocr_result_is_bad);
            hideTextActions(view);
            TextView explanation = (TextView) view.findViewById(R.id.explanation_text);
            explanation.setVisibility(View.VISIBLE);
            View divider0 = view.findViewById(R.id.divider0);
            divider0.setVisibility(View.VISIBLE);

        } else if (accuracy < MEDIUM_ACCURACY) {
            speech.setText(R.string.ocr_result_is_ok);
        } else {
            //speech.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fairy_happy, 0, 0, 0);
            speech.setText(R.string.ocr_result_is_good);
            hideTipsAndFeedback(view);
        }
        setButtonListeners(view);
        builder.setNegativeButton(android.R.string.ok, null);
        builder.setView(view);
        return builder.create();
    }

    private void hideTextActions(View view) {
        view.findViewById(R.id.divider4).setVisibility(View.GONE);
        view.findViewById(R.id.divider5).setVisibility(View.GONE);
        view.findViewById(R.id.button_copy_to_clipboard).setVisibility(View.GONE);
        view.findViewById(R.id.button_share_text).setVisibility(View.GONE);
    }

    private void setButtonListeners(View view) {
        view.findViewById(R.id.button_show_tips).setOnClickListener(this);
        view.findViewById(R.id.button_copy_to_clipboard).setOnClickListener(this);
        view.findViewById(R.id.button_share_text).setOnClickListener(this);
    }

    private void hideTipsAndFeedback(View view) {
        view.findViewById(R.id.divider1).setVisibility(View.GONE);
        view.findViewById(R.id.button_show_tips).setVisibility(View.GONE);
        view.findViewById(R.id.divider2).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        final DocumentActivity activity = (DocumentActivity) getActivity();
        if (activity == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.button_show_tips:
                startActivity(new Intent(activity, HelpActivity.class));
                break;
            case R.id.button_copy_to_clipboard:
                activity.copyTextToClipboard();
                break;
            case R.id.button_share_text:
                activity.shareText();
                break;
        }
        final android.support.v4.app.Fragment fragmentByTag = activity.getSupportFragmentManager().findFragmentByTag(TAG);
        if (fragmentByTag != null) {
            activity.getSupportFragmentManager().beginTransaction().remove(fragmentByTag).commitAllowingStateLoss();
        }
    }
}