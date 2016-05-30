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
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class All_DB {

    boolean flag = true;
    TestAdapter mDbHelper;

    public List<String> test(Context urContext, String sql_query){
        List<String> dbList = new ArrayList<String>();
        if(flag) {
            mDbHelper = new TestAdapter(urContext);

            mDbHelper.createDatabase();
            mDbHelper.open();
        }
        // looping through all rows and adding to list
        Cursor testdata = mDbHelper.getTestData(sql_query);

        int min_string_length = 1000;
        String[] data = new String[testdata.getColumnCount()];

        if(testdata != null){
            int num_columns = testdata.getColumnCount();

            while (testdata.moveToNext()) {
                String s = testdata.getString(num_columns-1);
                if(s == null){
                    dbList.add("N/A");
                }else{
                    if(s.length() < min_string_length){
                        min_string_length = s.length();
                        for (int j = 0; j < num_columns; j++){
                            if(testdata.getString(j) == null){
                                data[j] = "null";
                            }else{
                                data[j] = testdata.getString(j);
                            }
                        }
                    }
                }
            }
            for (int j = 0; j < num_columns; j++){
                if(data[j] != null){
                    Log.e("     Col:",data[j]);
                    dbList.add(data[j]);
                }else{
                    Log.e("     Col:", "Col->null");
                }
            }
        }

        flag = false;
        return dbList;
    }

    public void closeDB(){
        mDbHelper.close();
    }

}
