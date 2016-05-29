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

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.ds.documentview.SimpleDocumentAdapter.DocumentViewHolder;
import com.ds.documentview.SimpleDocumentAdapter.ViewBinder;
import com.ds.FoodScanner.DocumentContentProvider;
import com.ds.FoodScanner.DocumentContentProvider.Columns;
import com.ds.FoodScanner.R;
import com.ds.FoodScanner.cropimage.MonitoredActivity;
import com.ds.FoodScanner.help.HintDialog;

public class TableOfContentsActivity extends MonitoredActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
    private final static String[] PROJECTION = {Columns.ID, Columns.TITLE, Columns.OCR_TEXT, Columns.CREATED};


    public final static String EXTRA_DOCUMENT_ID = "document_id";
    public final static String EXTRA_DOCUMENT_POS = "document_pos";
    private ListView mList;
    private static final int HINT_DIALOG_ID = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_of_contents);
        if (getIntent() == null || getIntent().getData() == null) {
            finish();
            return;
        }
        mList = (ListView) findViewById(R.id.list);
        mList.setOnItemClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportLoaderManager().initLoader(0, null, this);
        initAppIcon(-1);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case HINT_DIALOG_ID:
                return HintDialog.createDialog(this, R.string.toc_help_title, "file:///android_res/raw/toc_help.html");
        }
        return super.onCreateDialog(id, args);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        final Uri documentUri = getIntent().getData();
        final String selection = DocumentContentProvider.Columns.PARENT_ID + "=? OR " + Columns.ID + "=?";
        final String[] args = new String[]{documentUri.getLastPathSegment(), documentUri.getLastPathSegment()};
        return new CursorLoader(this, DocumentContentProvider.CONTENT_URI, PROJECTION, selection, args, "created ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

        final SimpleDocumentAdapter adapter = new SimpleDocumentAdapter(this, R.layout.table_of_contents_element, cursor, new ViewBinder() {

            @Override
            public void bind(View v, DocumentViewHolder holder, String title, CharSequence formattedDate, String text, int position, final int id) {
                holder.date.setText(formattedDate);
                holder.text.setText(title);
                final String pageNo = String.valueOf(position + 1);
                holder.mPageNumber.setText(pageNo);
            }

        });
        mList.setAdapter(adapter);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent data = new Intent();
        data.putExtra(EXTRA_DOCUMENT_ID, (int) id);
        data.putExtra(EXTRA_DOCUMENT_POS, position);
        setResult(RESULT_OK, data);
        finish();
    }
}
