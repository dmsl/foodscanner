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
package com.ds.FoodScanner.help;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.ds.FoodScanner.R;

public class OCRLanguageAdapter extends BaseAdapter implements ListAdapter {

	private static Comparator<OCRLanguage> mLanguageComparator = new Comparator<OCRLanguageAdapter.OCRLanguage>() {

		@Override
		public int compare(OCRLanguage lhs, OCRLanguage rhs) {
			return lhs.mDisplayText.compareTo(rhs.mDisplayText);
		}
	};

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }


    public static class OCRLanguage implements Parcelable{

		public String getValue() {
			return mValue;
		}

		public String getDisplayText() {
			return mDisplayText;
		}

		boolean mDownloaded;
		boolean mDownloading;
        boolean needsCubeData;
		String mValue;
		String mDisplayText;
		long mSize;

        public OCRLanguage(Parcel in){
            mValue = in.readString();
            mDisplayText = in.readString();
        }

        public OCRLanguage(final String value, final String displayText, boolean downloaded, long size) {
			mDownloaded = downloaded;
			mValue = value;
			mDisplayText = displayText;
			this.mSize = size;
            if ("ara".equalsIgnoreCase(value) || "hin".equalsIgnoreCase(value)){
                needsCubeData = true;
            }
		}

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
            public OCRLanguage createFromParcel(Parcel in) {
                return new OCRLanguage(in);
            }

            public OCRLanguage[] newArray(int size) {
                return new OCRLanguage[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mValue);
            dest.writeString(mDisplayText);
        }

        @Override
        public String toString() {
            return mDisplayText;
        }
    }

	private static class ViewHolder {
		ViewFlipper mFlipper;
		TextView mTextViewLanguage;
	}

	private final List<OCRLanguage> mLanguages = new ArrayList<OCRLanguage>();
	private final LayoutInflater mInflater;
    private boolean mShowOnlyLanguageNames;

	public OCRLanguageAdapter(final Context context, boolean showOnlyLanguageNames) {
        this.mShowOnlyLanguageNames = showOnlyLanguageNames;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    public void addAll(List<OCRLanguage> languages) {
        mLanguages.addAll(languages);
        Collections.sort(mLanguages, mLanguageComparator);
    }


    public void add(OCRLanguage language) {
		mLanguages.add(language);
		Collections.sort(mLanguages, mLanguageComparator);
        notifyDataSetChanged();
	}

    @Override
	public int getCount() {
		return mLanguages.size();
	}

	@Override
	public Object getItem(int position) {
		return mLanguages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
//		if (convertView == null) {
//			convertView = mInflater.inflate(R.layout.ocr_language_list_item, null);
//			holder = new ViewHolder();
//			holder.mFlipper = (ViewFlipper) convertView.findViewById(R.id.viewFlipper);
//			holder.mTextViewLanguage = (TextView) convertView.findViewById(R.id.textView_language);
//			convertView.setTag(holder);
//		} else {
			holder = (ViewHolder) convertView.getTag();
		//}
		OCRLanguage language = mLanguages.get(position);
		if (mShowOnlyLanguageNames) {
			holder.mFlipper.setVisibility(View.INVISIBLE);
		} else {
			if (language.mDownloaded == true) {
				holder.mFlipper.setDisplayedChild(2);
			} else if (language.mDownloading == true) {
				holder.mFlipper.setDisplayedChild(1);
			} else {
				holder.mFlipper.setDisplayedChild(0);
			}

		}

		holder.mTextViewLanguage.setText(language.mDisplayText);
		return convertView;
	}

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return mLanguages.isEmpty();
    }

    public void setDownloading(String languageDisplayValue, boolean downloading) {
		for (OCRLanguage lang : this.mLanguages) {
			if (lang.mDisplayText.equalsIgnoreCase(languageDisplayValue)) {
				lang.mDownloading = downloading;
				break;
			}
		}
	}
}
