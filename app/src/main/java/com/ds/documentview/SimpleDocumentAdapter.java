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

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.ds.FoodScanner.DocumentContentProvider.Columns;
import com.ds.FoodScanner.R;

public class SimpleDocumentAdapter extends CursorAdapter {

	public interface ViewBinder {
		void bind(final View v, final DocumentViewHolder holder, final String title, final CharSequence formattedDate, final String text, final int position, int documentId);
	}

	private LayoutInflater mInflater;
	private final int mLayoutId;
	private int mIndexCreated;
	private int mIndexTitle;
	private int mIndexOCRText;
	private int mIndexId;
	private final ViewBinder mBinder;

	public static class DocumentViewHolder {

		public TextView text;
		public TextView date;
		public TextView mPageNumber;
		public EditText edit;
		public Button editTitleButton;
		public int boundId = -1;
		public TextWatcher watcher;
		
		DocumentViewHolder(View v) {
			text = (TextView) v.findViewById(R.id.text);
			date = (TextView) v.findViewById(R.id.date);
			edit = (EditText) v.findViewById(R.id.editText_document);
			mPageNumber = (TextView) v.findViewById(R.id.page_number);
			editTitleButton = (Button) v.findViewById(R.id.button_edit);
		}

	}

	@Override
	public Object getItem(int position) {
		Cursor c=  (Cursor) super.getItem(position);
		return c.getString(mIndexOCRText);
	}
	
	public SimpleDocumentAdapter(Activity context, final int layoutId, final Cursor cursor, final ViewBinder binder) {
		super(context, cursor,true);
		mInflater = LayoutInflater.from(context);
		final Cursor c = getCursor();
		mLayoutId = layoutId;
		mIndexCreated = c.getColumnIndex(Columns.CREATED);
		mIndexOCRText = c.getColumnIndex(Columns.OCR_TEXT);
		mIndexTitle = c.getColumnIndex(Columns.TITLE);
		mIndexId = c.getColumnIndex(Columns.ID);
		mBinder = binder;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		DocumentViewHolder holder = (DocumentViewHolder) view.getTag();
		final String title = cursor.getString(mIndexTitle);
		long created = cursor.getLong(mIndexCreated);
		final String text = cursor.getString(mIndexOCRText);
		CharSequence formattedDate = DateFormat.format("MMM dd, yyyy h:mmaa", new Date(created));
		final int id = cursor.getInt(mIndexId);
		mBinder.bind(view, holder, title, formattedDate, text, cursor.getPosition(),id);
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
		v = mInflater.inflate(mLayoutId, null);
		holder = new DocumentViewHolder(v);
		v.setTag(holder);
		return v;
	}
}
