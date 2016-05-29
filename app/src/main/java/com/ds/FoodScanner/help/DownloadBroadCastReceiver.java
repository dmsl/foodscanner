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

package com.ds.FoodScanner.help;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

public class DownloadBroadCastReceiver extends BroadcastReceiver {
	private static final String LOG_TAG = DownloadBroadCastReceiver.class.getSimpleName();

	@Override
	public void onReceive(final Context context, Intent intent) {
		String action = intent.getAction();
		if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
			Log.i(LOG_TAG,"received ACTION_DOWNLOAD_COMPLETE");
			long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
			Query query = new Query();
			query.setFilterById(downloadId);
			Cursor c = dm.query(query);
			if (c.moveToFirst()) {
				int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
				int status = c.getInt(columnIndex);
				columnIndex = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
				String title = c.getString(columnIndex);
				columnIndex = c.getColumnIndex(DownloadManager.COLUMN_URI);
				String name = c.getString(columnIndex);


				if (DownloadManager.STATUS_SUCCESSFUL == status) {
					Log.i(LOG_TAG,"Download successful");

					//start service to extract language file
					Intent serviceIntent = new Intent(context, OCRLanguageInstallService.class);
					serviceIntent.putExtra(DownloadManager.EXTRA_DOWNLOAD_ID, downloadId);
					serviceIntent.putExtra(OCRLanguageInstallService.EXTRA_FILE_NAME, name);
					context.startService(serviceIntent);

				} else if (DownloadManager.STATUS_FAILED==status){
					Log.i(LOG_TAG,"Download failed");
					Intent resultIntent = new Intent(OCRLanguageInstallService.ACTION_INSTALL_FAILED);
					columnIndex = c.getColumnIndex(DownloadManager.COLUMN_TITLE);
					resultIntent.putExtra(OCRLanguageInstallService.EXTRA_STATUS,status );
					resultIntent.putExtra(OCRLanguageInstallService.EXTRA_OCR_LANGUAGE_DISPLAY, title);
					context.sendBroadcast(resultIntent);
				}
			}
			c.close();

		}
	}

}
