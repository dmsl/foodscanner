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
package com.dmsl.FoodScanner.help;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.dmsl.FoodScanner.R;

public class HintDialog{

	private HintDialog(){
		
	}
	
	public static AlertDialog createDialog(final Context context,final int speechBubbleText, final String pathToHTML){
		AlertDialog.Builder builder;

		View layout = View.inflate(context, R.layout.dialog_fairy_helping, null);
		TextView speech = (TextView) layout.findViewById(R.id.help_header);
		speech.setText(speechBubbleText);
		WebView webView = (WebView) layout.findViewById(R.id.webView_help);
		webView.loadUrl(pathToHTML);
		builder = new AlertDialog.Builder(context);
		builder.setView(layout);
		builder.setNegativeButton(android.R.string.ok, null);
		return builder.create();
		
	}

}
