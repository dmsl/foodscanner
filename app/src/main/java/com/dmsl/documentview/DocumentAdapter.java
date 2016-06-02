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

import com.dmsl.FoodScanner.DocumentContentProvider.Columns;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

public class DocumentAdapter extends FragmentStatePagerAdapter {
    private int mIndexLanguage;
    private int mIndexImagePath;
	private int mIndexTitle;
	private int mIndexOCRText;
	private int mIndexId;

	Cursor mCursor;
    private Map<Integer, DocumentTextFragment> mPageReferenceMap = new HashMap<>();
    private boolean mShowText = true;

    public DocumentAdapter(FragmentManager fm, final Cursor cursor) {
        super(fm);
        mCursor = cursor;
        mIndexOCRText = mCursor.getColumnIndex(Columns.OCR_TEXT);
        mIndexImagePath = mCursor.getColumnIndex(Columns.PHOTO_PATH);
        mIndexTitle = mCursor.getColumnIndex(Columns.TITLE);
        mIndexId = mCursor.getColumnIndex(Columns.ID);
        mIndexLanguage = mCursor.getColumnIndex(Columns.OCR_LANG);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Object o = super.instantiateItem(container, position);
        if (o instanceof  DocumentTextFragment) {
            mPageReferenceMap.put(position, (DocumentTextFragment) o);
        } else {
            mPageReferenceMap.put(position,null);
        }
        return o;

    }

    public void setShowText(boolean text) {
        mShowText = text;
        notifyDataSetChanged();

    }

    @Override
    public Fragment getItem(int position) {
        String text = null;
        Integer documentId = -1;
        String imagePath = null;
        if (mCursor.moveToPosition(position)) {
            text = mCursor.getString(mIndexOCRText);
            documentId = mCursor.getInt(mIndexId);
            imagePath = mCursor.getString(mIndexImagePath);
        }
        if (mShowText) {
            return DocumentTextFragment.newInstance(text, documentId, imagePath);
        }else {
            return DocumentImageFragment.newInstance(imagePath);

        }
    }

    public DocumentTextFragment getFragment(int key) {
        return mPageReferenceMap.get(key);
    }

    public void destroyItem(View container, int position, Object object) {
        super.destroyItem(container, position, object);
        mPageReferenceMap.remove(position);
    }

	@Override
	public int getCount() {
		return mCursor.getCount();
	}


    public String getLanguage(int position){
        if(mCursor.moveToPosition(position)){
            return mCursor.getString(mIndexLanguage);
        }
        return null;
    }

    public int getId(int position){
        if(mCursor.moveToPosition(position)){
            return mCursor.getInt(mIndexId);
        }
        return -1;
    }

	public String getLongTitle(int position) {
		if (mCursor.moveToPosition(position)) {
			return mCursor.getString(mIndexTitle);
		}
		return null;
	}

    public String getText(int position){
        boolean success = mCursor.moveToPosition(position);
        if (success) {
            return mCursor.getString(mIndexOCRText);
        }
        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        if(object instanceof  DocumentTextFragment && !mShowText){
            return POSITION_NONE;
        }else if(object instanceof  DocumentImageFragment && mShowText){
            return POSITION_NONE;
        }
        return POSITION_UNCHANGED;
    }

    public void setCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public boolean getShowText() {
        return mShowText;
    }
}