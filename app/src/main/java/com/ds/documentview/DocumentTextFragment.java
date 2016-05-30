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
package com.ds.documentview;

import com.ds.FoodScanner.BaseDocumentActivitiy;
import com.ds.FoodScanner.DocumentContentProvider;
import com.ds.FoodScanner.R;
import com.ds.util.PreferencesUtils;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

public class DocumentTextFragment extends Fragment implements TextWatcher {

    private final static String LOG_TAG = DocumentTextFragment.class.getSimpleName();
    private final static String IS_STATE_SAVED = "is_state_saved";
    private EditText mEditText;
    private int mDocumentId;
    private boolean mHasTextChanged;
    private ViewSwitcher mViewSwitcher;
    private HtmlToSpannedAsyncTask mHtmlTask;
    private BaseDocumentActivitiy.SaveDocumentTask saveTask;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_STATE_SAVED, true);
    }

    public static DocumentTextFragment newInstance(final String text, Integer documentId, final String imagePath) {
        DocumentTextFragment f = new DocumentTextFragment();
        // Supply text input as an argument.
        Bundle args = new Bundle();
        args.putString("text", text);
        args.putInt("id", documentId);
        args.putString("image_path", imagePath);
        f.setArguments(args);
        return f;
    }

    public Spanned getDocumentText() {
        return mEditText.getText();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHtmlTask != null) {
            mHtmlTask.cancel(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveIfTextHasChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String text = getArguments().getString("text");
        mDocumentId = getArguments().getInt("id");
        View view = inflater.inflate(R.layout.fragment_document, container, false);
        mEditText = (EditText) view.findViewById(R.id.editText_document);
        mViewSwitcher = (ViewSwitcher) view.findViewById(R.id.viewSwitcher);
        if (mHtmlTask != null) {
            mHtmlTask.cancel(true);
        }
        if (savedInstanceState == null || !savedInstanceState.getBoolean(IS_STATE_SAVED)) {
            mHtmlTask = new HtmlToSpannedAsyncTask(mEditText, mViewSwitcher, this);
            mHtmlTask.execute(text);
        } else {
            mViewSwitcher.setDisplayedChild(1);
            mEditText.addTextChangedListener(this);
        }

        PreferencesUtils.applyTextPreferences(mEditText, getActivity());

        return view;
    }


    void saveIfTextHasChanged() {
        if (mHasTextChanged) {
            mHasTextChanged = false;
            final Uri uri = Uri.withAppendedPath(DocumentContentProvider.CONTENT_URI, String.valueOf(mDocumentId));
            List<Uri> ids = new ArrayList<>();
            List<Spanned> texts = new ArrayList<>();
            ids.add(uri);
            texts.add(mEditText.getText());
            saveTask = new BaseDocumentActivitiy.SaveDocumentTask(getActivity(), ids, texts);
            saveTask.execute();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        PreferencesUtils.applyTextPreferences(mEditText, getActivity());
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mHasTextChanged = true;
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private static class HtmlToSpannedAsyncTask extends AsyncTask<String, Void, Spanned> {

        private final EditText mEditText;
        private final ViewSwitcher mViewSwitcher;
        private final TextWatcher mTextWatcher;

        private HtmlToSpannedAsyncTask(final EditText editText, ViewSwitcher viewSwitcher, TextWatcher textWatcher) {
            mEditText = editText;
            mViewSwitcher = viewSwitcher;
            mTextWatcher = textWatcher;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mViewSwitcher.setDisplayedChild(0);
        }

        @Override
        protected Spanned doInBackground(String... params) {
            if (params != null && params.length > 0 && params[0] != null && params[0].length() > 0) {
                return Html.fromHtml(params[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Spanned spanned) {
            super.onPostExecute(spanned);
            mEditText.setText(spanned);
            mEditText.addTextChangedListener(mTextWatcher);
            mViewSwitcher.setDisplayedChild(1);
        }
    }
}
