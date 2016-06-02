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

import com.dmsl.documentview.DocumentActivity.DocumentContainerFragment;
import com.dmsl.FoodScanner.R;
import com.dmsl.util.PreferencesUtils;
import com.viewpagerindicator.CirclePageIndicator;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class DocumentPagerFragment extends Fragment implements DocumentContainerFragment {

    private ViewPager mPager;
    private CirclePageIndicator mTitleIndicator;
    private boolean mIsTitleIndicatorVisible = false;
    private boolean mIsNewCursor;
    private Cursor mCursor;
    DocumentAdapter mAdapter;
    private int mLastPosition = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_document_pager, container, false);
        mPager = (ViewPager) v.findViewById(R.id.document_pager);
        mTitleIndicator = (CirclePageIndicator) v.findViewById(R.id.titles);
        mLastPosition = 0;
        initPager();
        return v;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        boolean isKeyboardHidden = Configuration.KEYBOARDHIDDEN_YES == newConfig.hardKeyboardHidden;
        boolean isKeyboardShown = Configuration.KEYBOARDHIDDEN_NO == newConfig.hardKeyboardHidden;
        if (isKeyboardShown) {
            showTitleIndicator(false);
        } else if (isKeyboardHidden) {
            showTitleIndicator(true);
        }
    }


    private void showTitleIndicator(final boolean show) {
        if (mIsTitleIndicatorVisible) {
            if (show) {
                mTitleIndicator.setVisibility(View.VISIBLE);
            } else {
                mTitleIndicator.setVisibility(View.GONE);
            }
        }
    }

    public void applyTextPreferences() {
        final int count = mPager.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = mPager.getChildAt(i);
            EditText e = (EditText) v.findViewById(R.id.editText_document);
            PreferencesUtils.applyTextPreferences(e, getActivity());
        }
    }

    public void setDisplayedPage(final int pageno) {
        mPager.setCurrentItem(pageno, true);
    }

    public void setDisplayedPageByDocumentId(final int documentId) {
        final int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            final int id = mAdapter.getId(i);
            if (documentId == id) {
                mPager.setCurrentItem(i, false);
                return;
            }
        }
    }

    private void initPager() {

        if (mIsNewCursor && mPager != null) {


            if (mAdapter != null) {
                mAdapter.setCursor(mCursor);
            } else {
                mAdapter = new DocumentAdapter(getFragmentManager(), mCursor);
                mPager.setAdapter(mAdapter);
            }

            if (mAdapter.getCount() > 1) {
                mTitleIndicator.setViewPager(mPager);
                mIsTitleIndicatorVisible = true;
            } else {
                mTitleIndicator.setVisibility(View.GONE);
            }

            mTitleIndicator.setOnPageChangeListener(new OnPageChangeListener() {

                @Override
                public void onPageSelected(int position) {
                    final DocumentTextFragment fragment = mAdapter.getFragment(mLastPosition);
                    if (fragment != null) {
                        fragment.saveIfTextHasChanged();
                    }
                    mLastPosition = position;
                    final String title = mAdapter.getLongTitle(position);
                    ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(title);
                    ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
                }

                @Override
                public void onPageScrolled(int arg0, float arg1, int arg2) {
                }

                @Override
                public void onPageScrollStateChanged(int arg0) {
                }
            });
            mIsNewCursor = false;
        }
    }

    @Override
    public String getLangOfCurrentlyShownDocument() {
        DocumentAdapter adapter = (DocumentAdapter) mPager.getAdapter();
        int currentItem = mPager.getCurrentItem();
        return adapter.getLanguage(currentItem);
    }

    @Override
    public void setCursor(Cursor cursor) {
        mIsNewCursor = true;
        mCursor = cursor;
        initPager();
    }

    @Override
    public String getTextOfAllDocuments() {
        final DocumentTextFragment fragment = mAdapter.getFragment(mLastPosition);

        DocumentAdapter adapter = (DocumentAdapter) mPager.getAdapter();
        final int count = adapter.getCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            String text = null;
            if (i == mLastPosition && fragment != null) {
                final Spanned documentText = fragment.getDocumentText();
                if (!TextUtils.isEmpty(documentText)) {
                    text = Html.toHtml(documentText);
                }
            } else {
                text = adapter.getText(i);
            }
            if (text != null) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(text);
            }
        }
        return sb.toString();
    }

    @Override
    public void setShowText(boolean text) {
        mAdapter.setShowText(text);
    }

    @Override
    public boolean getShowText() {
        return mAdapter.getShowText();
    }

}
