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
package com.dmsl.util;

import com.dmsl.FoodScanner.R;
import com.dmsl.FoodScanner.help.OCRLanguageAdapter.OCRLanguage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Pair;
import android.widget.TextView;

public class PreferencesUtils {

    /* ids of the radio buttons pressed in the options dialogs */
    public final static String PREFERENCES_SPACING_KEY = "line_spacing";
    public final static String PREFERENCES_DESIGN_KEY = "text_design";
    public final static String PREFERENCES_ALIGNMENT_KEY = "text_alignment";
    public final static String PREFERENCES_TEXT_SIZE_KEY = "text_size";
    private final static String PREFERENCES_TRAINING_DATA_DIR = "training_data_dir";

    // actual language
    public final static String PREFERENCES_OCR_LANG = "ocr_language";
    private static final String PREFERENCES_OCR_LANG_DISPLAY = "ocr_language_display";

    public final static String PREFERENCES_KEY = "text_preferences";
    private static final String PREFERENCES_THUMBNAIL_HEIGHT = "thumbnail_width";
    private static final String PREFERENCES_THUMBNAIL_WIDTH = "thumbnail_height";
    private static final String PREFERENCES_HAS_ASKED_FOR_FEEDBACK = "has_asked_for_feedback";

    public static void initPreferencesWithDefaultsIfEmpty(Context appContext) {
        SharedPreferences prefs = getPreferences(appContext);
        Editor edit = prefs.edit();

        final String defaultLanguage = appContext.getString(R.string.default_ocr_language);
        final String defaultLanguageDisplay = appContext.getString(R.string.default_ocr_display_language);
        setIfEmpty(edit, prefs, PREFERENCES_OCR_LANG, defaultLanguage);
        setIfEmpty(edit, prefs, PREFERENCES_OCR_LANG_DISPLAY, defaultLanguageDisplay);
        edit.apply();
    }

    private static void setIfEmpty(final Editor edit, final SharedPreferences prefs, final String id, final int value) {
        if (!prefs.contains(id)) {
            edit.putInt(id, value);
        }
    }

    private static void setIfEmpty(final Editor edit, final SharedPreferences prefs, final String id, final String value) {
        if (!prefs.contains(id)) {
            edit.putString(id, value);
        }
    }

    public static void saveOCRLanguage(final Context context, OCRLanguage language) {
        SharedPreferences prefs = getPreferences(context);
        Editor edit = prefs.edit();
        edit.putString(PREFERENCES_OCR_LANG, language.getValue());
        edit.putString(PREFERENCES_OCR_LANG_DISPLAY, language.getDisplayText());
        edit.apply();
    }

//	public static void pushDownloadId(Context context, long downloadId) {
//		SharedPreferences prefs = getPreferences(context);
//		Editor edit = prefs.edit();
//		edit.putLong("" + downloadId, downloadId);
//		edit.apply();
//	}

//	public static boolean isDownloadId(Context context, long downloadId) {
//		SharedPreferences prefs = getPreferences(context);
//		long savedId = prefs.getLong("" + downloadId, -1);
//		if (savedId != -1) {
//			Editor edit = prefs.edit();
//			edit.remove("" + downloadId);
//			edit.apply();
//			return true;
//		}
//		return false;
//	}

    public static Pair<String, String> getOCRLanguage(final Context context) {
        SharedPreferences prefs = getPreferences(context);
        String value = prefs.getString(PREFERENCES_OCR_LANG, null);
        String display = prefs.getString(PREFERENCES_OCR_LANG_DISPLAY, null);
        return new Pair<>(value, display);
    }

    public static void saveTessDir(Context appContext, final String value) {
        SharedPreferences prefs = getPreferences(appContext);
        Editor edit = prefs.edit();
        edit.putString(PREFERENCES_TRAINING_DATA_DIR, value);
        edit.apply();
    }

    public static void setNumberOfSuccessfulScans(Context appContext, final int value) {
        SharedPreferences prefs = getPreferences(appContext);
        Editor edit = prefs.edit();
        edit.putInt(PREFERENCES_HAS_ASKED_FOR_FEEDBACK, value);
        edit.apply();
    }

    public static int getNumberOfSuccessfulScans(Context appContext) {
        SharedPreferences prefs = getPreferences(appContext);
        return prefs.getInt(PREFERENCES_HAS_ASKED_FOR_FEEDBACK, 0);
    }


    public static String getTessDir(Context appContext) {
        SharedPreferences prefs = getPreferences(appContext);
        return prefs.getString(PREFERENCES_TRAINING_DATA_DIR, null);
    }


    public static void saveTextSize(Context appContext, float size) {
        SharedPreferences prefs = getPreferences(appContext);
        Editor edit = prefs.edit();
        edit.putFloat(PREFERENCES_TEXT_SIZE_KEY, size);
        edit.apply();
    }

    public static void applyTextPreferences(TextView view, SharedPreferences prefs) {
        int id = prefs.getInt(PREFERENCES_ALIGNMENT_KEY, -1);
        applyById(view, id);
        id = prefs.getInt(PREFERENCES_DESIGN_KEY, -1);
        applyById(view, id);
        id = prefs.getInt(PREFERENCES_SPACING_KEY, -1);
        applyById(view, id);
        float size = prefs.getFloat(PREFERENCES_TEXT_SIZE_KEY, -1f);
        if (size != -1) {
            view.setTextSize(size);
        }
    }

    public static float getTextSize(Context appContext) {
        SharedPreferences prefs = getPreferences(appContext);
        return prefs.getFloat(PREFERENCES_TEXT_SIZE_KEY, 18f);
    }

    public static void applyTextPreferences(TextView view, Context appContext) {
        SharedPreferences prefs = getPreferences(appContext);
        applyTextPreferences(view, prefs);

    }

    private static void applyById(TextView view, int id) {
        if (view == null) {
            return;
        }

    }

    public static SharedPreferences getPreferences(Context applicationContext) {
        return applicationContext.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public static int getThumbnailWidth(Context context) {
        SharedPreferences prefs = getPreferences(context);
        return prefs.getInt(PREFERENCES_THUMBNAIL_WIDTH, 20);
    }

    public static int getThumbnailHeight(Context context) {
        SharedPreferences prefs = getPreferences(context);
        return prefs.getInt(PREFERENCES_THUMBNAIL_HEIGHT, 20);
    }

    public static void saveThumbnailSize(Context context, int w, int h) {
        SharedPreferences prefs = getPreferences(context);
        Editor edit = prefs.edit();
        edit.putInt(PREFERENCES_THUMBNAIL_WIDTH, w);
        edit.putInt(PREFERENCES_THUMBNAIL_HEIGHT, h);
        edit.apply();
    }

}
