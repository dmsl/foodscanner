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

package com.ds.FoodScanner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.textservice.TextInfo;
import android.view.textservice.TextServicesManager;

import android.widget.Button;
import android.widget.EditText;

import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SpellCheckerSession.SpellCheckerSessionListener;
import android.view.textservice.SuggestionsInfo;

import android.widget.TextView;
import android.widget.Toast;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.lukhnos.portmobile.file.Files;
import org.lukhnos.portmobile.file.Path;
import org.lukhnos.portmobile.file.Paths;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;

public class MySpellChecker{
    Path temp;
    Path dictionaryPath;
    org.apache.lucene.search.spell.SpellChecker spellChecker;
    final int SuggestionsNumber = 1;
    HashSet<String> dictionary;

    public MySpellChecker(String externalFilesDir, AssetManager assetManager) {
        // create dictionary
        dictionary = new HashSet<String>();
        createDictionary(assetManager, "ingredient_Dictionary.txt");

        // copy dictionary to other folder
        copyAssets("ingredient_Dictionary.txt", assetManager, externalFilesDir);
        String p = externalFilesDir + "/ingredient_Dictionary.txt";

        try {
            // initialize lucene
            temp = Files.createTempDirectory(MySpellChecker.class.getCanonicalName());
            Path indexRootPath = Paths.get(temp.toString());
            Directory spellIndexDirectory = FSDirectory.open(indexRootPath);

            dictionaryPath = Paths.get(p);
            spellChecker = new org.apache.lucene.search.spell.SpellChecker(spellIndexDirectory);

            spellChecker.indexDictionary(new PlainTextDictionary(dictionaryPath),  new IndexWriterConfig(null), false);

            //spellChecker.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String luceneSpellchecker(String wordForSuggestions) throws IOException{
        if(!dictionary.contains(wordForSuggestions)){
            String[] suggestions = ((org.apache.lucene.search.spell.SpellChecker) spellChecker).
                    suggestSimilar(wordForSuggestions, SuggestionsNumber);

            if (suggestions!=null && suggestions.length>0) {
                Log.e("Suggestion",suggestions[0]);
                return suggestions[0].toLowerCase();
            }
        }
        return null;
    }

    private void copyAssets(String filename, AssetManager assetManager, String externalFilesDir) {

        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null){
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(externalFilesDir, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private void createDictionary(AssetManager assetManager, String filename) {
        InputStream in = null;

        try {
            in = assetManager.open(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String str;
            if (in != null) {
                while ((str = reader.readLine()) != null) {
                    dictionary.add(str.toLowerCase());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {}
            }
        }

    }

}
