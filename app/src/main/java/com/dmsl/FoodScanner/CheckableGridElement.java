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

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class CheckableGridElement extends RelativeLayout implements Checkable {

	private boolean mIsChecked = false;
	private OnCheckedChangeListener mListener;
	private ImageView mThumbnailImageView;
	private final Transformation mSelectionTransformation;
	private final Transformation mAlphaTransformation;
	private AnimatorSet mAnimatorSet;

	private float mTargetAlpha = 1;
	private float mCurrentAlpha = 1;

	private float mTargetScale = 1;
	private float mCurrentScale = 1;

	private static final float SELECTED_ALPHA = 1.0f;
	private static final float NOT_SELECTED_ALPHA = .35f;

	private static final float SELECTED_SCALE = 1.0f;
	private static final float NOT_SELECTED_SCALE = 0.95f;

	private static int ANIMATION_DURATION = 200;
	private final long TAP_ANIMATINO_DURATION;
	@SuppressWarnings("unused")
	private final String LOG_TAG = CheckableGridElement.class.getSimpleName();

	public interface OnCheckedChangeListener {
		void onCheckedChanged(View documentView, boolean isChecked);
	}

	public CheckableGridElement(Context context, AttributeSet attrs) {
		super(context, attrs);
		TAP_ANIMATINO_DURATION = ViewConfiguration.getLongPressTimeout();
		this.setWillNotDraw(false);
		if (!isInEditMode()) {
			setStaticTransformationsEnabled(true);
			mSelectionTransformation = new Transformation();
			mAlphaTransformation = new Transformation();
			mSelectionTransformation.setTransformationType(Transformation.TYPE_MATRIX);
			mAlphaTransformation.setTransformationType(Transformation.TYPE_ALPHA);
		} else {
			mAlphaTransformation = null;
			mSelectionTransformation = null;
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mThumbnailImageView = (ImageView) findViewById(R.id.thumb);
		this.setFocusable(false);
	}

	public void setImage(Drawable d) {
		mThumbnailImageView.setImageDrawable(d);
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mListener = listener;
	}

	/**
	 * sets checked state without starting an animation
	 * 
	 * @param checked
	 */
	public void setCheckedNoAnimate(final boolean checked) {
		mIsChecked = checked;
		// stop any running animation
		if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
			mAnimatorSet.removeAllListeners();
			mAnimatorSet.cancel();
		}
		setCurrentAlpha(getDesiredAlpha());
		setCurrentScale(getDesiredScale());
		if (mListener != null) {
			mListener.onCheckedChanged(this, mIsChecked);
		}

	}

	/**
	 * sets checked state and starts animation if necessary
	 */
	@Override
	public void setChecked(boolean checked) {
		mIsChecked = checked;

		initAnimationProperties(AnimationType.CHECK);

		if (mListener != null) {
			mListener.onCheckedChanged(this, mIsChecked);
		}
	}

	@Override
	public boolean isChecked() {
		return mIsChecked;
	}

	private float getDesiredScale() {
		if (mIsChecked && MainActivity.isInSelectionMode()) {
			return SELECTED_SCALE;
		} else if (!mIsChecked && MainActivity.isInSelectionMode()) {
			return NOT_SELECTED_SCALE;
		} else {
			return SELECTED_SCALE;
		}
	}

	private float getDesiredAlpha() {
		if (mIsChecked && MainActivity.isInSelectionMode()) {
			return SELECTED_ALPHA;
		} else if (!mIsChecked && MainActivity.isInSelectionMode()) {
			return NOT_SELECTED_ALPHA;
		} else {
			return SELECTED_ALPHA;
		}
	}

	private enum AnimationType {
		TAP_UP, TAP_DOWN, CHECK;
	}

	private void initAnimationProperties(final AnimationType type) {
		final long duration;
		switch (type) {

		case CHECK: {
			duration = ANIMATION_DURATION;
			mTargetAlpha = getDesiredAlpha();
			//mTargetScale = getDesiredScale();
			break;
		}
		case TAP_DOWN: {
			duration = TAP_ANIMATINO_DURATION;
			mTargetScale = NOT_SELECTED_SCALE;
			break;
		}
		case TAP_UP: {
			duration = ANIMATION_DURATION;
			mTargetScale = SELECTED_SCALE;
			break;
		}
		default:
			duration = ANIMATION_DURATION;
		}

		startAnimation(duration);
	}

	private void setCurrentScale(final float value) {
		mCurrentScale = value;
		getChildAt(0).invalidate();
	}

	private void setCurrentAlpha(final float value) {
		mCurrentAlpha = value;
	}

	private void startAnimation(final long anmationDuration) {
		final long duration = (long) (anmationDuration * (Math.abs(mCurrentScale - mTargetScale) / (SELECTED_SCALE - NOT_SELECTED_SCALE)));
		// Log.i(LOG_TAG, "scaling from "+mCurrentScale + " to " + mTargetScale
		// + " in " + duration);

		ObjectAnimator scale = ObjectAnimator.ofFloat(this, "currentScale", mCurrentScale, mTargetScale);
		ObjectAnimator alpha = ObjectAnimator.ofFloat(this, "currentAlpha", mCurrentAlpha, mTargetAlpha);
		mAnimatorSet = new AnimatorSet();
		mAnimatorSet.setDuration(duration);
		mAnimatorSet.setInterpolator(new DecelerateInterpolator(1.2f));
		mAnimatorSet.playTogether(scale, alpha);
		mAnimatorSet.start();
	}

	@Override
	public void toggle() {
		mIsChecked = !mIsChecked;
		initAnimationProperties(AnimationType.CHECK);
		if (mListener != null) {
			mListener.onCheckedChanged(this, mIsChecked);
		}
	}

	public void startTouchDownAnimation() {
		if (!MainActivity.isInSelectionMode()) {
			initAnimationProperties(AnimationType.TAP_DOWN);
		}
	}

	public void startTouchUpAnimation() {
		if (!MainActivity.isInSelectionMode()) {
			initAnimationProperties(AnimationType.TAP_UP);
		}
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		boolean hasChanged = false;
		if (mTargetAlpha != mCurrentAlpha || mTargetAlpha != SELECTED_ALPHA) {
			mAlphaTransformation.setAlpha(mCurrentAlpha);
			t.compose(mAlphaTransformation);
			hasChanged = true;
		}
		if (mTargetScale != mCurrentScale || mTargetScale != SELECTED_SCALE) {
			mSelectionTransformation.getMatrix().reset();
			final float px = child.getLeft() + (child.getWidth()) / 2;
			final float py = child.getTop() + (child.getHeight()) / 2;
			mSelectionTransformation.getMatrix().postScale(mCurrentScale, mCurrentScale, px, py);
			t.compose(mSelectionTransformation);
			hasChanged = true;
		}
		if (hasChanged) {
			child.invalidate();
			this.invalidate();
		}
		return hasChanged;
	}

}
