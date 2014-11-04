package com.ken.promotion;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class PromotionDetail extends LinearLayout {

	public PromotionDetail(Context context) {
		this(context, null);
	}

	public PromotionDetail(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PromotionDetail(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setUpView(context);
	}

	protected void setUpView(Context context) {
	}

	@Override
	protected void onFinishInflate() {
	}
}
