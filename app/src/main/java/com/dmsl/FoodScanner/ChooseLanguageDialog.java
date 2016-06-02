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

import java.util.List;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.dmsl.FoodScanner.help.OCRLanguageActivity;
import com.dmsl.FoodScanner.help.OCRLanguageAdapter;
import com.dmsl.FoodScanner.help.OCRLanguageAdapter.OCRLanguage;

public class ChooseLanguageDialog {
	
	interface OnLanguageChosenListener {
		void onLanguageChosen(final OCRLanguage lang);
	}

	public static AlertDialog createDialog(Context context, final OnLanguageChosenListener onLanguageChosenListener) {
		List<OCRLanguage> installedLanguages = OCRLanguageActivity.getInstalledOCRLanguages(context);
		// actual values uses by tesseract
		OCRLanguageAdapter adapter = new OCRLanguageAdapter(context,true);
        adapter.addAll(installedLanguages);
		LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout =  li.inflate(R.layout.dialog_language_list, null,false);
		final ListView list = (ListView) layout.findViewById(R.id.listView_languages);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(true);
		builder.setView(layout);
		final AlertDialog result = builder.create();
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				OCRLanguage lang = (OCRLanguage) list.getItemAtPosition(position);
				onLanguageChosenListener.onLanguageChosen(lang);
				result.cancel();
				
			}
		});
		return result;
	}

}
