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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.GridView;

public class FancyGridView extends GridView {

	public FancyGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private CheckableGridElement mLastTouchedChild;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
//		final int x = (int) ev.getX();
//		final int y = (int) ev.getY();
//		final int motionPosition = pointToPosition(x, y);
//		CheckableGridElement touchedChild = null;
//		if (motionPosition >= 0) {
//			touchedChild = (CheckableGridElement) getChildAt(motionPosition - getFirstVisiblePosition());
//		}
//
//		switch (action & MotionEvent.ACTION_MASK) {
//			case MotionEvent.ACTION_DOWN: {
//				if (touchedChild != null) {
//					touchedChild.startTouchDownAnimation();
//					mLastTouchedChild = touchedChild;
//				} else {
//					mLastTouchedChild = null;
//				}
//				break;
//			}
//			case MotionEvent.ACTION_UP: {
//				if (mLastTouchedChild != null) {
//					mLastTouchedChild.startTouchUpAnimation();
//					mLastTouchedChild = null;
//				}
//				break;
//			}
//			case MotionEvent.ACTION_MOVE: {
//
//				if (mLastTouchedChild != null && !mLastTouchedChild.equals(touchedChild)) {
//					mLastTouchedChild.startTouchUpAnimation();
//
//				}
//
//				mLastTouchedChild = touchedChild;
//				break;
//			}
//		}

		return super.onTouchEvent(ev);
	}

}
