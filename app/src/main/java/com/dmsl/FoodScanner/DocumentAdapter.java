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
package com.dmsl.FoodScanner;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.dmsl.drawable.CrossFadeDrawable;
import com.dmsl.drawable.FastBitmapDrawable;
import com.dmsl.FoodScanner.CheckableGridElement.OnCheckedChangeListener;
import com.dmsl.FoodScanner.DocumentContentProvider.Columns;
import com.dmsl.util.Util;

public class DocumentAdapter extends CursorAdapter implements OnCheckedChangeListener {

	public interface OnCheckedChangeListener {
		void onCheckedChanged(final Set<Integer> checkedIds);
	}

	private final static String[] PROJECTION = { Columns.ID, Columns.TITLE, Columns.OCR_TEXT, Columns.CREATED, Columns.PHOTO_PATH,Columns.CHILD_COUNT };

	private Set<Integer> mSelectedDocuments = new HashSet<Integer>();
	private LayoutInflater mInflater;
	private final MainActivity mActivity;
	private int mElementLayoutId;

	private int mIndexCreated;
	private int mIndexTitle;
	private int mIndexID;
	private int mChildCountID;
	private OnCheckedChangeListener mCheckedChangeListener = null;

	static class DocumentViewHolder {

		public CheckableGridElement gridElement;
		private TextView date;
		private TextView mPageNumber;
		public int documentId;
		public boolean updateThumbnail;
		CrossFadeDrawable transition;

		DocumentViewHolder(View v) {
			gridElement = (CheckableGridElement) v;
			date = (TextView) v.findViewById(R.id.date);
			mPageNumber = (TextView) v.findViewById(R.id.page_number);
		}

	}

	public void clearAllSelection() {
		mSelectedDocuments.clear();
	}

	public DocumentAdapter(MainActivity activity, int elementLayout, OnCheckedChangeListener listener) {
		super(activity, activity.getContentResolver().query(DocumentContentProvider.CONTENT_URI, PROJECTION, DocumentContentProvider.Columns.PARENT_ID + "=-1", null, null), true);
		mElementLayoutId = elementLayout;
		mActivity = activity;
		mInflater = LayoutInflater.from(activity);
		final Cursor c = getCursor();
		mIndexCreated = c.getColumnIndex(Columns.CREATED);
		mIndexID = c.getColumnIndex(Columns.ID);
		mIndexTitle = c.getColumnIndex(Columns.TITLE);
		mChildCountID = c.getColumnIndex(Columns.CHILD_COUNT);
		mCheckedChangeListener = listener;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final DocumentViewHolder holder = (DocumentViewHolder) view.getTag();
		final int documentId = cursor.getInt(mIndexID);
		final int childCount = cursor.getInt(mChildCountID);
		final boolean isSelected = mSelectedDocuments.contains(documentId);
		holder.documentId = documentId;

		String title = cursor.getString(mIndexTitle);
		if (title != null && title.length() > 0) {
			holder.date.setText(title);
		} else {
			long created = cursor.getLong(mIndexCreated);
			CharSequence formattedDate = DateFormat.format("dd MMM, yyyy", new Date(created));
			holder.date.setText(formattedDate);
		}

//		if (holder.mPageNumber != null) {
//			holder.mPageNumber.setText(String.valueOf(childCount+1));
//		}
		if (holder.gridElement != null) {

			if (mActivity.getScrollState() == AbsListView.OnScrollListener.SCROLL_STATE_FLING || mActivity.isPendingThumbnailUpdate()) {
				holder.gridElement.setImage(Util.sDefaultDocumentThumbnail);
				holder.updateThumbnail = true;
			} else {
				final Drawable d = Util.getDocumentThumbnail(documentId);
				holder.gridElement.setImage(d);
				holder.updateThumbnail = false;
			}
		}
		holder.gridElement.setCheckedNoAnimate(isSelected);
	};
	
	@Override
	public long getItemId(int position) {
		if (getCursor().moveToPosition(position)) {
			int index = getCursor().getColumnIndex(Columns.ID);
			return getCursor().getLong(index);
		}
		return -1;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = null;
		DocumentViewHolder holder = null;
		v = mInflater.inflate(mElementLayoutId,null, false);
		int index = cursor.getColumnIndex(Columns.ID);
		int documentId = cursor.getInt(index);
		holder = new DocumentViewHolder(v);
		holder.documentId = documentId;
		holder.gridElement.setChecked(mSelectedDocuments.contains(documentId));
		holder.gridElement.setOnCheckedChangeListener(this);
		v.setTag(holder);
		FastBitmapDrawable start = Util.sDefaultDocumentThumbnail;
		Bitmap startBitmap = null;
		if(start!=null){
			startBitmap = start.getBitmap();
		}
		final CrossFadeDrawable transition = new CrossFadeDrawable(startBitmap, null);
		transition.setCallback(v);
		transition.setCrossFadeEnabled(true);
		holder.transition = transition;
		return v;
	}
	
	public void setSelectedDocumentIds(List<Integer> selection) {
		mSelectedDocuments.addAll(selection);
		if (mCheckedChangeListener!=null){
			mCheckedChangeListener.onCheckedChanged(mSelectedDocuments);
		}
	}

	public Set<Integer> getSelectedDocumentIds() {
		return mSelectedDocuments;
	}

	@Override
	public void onCheckedChanged(View documentView, boolean isChecked) {
		DocumentViewHolder holder = (DocumentViewHolder) documentView.getTag();
		if (isChecked) {
			mSelectedDocuments.add(holder.documentId);
		} else {
			mSelectedDocuments.remove(holder.documentId);
		}
		mCheckedChangeListener.onCheckedChanged(mSelectedDocuments);
	}
}
