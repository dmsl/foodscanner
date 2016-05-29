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

import com.ds.FoodScanner.All_DB;
import com.ds.FoodScanner.BaseDocumentActivitiy;
import com.ds.FoodScanner.DocumentContentProvider;
import com.ds.FoodScanner.DocumentContentProvider.Columns;
import com.ds.FoodScanner.MySpellChecker;
import com.ds.FoodScanner.R;
import com.ds.FoodScanner.help.HintDialog;
import com.ds.FoodScanner.help.OCRLanguageAdapter;
import com.ds.util.ListUtils;
import com.ds.util.PreferencesUtils;

import android.annotation.SuppressLint;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class DocumentActivity extends BaseDocumentActivitiy implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String LOG_TAG = DocumentActivity.class.getSimpleName();
    private static final String STATE_DOCUMENT_URI = "documet_uri";

    public interface DocumentContainerFragment {
        String getLangOfCurrentlyShownDocument();

        void setCursor(final Cursor cursor);

        String getTextOfAllDocuments();

        void setShowText(boolean text);

        boolean getShowText();
    }

    static final int REQUEST_CODE_TTS_CHECK = 6;
    private static final int REQUEST_CODE_OPTIONS = 4;
    private static final int REQUEST_CODE_TABLE_OF_CONTENTS = 5;
    public static final String EXTRA_ACCURACY = "ask_for_title";
    All_DB db = new All_DB();

    private int mParentId;
    private Cursor mCursor;
    //private TtsActionCallback mActionCallback;
    String grams_ml = "";
    MySpellChecker spellchecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setVolumeControlStream(AudioManager.STREAM_ALARM);
        //getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_document);

        setTitle(getResources().getText(R.string.Ingr));

        setupAnalyzeButton();

        setupAutocorrectButton();


        spellchecker = new MySpellChecker(getExternalFilesDir(null)+"", getAssets());


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (!init(savedInstanceState)) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            showResultDialog();
        }

        setDocumentFragmentType();
        initAppIcon(HINT_DIALOG_ID);
    }

    private void setupAnalyzeButton() {
        Button analyzeButton = (Button) findViewById(R.id.analyze_button);

        analyzeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DocumentActivity.this);
                final EditText txtInput = new EditText(DocumentActivity.this);
                txtInput.setInputType(InputType.TYPE_CLASS_NUMBER);

                dialogBuilder.setTitle("grams/mg");
                dialogBuilder.setView(txtInput);
                dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String plainText = getPlainDocumentText();
                        String upperText = plainText.toLowerCase();
                        String normalText = upperText.replaceAll("[^a-zA-Z0-9(%.)\\-, ]+", "");

                        String ingredients;
                        if (normalText.charAt(normalText.length() - 1) == '.' || normalText.charAt(normalText.length() - 1) == ',')
                            ingredients = normalText.substring(0, normalText.length() - 1);
                        else
                            ingredients = normalText;

                        EditText editText = (EditText) findViewById(R.id.editText_document);
                        editText.setText(ingredients, TextView.BufferType.EDITABLE);
                        /////////////////////////////////////////////////////////////////////////////

                        grams_ml = txtInput.getText().toString();
                        if (grams_ml.compareTo("") == 0)
                            return;


                        double product_grams;
                        try {
                            product_grams = Double.parseDouble(grams_ml);
                        } catch (NumberFormatException nfe) {
                            return;
                        }

                        setContentView(R.layout.results);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        setTitle(getResources().getText(R.string.Results));


                        ArrayList<String> ingr = new ArrayList<String>();
                        ArrayList<Double> percentage = new ArrayList<Double>();
                        ArrayList<String> possibly_allergen = new ArrayList<String>();

                        StringTokenizer st = new StringTokenizer(ingredients, ",");
                        while (st.hasMoreElements()) {
                            StringTokenizer st_parenthesis = new StringTokenizer((String) st.nextElement(), "(");

                            String word = (String) st_parenthesis.nextElement();
                            word = word.replaceAll("\\s+", " ");
                            word = word.replaceAll("^\\s+", "");
                            word = word.replaceAll("\\s+$", "");
                            if (word.length() > 2)
                                ingr.add(word);

                            if (st_parenthesis.hasMoreElements()) {
                                String str = ((String) st_parenthesis.nextElement()).replaceAll("[(%)]+", "");
                                str = str.replaceAll("\\s+", " ");
                                str = str.replaceAll("^\\s+", "");
                                str = str.replaceAll("\\s+$", "");
                                double percentNum;
                                try {
                                    percentNum = Double.parseDouble(str);
                                } catch (NumberFormatException nfe) {
                                    if (str.length() > 2)
                                        possibly_allergen.add(str);
                                }
                            }
                        }

                        ArrayList<HashMap<String, String>> feedlist1 = new ArrayList<HashMap<String,String>>();
                        HashMap<String,String> hm1;// = new HashMap<String, String>();


                        ArrayList<String> data2 = new ArrayList<String>();// allergens.toString()
                        ArrayList<String> data3 = new ArrayList<String>();// chemicals.toString()

                        // Get nutritional facts
                        List<String> temp;
                        List<String> temp2;
                        String[] fields = {"Calories", "Protein", "Lipid", "Carbohydrates",
                                "Fiber", "Sugar", "Calcium", "Iron", "Magnesium", "Phosphorus",
                                "Potassium", "Sodium", "Vit C", "Vit B6", "Vit B12", "Vit A",
                                "Vit E", "Vit D", "Vit K", "Saturated Fat", "Monounsat. Fat",
                                "Polyunsat. Fat", "Cholesterol"
                        };

                        String[] field_metric = {"kJ", "g", "g", "g",
                                "g", "g", "mg", "mg", "mg", "mg",
                                "mg", "mg", "mg", "mg", "μg", "μg",
                                "mg", "μg", "μg", "g", "g",
                                "g", "mg"
                        };

                        double[] nutr_values = new double[fields.length];
                        boolean no_ingr_found = true;

                        double ingrs_size = ingr.size()/5;
                        if(ingrs_size < 1){
                            ingrs_size = 1;
                        }

                        for (int i = 0; i < ingr.size(); i++) {
                            String str = ingr.get(i);

                            Log.e("Search ingr: ",str);
                            temp = db.test(DocumentActivity.this, "SELECT `Energ_kcal`, `Protein_(g)`, " +
                                    "`Lipid_Tot_(g)`, `Carbohydrt_(g)` ,`Fiber_TD_(g)`, `Sugar_Tot_(g)`, " +
                                    "`Calcium_(mg)`, `Iron_(mg)`, `Magnesium_(mg)`, `Phosphorus_(mg)`, " +
                                    "`Potassium_(mg)`, `Sodium_(mg)`, `Vit_C_(mg)`, `Vit_B6_(mg)`, " +
                                    "`Vit_B12_(µg)`, `Vit_A_RAE`, `Vit_E_(mg)`, `Vit_D_µg`, " +
                                    "`Vit_D_IU`, `Vit_K_(µg)`, `FA_Sat_(g)`, `FA_Mono_(g)`, " +
                                    "`FA_Poly_(g)`, `Cholestrl_(mg)`, `Long_Desc` " +
                                    "FROM NUTR_FACTS WHERE Long_Desc like '%" + str + "%'");
                            if (temp.size() != 0 && temp.get(0).compareTo("N/A") != 0) {
                                no_ingr_found = false;
                                for (int j = 0; j < fields.length; j++) {
                                    if(temp.get(j).compareTo("null")==0){
                                        nutr_values[j] = 0;
                                    }else{
                                        if(field_metric[j].compareTo("μg")==0){
                                            nutr_values[j] += (Double.parseDouble(temp.get(j))/(i+1));//1000));
                                            field_metric[j] = new String("g");
                                        }else if(j <= 11 && j >= 6){
                                            nutr_values[j] += (Double.parseDouble(temp.get(j))/(i+1));///30000;
                                            //field_metric[j] = new String("g");
                                        }else if(field_metric[j].compareTo("kJ")==0){
                                            nutr_values[j] += (Double.parseDouble(temp.get(j))/(i+1));//0.4
                                            //field_metric[j] = new String("g");
                                        }else{
                                            nutr_values[j] += (Double.parseDouble(temp.get(j))/(i+1+2.5));//0.6
                                            field_metric[j] = new String("g");
                                        }
                                    }
                                }
                            } else {
                                Log.e(">>>>>>>>>>>>>>>>>>>>>>>", str + ": N/A");

                                StringTokenizer ingrNotFoundTok = new StringTokenizer(str);
                                int num_of_ingrs =  ingrNotFoundTok.countTokens();

                                while (ingrNotFoundTok.hasMoreTokens()) {
                                    String ingr_str = ingrNotFoundTok.nextToken();
                                    Log.e("         String", ingr_str);
                                    temp = db.test(DocumentActivity.this, "SELECT `Energ_kcal`, `Protein_(g)`, " +
                                            "`Lipid_Tot_(g)`, `Carbohydrt_(g)` ,`Fiber_TD_(g)`, `Sugar_Tot_(g)`, " +
                                            "`Calcium_(mg)`, `Iron_(mg)`, `Magnesium_(mg)`, `Phosphorus_(mg)`, " +
                                            "`Potassium_(mg)`, `Sodium_(mg)`, `Vit_C_(mg)`, `Vit_B6_(mg)`, " +
                                            "`Vit_B12_(µg)`, `Vit_A_RAE`, `Vit_E_(mg)`, `Vit_D_µg`, " +
                                            "`Vit_D_IU`, `Vit_K_(µg)`, `FA_Sat_(g)`, `FA_Mono_(g)`, " +
                                            "`FA_Poly_(g)`, `Cholestrl_(mg)`, `Long_Desc` " +
                                            "FROM NUTR_FACTS WHERE Long_Desc like '%" + ingr_str + "%'");
                                    if (temp.size() != 0 && temp.get(0).compareTo("N/A") != 0) {
                                        no_ingr_found = false;
                                        for (int j = 0; j < fields.length; j++) {
                                            if(temp.get(j).compareTo("null")==0){
                                                nutr_values[j] = 0;
                                            }else{
                                                if(field_metric[j].compareTo("μg")==0){
                                                    nutr_values[j] += (Double.parseDouble(temp.get(j))/(i+1));///(i+1000));
                                                    //field_metric[j] = new String("g");
                                                }else if(j <= 11 && j >= 6){
                                                    nutr_values[j] += (Double.parseDouble(temp.get(j))/(i+1));///30000;
                                                    //field_metric[j] = new String("g");
                                                }else if(field_metric[j].compareTo("kJ")==0){
                                                    nutr_values[j] += (Double.parseDouble(temp.get(j))/(i+1+1));//0.4
                                                    //field_metric[j] = new String("g");
                                                }else{
                                                    nutr_values[j] += (Double.parseDouble(temp.get(j))/(i+1+2.5));// .6
                                                    //field_metric[j] = new String("g");
                                                }
                                                //nutr_values[j] += (Double.parseDouble(temp.get(j))/(i+1+num_of_ingrs));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if(no_ingr_found){
                            AlertDialog alertDialog = new AlertDialog.Builder(DocumentActivity.this).create();
                            alertDialog.setTitle("Ingredients Not Found");
                            alertDialog.setMessage("The ingredients could not be found.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                            return;
                        }

                        field_metric[0] = "";

                        DecimalFormat df2 = new DecimalFormat("#.##");
                        StringBuilder nutr_facts = new StringBuilder();
                        nutr_facts.append("");

                        for (int i = 0; i < fields.length; i++) {
                            if(nutr_values[i] != -1){
                                double f = (nutr_values[i] * product_grams) / 100;
                                if(f > 0.0099){
                                    nutr_facts.append(fields[i] + ": " + df2.format(f) + field_metric[i] + "\n");

                                    hm1 = new HashMap<String, String>();
                                    hm1.put("first", fields[i]);
                                    hm1.put("second", df2.format(f) + field_metric[i]);
                                    feedlist1.add(hm1);
                                }
                            }
                        }
                        nutr_facts.append("\n");

                        Log.e(">>>>>>>>>>>>>>>>>>>>>>>", nutr_facts.toString());


                        // Get allergens
                        StringBuilder allergens = new StringBuilder("");
                        HashSet hs = new HashSet();

                        for (int i = 0; i < ingr.size(); i++) {
                            StringTokenizer s = new StringTokenizer(ingr.get(i));
                            while (s.hasMoreTokens()) {
                                String str = s.nextToken();
                                temp = db.test(DocumentActivity.this, "SELECT nameOfAllergen FROM ALLERGENS WHERE nameOfAllergen like '%" + "Tuna" + "%'");
                                Log.e(">>>>>>>>>>>>>>>>>>>>>>>", str.length() + ", " + str + "," + temp);

                                if (temp.size() > 0 && !hs.contains(str) && str.length() > 2) {
                                    boolean flag = false;
                                    for(int k=0; k < temp.size(); k++){
                                        StringTokenizer w = new StringTokenizer(temp.get(k));
                                        while (w.hasMoreTokens()) {
                                            String a = w.nextToken();
                                            if(Math.abs(a.length() - str.length()) < 2 &&
                                                    (a.charAt(0) == str.charAt(0) || a.charAt(a.length()-1) == str.charAt(str.length()-1))){
                                                //Log.e(">>>>>>>>hereeeeeeee", a + ", " + str);
                                                flag = true;
                                            }
                                        }
                                    }
                                    if(flag){
                                        hs.add(str);
                                        allergens.append(str + "\n");
                                        data2.add(str);
                                    }
                                }
                            }
                        }

                        for (int i = 0; i < possibly_allergen.size(); i++) {
                            StringTokenizer s = new StringTokenizer(possibly_allergen.get(i));
                            while (s.hasMoreTokens()) {
                                String str = s.nextToken();
                                temp = db.test(DocumentActivity.this, "SELECT nameOfAllergen FROM ALLERGENS WHERE nameOfAllergen like '%" + str + "%'");
                                //Log.e(">>>>>>>>>>>>>>>>>>>>>>>", str.length() + ", " + str + "," + temp);
                                if (temp.size() > 0 && !hs.contains(str) && str.length() > 2) {
                                    boolean flag = false;

                                    for(int k=0; k < temp.size(); k++){
                                        StringTokenizer w = new StringTokenizer(temp.get(k));
                                        while (w.hasMoreTokens()) {
                                            String a = w.nextToken();
                                            if(Math.abs(a.length() - str.length()) < 2 &&
                                                    (a.charAt(0) == str.charAt(0) || a.charAt(a.length()-1) == str.charAt(str.length()-1))){
                                                //Log.e(">>>>>>>>hereeeeeeee", a + ", " + str);
                                                flag = true;
                                            }
                                        }
                                    }
                                    if(flag){
                                        hs.add(str);
                                        allergens.append(str + "\n");
                                        data2.add(str);
                                    }

                                }
                            }
                        }
                        allergens.append("\n");
                        Log.e(">>>>>>>>>>>>>>>>>>>>>>>", allergens.toString());


                        // Get chemicals
                        StringBuilder chemicals = new StringBuilder("");

                        for (int i = 0; i < ingr.size(); i++) {
                            String str = ingr.get(i);
                            temp = db.test(DocumentActivity.this, "SELECT name FROM CHEMICALS WHERE name like '%" + str + "%'");
                            temp2 = db.test(DocumentActivity.this, "SELECT abbreviation FROM CHEMICALS WHERE abbreviation like '%" + str + "%'");
                            if (temp.size() > 0 || temp2.size() > 0) {
                                chemicals.append("        " + str + "\n");
                            }
                        }

                        for (int i = 0; i < possibly_allergen.size(); i++) {
                            String str = possibly_allergen.get(i);
                            temp = db.test(DocumentActivity.this, "SELECT name FROM CHEMICALS WHERE name like '%" + str + "%'");
                            temp2 = db.test(DocumentActivity.this, "SELECT abbreviation FROM CHEMICALS WHERE abbreviation like '%" + str + "%'");
                            if (temp.size() > 0 || temp2.size() > 0) {
                                chemicals.append("        " + str + "\n");
                            }
                        }

                        Log.e(">>>>>>>>>>>>>>>>>>>>>>>", chemicals.toString());


                        ListView mListView1 = (ListView)findViewById(R.id.listView1);
                        ListView mListView2 = (ListView)findViewById(R.id.listView2);
                        ListView mListView3 = (ListView)findViewById(R.id.listView3);

                        mListView2.setAdapter(new ArrayAdapter<String>(DocumentActivity.this, android.R.layout.simple_list_item_1, data2));
                        mListView3.setAdapter(new ArrayAdapter<String>(DocumentActivity.this, android.R.layout.simple_list_item_1, data3));

                        SimpleAdapter simpleAdapter1 = new SimpleAdapter(DocumentActivity.this, feedlist1, R.layout.activity_listview, new String[]{"first", "second"}, new int[]{R.id.name, R.id.num});
                        mListView1.setAdapter(simpleAdapter1);

                        ListUtils.setDynamicHeight(mListView1);
                        ListUtils.setDynamicHeight(mListView2);
                        ListUtils.setDynamicHeight(mListView3);

                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();

            }
        });
    }

    private void setupAutocorrectButton() {
        Button autocorrectButton = (Button) findViewById(R.id.autocorrect_button);

        autocorrectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String plainText = getPlainDocumentText();
                String upperText = plainText.toLowerCase();
                String normalText = upperText.replaceAll("[^a-zA-Z0-9(%.)\\-, ]+", "");
                normalText = normalText.replace('.',',');

                String ingredients;
                if (normalText.charAt(normalText.length() - 1) == '.' || normalText.charAt(normalText.length() - 1) == ',')
                    ingredients = normalText.substring(0, normalText.length() - 1);
                else
                    ingredients = normalText;

                boolean flag = true;
                StringBuilder correctText = new StringBuilder();
                StringBuilder word = new StringBuilder();
                Log.e(">>>", ingredients);

                for (int i = 0; i < ingredients.length(); i++) {

                    if(ingredients.charAt(i) == ',' || ingredients.charAt(i) == '.' ||
                            ingredients.charAt(i) == '(' || ingredients.charAt(i) == ')' ||
                            ingredients.charAt(i) == ' '){
                        // call lucene spellchecker
                        Log.e(">>", word.toString());
                        try {
                            if(word.length() > 0) {
                                String s = spellchecker.luceneSpellchecker(word.toString());
                                if (s != null) {
                                    correctText.append(s);
                                } else {
                                    correctText.append(word.toString());
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // create new string
                        word = new StringBuilder();
                        correctText.append(ingredients.charAt(i));
                        flag = false;
                    }else{ // found letter
                        Log.e("Here", ingredients.charAt(i) + ":" + (ingredients.charAt(i)-'0'));
                        word.append(ingredients.charAt(i));
                        flag = true;
                    }
                }

                if(flag){
                    correctText.append(word);
                }

                EditText editText = (EditText) findViewById(R.id.editText_document);
                editText.setText(correctText, TextView.BufferType.EDITABLE);

//                StringTokenizer st = new StringTokenizer(ingredients, ",");
//                while (st.hasMoreElements())
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            setIntent(intent);
            showResultDialog();
        }
    }

    private void showResultDialog() {
        int accuracy = getIntent().getIntExtra(EXTRA_ACCURACY, -1);
        int numberOfSuccessfulScans = PreferencesUtils.getNumberOfSuccessfulScans(getApplicationContext());
        if (accuracy >= OCRResultDialog.MEDIUM_ACCURACY) {
            PreferencesUtils.setNumberOfSuccessfulScans(getApplicationContext(), ++numberOfSuccessfulScans);
        }
        if (numberOfSuccessfulScans == 2) {
            //GetOpinionDialog.newInstance().show(getSupportFragmentManager(), GetOpinionDialog.TAG);
            PreferencesUtils.setNumberOfSuccessfulScans(getApplicationContext(), ++numberOfSuccessfulScans);
        } else if (accuracy > -1) {
            OCRResultDialog.newInstance(accuracy).show(getSupportFragmentManager(), OCRResultDialog.TAG);
        }

    }

    //@Override
    public void onContinueClicked() {
        int accuracy = getIntent().getIntExtra(EXTRA_ACCURACY, 0);
        OCRResultDialog.newInstance(accuracy).show(getSupportFragmentManager(), OCRResultDialog.TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.document_activity_options, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        if (itemId == R.id.item_view_mode) {
            DocumentContainerFragment fragment = (DocumentContainerFragment) getSupportFragmentManager().findFragmentById(R.id.document_fragment_container);
            fragment.setShowText(!fragment.getShowText());
            return true;
        }else if (itemId == R.id.item_content) {
            Intent tocIndent = new Intent(this, TableOfContentsActivity.class);
            Uri uri = Uri.parse(DocumentContentProvider.CONTENT_URI + "/" + getParentId());
            tocIndent.setData(uri);
            startActivityForResult(tocIndent, REQUEST_CODE_TABLE_OF_CONTENTS);
            return true;
        } else if (itemId == R.id.item_delete) {
            Set<Integer> idToDelete = new HashSet<>();
            idToDelete.add(getParentId());
            new DeleteDocumentTask(idToDelete, true).execute();
            return true;
        }else if (itemId == R.id.item_copy_to_clipboard) {
            copyTextToClipboard();
            return true;
        }else if (itemId == R.id.item_share_text) {
            shareText();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    void copyTextToClipboard() {
        final String text = getPlainDocumentText();
        //some apps don't like html text
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//			copyHtmlTextToClipboard(htmlText, text);
//		} else 
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            copyTextToClipboardNewApi(text);
        } else {
            copyTextToClipboard(text);
        }

        Toast.makeText(this, getString(R.string.text_was_copied_to_clipboard), Toast.LENGTH_LONG).show();
    }

    String getLanguageOfDocument() {
        return getDocumentContainer().getLangOfCurrentlyShownDocument();
    }

    String getPlainDocumentText() {
        final String htmlText = getDocumentContainer().getTextOfAllDocuments();
        if (htmlText != null) {
            return Html.fromHtml(htmlText).toString();
        } else {
            return null;
        }
    }

    void shareText() {
        String shareBody = getPlainDocumentText();
        if (shareBody == null) {
            Toast.makeText(DocumentActivity.this, R.string.empty_document, Toast.LENGTH_LONG).show();
            return;
        }

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.share_subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_chooser_title)));
    }


    @SuppressLint("NewApi")
    private void copyTextToClipboardNewApi(final String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.app_name), text);
        clipboard.setPrimaryClip(clip);
    }

    @SuppressWarnings("deprecation")
    private void copyTextToClipboard(String text) {
        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);
    }

