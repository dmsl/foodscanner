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

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ds.FoodScanner.All_DB;
import com.ds.FoodScanner.R;
import com.ds.FoodScanner.cropimage.MonitoredActivity;

import java.util.List;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean slideOutLeft = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //initAppIcon(-1);
        findViewById(R.id.show_licences).setOnClickListener(this);
        TextView version = (TextView) findViewById(R.id.version_name);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            version.setText(getString(R.string.app_version,versionName));
        } catch (PackageManager.NameNotFoundException e) {
            version.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.show_licences:
                slideOutLeft = true;
                startActivity(new Intent(this, LicenseActivity.class));
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (slideOutLeft){
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            slideOutLeft = false;
        } else {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void setDialogId(int dialogId) {
//        // ignored
//    }

}
