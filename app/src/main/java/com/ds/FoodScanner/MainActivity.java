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
package com.ds.FoodScanner;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ds.util.ListUtils;
import com.fatsecret.platform.FatSecretAPI;
import com.ds.FoodScanner.BarcodeScanner.FullScannerActivity;
import com.ds.FoodScanner.help.AboutActivity;
import com.ds.FoodScanner.help.HelpActivity;
import com.ds.FoodScanner.help.HintDialog;
import com.ds.documentview.DocumentActivity;
import com.ds.drawable.CrossFadeDrawable;
import com.ds.drawable.FastBitmapDrawable;
import com.ds.install.InstallActivity;
import com.ds.util.Util;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;


public class MainActivity extends BaseDocumentActivitiy
        implements NavigationView.OnNavigationItemSelectedListener,DocumentAdapter.OnCheckedChangeListener, LoaderManager.LoaderCallbacks<Cursor>  {

    private DocumentAdapter mDocumentAdapter;
    private GridView mGridView;
    private static final int MESSAGE_UPDATE_THUMNAILS = 1;
    private static final int DELAY_SHOW_THUMBNAILS = 550;
    private static final String SAVE_STATE_KEY = "selection";
    private static final int JOIN_PROGRESS_DIALOG = 4;
    private ActionMode mActionMode;
    private static final int REQUEST_CODE_INSTALL = 234;
    /**
     * global state
     */
    private static boolean sIsInSelectionMode = false;

    private boolean mFingerUp = true;
    private int mScrollState = AbsListView.OnScrollListener.SCROLL_STATE_IDLE;
    private final Handler mScrollHandler = new ScrollHandler();
    private boolean mPendingThumbnailUpdate = false;

    private static final int ZXING_CAMERA_PERMISSION = 1;
    private Class<?> mClss;
    FloatingActionMenu menuLabelsRightClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View v_fab_menu = (View) findViewById(R.id.menu_labels_right);
        final FloatingActionMenu menuLabelsRight = (FloatingActionMenu) v_fab_menu;
        menuLabelsRight.setClosedOnTouchOutside(true);
        menuLabelsRightClose = menuLabelsRight;

        View menu_fab1 = (View) findViewById(R.id.menu_fab1);
        menu_fab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startGallery();
                menuLabelsRight.close(true);
            }
        });

        View menu_fab2 = (View) findViewById(R.id.menu_fab2);
        menu_fab2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startCamera();
                menuLabelsRight.close(true);
            }
        });

        View menu_fab3 = (View) findViewById(R.id.menu_fabBarcode);
        menu_fab3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchFullActivity(findViewById(R.id.menu_fabBarcode));
                menuLabelsRight.close(true);
            }
        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        initAppIcon(HINT_DIALOG_ID);
        initGridView();
        startInstallActivityIfNeeded();
        final int columnWidth = Util.determineThumbnailSize(this, null);
        Util.setThumbnailSize(columnWidth, columnWidth, this);
		if(savedInstanceState==null) {
			checkForImageIntent(getIntent());
		}

    }

    @Override
    public void onBackPressed() {
        boolean flag1 = false;
        boolean flag2 = false;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            flag1 = true;
        }

        if (mDocumentAdapter.getSelectedDocumentIds().size() > 0) {
            cancelMultiSelectionMode();
        } else {
            flag2 = true;
        }

        if(flag1 || flag2){
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_tips) {
            startActivity(new Intent(this,HelpActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        checkForImageIntent(intent);
    }

    private void checkForImageIntent(Intent intent) {
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            if (imageUri != null) {
                loadBitmapFromContentUri(imageUri, ImageSource.INTENT);
            } else {
                showFileError(PixLoadStatus.IMAGE_COULD_NOT_BE_READ, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            }

        }
    }

    public void launchFullActivity(View v) {
        launchActivity(FullScannerActivity.class);
    }


    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivityForResult(intent,1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(mClss != null) {
                        Intent intent = new Intent(this, mClss);
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(this, "Please grant camera permission to use the QR Scanner", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }

    /**
     * Start the InstallActivity if possible and needed.
     */
    private void startInstallActivityIfNeeded() {
        final String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            if (!InstallActivity.IsInstalled(this)) {
                // install the languages if needed, create directory structure
                // (one
                // time)
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setClassName(this, com.ds.install.InstallActivity.class.getName());
                startActivityForResult(intent, REQUEST_CODE_INSTALL);
            }
        } else {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            // alert.setTitle(R.string.no_sd_card);
            alert.setMessage(getString(R.string.no_sd_card));
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_INSTALL) {
            if (RESULT_OK == resultCode) {
                // install successfull, show happy fairy or introduction text
            } else {
                // install failed, quit immediately
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1) {
                if(resultCode == RESULT_OK){

                    //flag_folder
                    menuLabelsRightClose.close(true);
                    String barcode = data.getStringExtra("barcode");
                    //Toast toast = Toast.makeText(getApplicationContext(), barcode, Toast.LENGTH_LONG);
                    //toast.show();

                    String title = "";
                    String message = "";
                    boolean flag = false;
                    //if(barcode != null)
                    String[] separated1 = barcode.split(",");
                    //if(separated1 != null)
                    String[] separated2 = separated1[0].split("=");
                   // if(separated2 != null)
                    String[] separated3 = separated2[1].split(" ");
                    //if(separated3 != null)

                    Log.e("Barcode", separated1[1]);
                    Log.e("Barcode",separated3[1]);
                    String barcodeF = separated3[1];
                    /////////////////////////////////////// call api of fatsecret /////////////////////////////////////
                    FatSecretAPI api = new FatSecretAPI("7b287bf771e54c16a1dc44fb970a301a", "94833415af944b39affc633ccbbc6dfb");
                    try {
                        Pair<JSONObject,String> output = api.new RetrieveFoodIdUsingBarcode()
                                .execute(barcodeF)
                                .get();

                        if(output.second.compareTo("Connection Error") == 0){
                            flag = true;
                            title = "No Internet Connection";
                            message = "Make sure you 're connected to a WiFi or mobile network and try again.";
                        }else if(output.second.compareTo("OK") == 0){

                            String foodId = output.first.getJSONObject("result").getJSONObject("food_id").get("value").toString();
                            if(foodId.compareTo("0") == 0){
                                flag = true;
                                title = "Food Not Found";
                                message = "We couldn't found the barcode. Please use the OCR.";
                            }else{
                                Pair<JSONObject,String> food_info = api.new RetrieveFoodUsingId()
                                        .execute(foodId)
                                        .get();

                                Log.e("Finish",food_info.first.getJSONObject("result")
                                        .getJSONObject("food")
                                        .getJSONObject("servings")
                                        .getJSONObject("serving").toString());

                                JSONObject jsObj = food_info.first.getJSONObject("result")
                                        .getJSONObject("food")
                                        .getJSONObject("servings")
                                        .getJSONObject("serving");

                                ArrayList<String> nutritionalFacts = new ArrayList<String>();
                                ArrayList<String> temp = new ArrayList<String>();

                                for(Iterator<String> iter = jsObj.keys(); iter.hasNext();) {
                                    String key = iter.next();
                                    if(key.compareTo("measurement_description") != 0 &&
                                            key.compareTo("metric_serving_amount") != 0 &&
                                            key.compareTo("metric_serving_unit") != 0 &&
                                            key.compareTo("number_of_units") != 0 &&
                                            key.compareTo("serving_url") != 0 &&
                                            key.compareTo("serving_id") != 0 &&
                                            key.compareTo("serving_description") != 0){

                                        if(key.compareTo("calories") == 0 ||
                                                key.compareTo("fat") == 0 ||
                                                key.compareTo("carbohydrate") == 0){

                                            nutritionalFacts.add(key);
                                        } else{
                                          temp.add(key);
                                        }
                                    }
                                }
                                nutritionalFacts.addAll(temp);
                                nutritionalFacts.add("serving_description");


                                StringBuilder nutritionalInfo = new StringBuilder("");
                                ArrayList<HashMap<String, String>> feedlist1 = new ArrayList<HashMap<String,String>>();
                                HashMap<String,String> hm1;
                                int i;
                                for (i = 0; i < nutritionalFacts.size() - 1; i++){
                                    nutritionalInfo.append(nutritionalFacts.get(i).replaceAll("_", " ") + ": " + jsObj.get(nutritionalFacts.get(i)) + "g" + "\n");

                                    hm1 = new HashMap<String, String>();
                                    String input =  nutritionalFacts.get(i).replaceAll("_", " ");
                                    if(input.compareTo("calories")==0){
                                        hm1.put("first", input.substring(0, 1).toUpperCase() + input.substring(1));
                                        hm1.put("second", jsObj.get(nutritionalFacts.get(i)) + "");
                                    }else if(input.compareTo("polyunsaturated fat")==0){
                                        hm1.put("first", "Polyunsat. Fat");
                                        hm1.put("second", jsObj.get(nutritionalFacts.get(i)) + "g");
                                    }else if(input.compareTo("monounsaturated fat")==0){
                                        hm1.put("first", "Monounsat. Fat");
                                        hm1.put("second", jsObj.get(nutritionalFacts.get(i)) + "g");
                                    }else{
                                        hm1.put("first", input.substring(0, 1).toUpperCase() + input.substring(1));
                                        hm1.put("second", jsObj.get(nutritionalFacts.get(i)) + "g");
                                    }
                                    feedlist1.add(hm1);
                                }
                                nutritionalInfo.append(nutritionalFacts.get(i).replaceAll("_", " ") + ": " + jsObj.get(nutritionalFacts.get(i)) + "\n");
                                hm1 = new HashMap<String, String>();
                                String input =  nutritionalFacts.get(i).replaceAll("_", " ");
                                hm1.put("first", input.substring(0, 1).toUpperCase() + input.substring(1));
                                hm1.put("second", jsObj.get(nutritionalFacts.get(i)) +  "");
                                feedlist1.add(hm1);

                                Intent act = new Intent(getApplicationContext(), ResultActivity.class);
                                Bundle args = new Bundle();
                                args.putSerializable("feedlist",(Serializable)feedlist1);
                                act.putExtra("BUNDLE",args);
                                startActivity(act);
                            }

                        }else{
                            flag = true;
                            title = "Error";
                            message = output.second;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if(flag){
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle(title);
                        alertDialog.setMessage(message);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                    }
                    /////////////////////////////////////// End call /////////////////////////////////////

                }
            }else if(requestCode == 10){
                if(resultCode == RESULT_OK) {

                }
            }
        }
    }

    public static boolean isInSelectionMode() {
        return sIsInSelectionMode;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        //mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case HINT_DIALOG_ID:
                return HintDialog.createDialog(this, R.string.document_list_help_title, "file:///android_res/raw/document_list_help.html");
        }
        return super.onCreateDialog(id, args);
    }

    @Override
    public void onCheckedChanged(Set<Integer> checkedIds) {
        if (mActionMode == null && checkedIds.size() > 0) {
            mActionMode = startSupportActionMode(new DocumentActionCallback());
        } else if (mActionMode != null && checkedIds.size() == 0) {
            mActionMode.finish();
            mActionMode = null;
        }

        if (mActionMode != null) {
            // change state of action mode depending on the selection
            final MenuItem editItem = mActionMode.getMenu().findItem(R.id.item_edit_title);
            if (checkedIds.size() == 1) {
                editItem.setVisible(true);
                editItem.setEnabled(true);
            } else {
                editItem.setVisible(false);
                editItem.setEnabled(false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Set<Integer> selection = mDocumentAdapter.getSelectedDocumentIds();
        ArrayList<Integer> save = new ArrayList<Integer>(selection.size());
        save.addAll(selection);
        outState.putIntegerArrayList(SAVE_STATE_KEY, save);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Integer> selection = savedInstanceState.getIntegerArrayList(SAVE_STATE_KEY);
        mDocumentAdapter.setSelectedDocumentIds(selection);
    }

    public class DocumentClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DocumentAdapter.DocumentViewHolder holder = (DocumentAdapter.DocumentViewHolder) view.getTag();
            if (sIsInSelectionMode) {
                holder.gridElement.toggle();
            } else {
                Intent i = new Intent(MainActivity.this, DocumentActivity.class);
                long documentId = mDocumentAdapter.getItemId(position);
                Uri uri = Uri.withAppendedPath(DocumentContentProvider.CONTENT_URI, String.valueOf(documentId));
                i.setData(uri);
                startActivity(i);
            }
        }
    }
    AdapterView<?> parentOut;
    View viewOut;

    private class DocumentLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            Log.e("Long","ok");
            CheckableGridElement clicked = (CheckableGridElement) view;
            if (sIsInSelectionMode == false) {
                sIsInSelectionMode = true;
                clicked.toggle();
                final int childCount = parent.getChildCount();
                parentOut = parent;
                viewOut = view;
                for (int i = 0; i < childCount; i++) {
                    CheckableGridElement element = (CheckableGridElement) parent.getChildAt(i);
                    if (element != view) {
                        element.setChecked(false);
                    }
                }
            } else {
                clicked.toggle();
            }
            return true;
        }
    }

    int getScrollState() {
        return mScrollState;
    }

    private class DocumentScrollListener implements AbsListView.OnScrollListener {
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mScrollState == SCROLL_STATE_FLING && scrollState != SCROLL_STATE_FLING) {
                final Handler handler = mScrollHandler;
                final Message message = handler.obtainMessage(MESSAGE_UPDATE_THUMNAILS, MainActivity.this);
                handler.removeMessages(MESSAGE_UPDATE_THUMNAILS);
                handler.sendMessageDelayed(message, mFingerUp ? 0 : DELAY_SHOW_THUMBNAILS);
                mPendingThumbnailUpdate = true;
            } else if (scrollState == SCROLL_STATE_FLING) {
                mPendingThumbnailUpdate = false;
                mScrollHandler.removeMessages(MESSAGE_UPDATE_THUMNAILS);
            }

            mScrollState = scrollState;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    }

    private static class ScrollHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_UPDATE_THUMNAILS:
                    ((MainActivity) msg.obj).updateDocumentThumbnails();
                    break;
            }
        }
    }

    private class FingerTracker implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent event) {
            final int action = event.getAction();
            mFingerUp = action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL;
            if (mFingerUp && mScrollState != DocumentScrollListener.SCROLL_STATE_FLING) {
                postDocumentThumbnails();
            }
            return false;
        }
    }

    public boolean isPendingThumbnailUpdate() {
        return mPendingThumbnailUpdate;
    }

    private void updateDocumentThumbnails() {
        mPendingThumbnailUpdate = false;

        final GridView grid = mGridView;
        final int count = grid.getChildCount();

        for (int i = 0; i < count; i++) {
            final View view = grid.getChildAt(i);
            final DocumentAdapter.DocumentViewHolder holder = (DocumentAdapter.DocumentViewHolder) view.getTag();
            if (holder.updateThumbnail == true) {
                final int documentId = holder.documentId;
                CrossFadeDrawable d = holder.transition;
                FastBitmapDrawable thumb = Util.getDocumentThumbnail(documentId);
                d.setEnd(thumb.getBitmap());
                holder.gridElement.setImage(d);
                d.startTransition(375);
                holder.updateThumbnail = false;
            }
        }

        grid.invalidate();
    }

    private void postDocumentThumbnails() {
        Handler handler = mScrollHandler;
        Message message = handler.obtainMessage(MESSAGE_UPDATE_THUMNAILS, MainActivity.this);
        handler.removeMessages(MESSAGE_UPDATE_THUMNAILS);
        mPendingThumbnailUpdate = true;
        handler.sendMessage(message);
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        final AnimationDrawable animation = (AnimationDrawable) getResources().getDrawable(R.drawable.textfairy_title);
        getSupportActionBar().setIcon(animation);
        animation.start();
    }

    private class DocumentActionCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.grid_action_mode, menu);
            return true;
        }


        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();

            if (itemId == R.id.item_edit_title) {
                final Set<Integer> selectedDocs = mDocumentAdapter.getSelectedDocumentIds();
                final int documentId = selectedDocs.iterator().next();
                getSupportLoaderManager().initLoader(documentId, null, MainActivity.this);
                cancelMultiSelectionMode();
                return true;
            }else if (itemId == R.id.item_delete) {
                new DeleteDocumentTask(mDocumentAdapter.getSelectedDocumentIds(), false).execute();
                cancelMultiSelectionMode();
                mode.finish();
                return true;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.e("Destroy","ActionMode");
            final int childCount = parentOut.getChildCount();

            final Set<Integer> selectedDocs = mDocumentAdapter.getSelectedDocumentIds();

            for (int i = 0; i < childCount; i++) {
                CheckableGridElement element = (CheckableGridElement) parentOut.getChildAt(i);
                if (element != viewOut) {
                    element.setChecked(true);
                }
            }

            if (mActionMode != null) {
                mActionMode = null;
                cancelMultiSelectionMode();
            }

        }

    }


    private void initGridView() {
        mGridView = (GridView) findViewById(R.id.gridview);
        mDocumentAdapter = new DocumentAdapter(this, R.layout.document_element, this);
        registerForContextMenu(mGridView);
        mGridView.setAdapter(mDocumentAdapter);
        mGridView.setLongClickable(true);
        mGridView.setOnItemClickListener(new DocumentClickListener());
        mGridView.setOnItemLongClickListener(new DocumentLongClickListener());
        mGridView.setOnScrollListener(new DocumentScrollListener());
        mGridView.setOnTouchListener(new FingerTracker());
        final int[] outNum = new int[1];
        final int columnWidth = Util.determineThumbnailSize(this, outNum);
        mGridView.setColumnWidth(columnWidth);
        mGridView.setNumColumns(outNum[0]);
        final View emptyView = findViewById(R.id.empty_view);
        mGridView.setEmptyView(emptyView);
    }

    @Override
    protected int getParentId() {
        return -1;
    }

    public void cancelMultiSelectionMode() {
        mDocumentAdapter.getSelectedDocumentIds().clear();
        sIsInSelectionMode = false;
        final int childCount = mGridView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View v = mGridView.getChildAt(i);
            final DocumentAdapter.DocumentViewHolder holder = (DocumentAdapter.DocumentViewHolder) v.getTag();
            holder.gridElement.setChecked(false);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case JOIN_PROGRESS_DIALOG:
                ProgressDialog d = new ProgressDialog(this);
                d.setTitle(R.string.join_documents_title);
                d.setIndeterminate(true);
                return d;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int documentId, final Bundle bundle) {
        final Uri uri = Uri.withAppendedPath(DocumentContentProvider.CONTENT_URI, String.valueOf(documentId));
        return new CursorLoader(this, uri, new String[] { DocumentContentProvider.Columns.TITLE, DocumentContentProvider.Columns.ID }, null, null, "created ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            final int titleIndex = cursor.getColumnIndex(DocumentContentProvider.Columns.TITLE);
            final String oldTitle = cursor.getString(titleIndex);
            final int idIndex = cursor.getColumnIndex(DocumentContentProvider.Columns.ID);
            final String documentId = String.valueOf(cursor.getInt(idIndex));
            final Uri documentUri = Uri.withAppendedPath(DocumentContentProvider.CONTENT_URI, documentId);
            askUserForNewTitle(oldTitle, documentUri);
        }
        getSupportLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
    }
}