//    public void onTtsLanguageChosen(OCRLanguageAdapter.OCRLanguage lang) {
//        mActionCallback.onTtsLanguageChosen(lang);
//    }

//    public void onTtsCancelled() {
//        mActionCallback.onTtsCancelled();
//    }

//    public boolean isTtsLanguageAvailable(OCRLanguageAdapter.OCRLanguage lang) {
//        return mActionCallback.isLanguageAvailable(lang);
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_TTS_CHECK) {
            //mActionCallback.onTtsCheck(resultCode);
        } else if (requestCode == REQUEST_CODE_OPTIONS) {
            Fragment frag = getSupportFragmentManager().findFragmentById(R.id.document_fragment_container);
            if (frag instanceof DocumentPagerFragment) {
                DocumentPagerFragment pagerFragment = (DocumentPagerFragment) frag;
                pagerFragment.applyTextPreferences();
            }
        } else if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_TABLE_OF_CONTENTS:
                    int documentPos = data.getIntExtra(TableOfContentsActivity.EXTRA_DOCUMENT_POS, -1);
                    DocumentContainerFragment fragment = getDocumentContainer();
                    if (fragment != null) {
                        ((DocumentPagerFragment) fragment).setDisplayedPage(documentPos);
                    }
                    break;
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case HINT_DIALOG_ID:
                return HintDialog.createDialog(this, R.string.document_help_title, "file:///android_res/raw/document_help.html");
        }
        return super.onCreateDialog(id, args);
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(STATE_DOCUMENT_URI, getIntent().getData());
    }


    private boolean init(Bundle savedInstanceState) {
        Uri data = getIntent().getData();
        if (data == null && savedInstanceState != null) {
            data = savedInstanceState.getParcelable(STATE_DOCUMENT_URI);
        }
        if (data == null) {
            return false;
        }
        String id = data.getLastPathSegment();
        int parentId = getParentId(data);
        // Base class needs that value
        if (parentId == -1) {
            mParentId = Integer.parseInt(id);
        } else {
            mParentId = parentId;
        }

        getSupportLoaderManager().initLoader(0, null, this);
        return true;
    }

    private int getParentId(Uri documentUri) {
        int parentId = -1;
        Cursor c = getContentResolver().query(documentUri, new String[]{Columns.PARENT_ID}, null, null, null);
        if (!c.moveToFirst()) {
            return parentId;
        }
        int index = c.getColumnIndex(Columns.PARENT_ID);
        if (index > -1) {
            parentId = c.getInt(index);
        }
        c.close();
        return parentId;
    }

    @Override
    protected int getParentId() {
        return mParentId;
    }

    public DocumentContainerFragment getDocumentContainer() {
        return (DocumentContainerFragment) getSupportFragmentManager().findFragmentById(R.id.document_fragment_container);
    }

    private void setDocumentFragmentType() {
        // Check what fragment is shown, replace if needed.
        DocumentContainerFragment fragment = getDocumentContainer();
        DocumentContainerFragment newFragment = null;
        if (fragment == null) {
            newFragment = new DocumentPagerFragment();
        }
        if (newFragment != null) {
            if (mCursor != null) {
                newFragment.setCursor(mCursor);
            }
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.document_fragment_container, (Fragment) newFragment);
            //ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            ft.commit();
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.i(LOG_TAG, "onLoadFinished");
        mCursor = cursor;
        DocumentContainerFragment frag = getDocumentContainer();
        frag.setCursor(cursor);
        if (getIntent().getData() != null) {
            String id = getIntent().getData().getLastPathSegment();
            DocumentPagerFragment documentContainer = (DocumentPagerFragment) getDocumentContainer();
            documentContainer.setDisplayedPageByDocumentId(Integer.parseInt(id));
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onCreateLoader");

        return new CursorLoader(this, DocumentContentProvider.CONTENT_URI, null, Columns.PARENT_ID + "=? OR " + Columns.ID + "=?", new String[]{String.valueOf(mParentId),
                String.valueOf(mParentId)}, "created ASC");
    }

}
