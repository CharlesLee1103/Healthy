package com.healthy.ui.base;

import com.healthy.R;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class LoadingView extends LinearLayout {

	// ���ؽ��ȶ���
	private View loadingLayout;// ����������
	private ImageView loadingIV;
	private AnimationDrawable anim;
	private boolean firstStart = true;

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		loadingLayout = LayoutInflater.from(context).inflate(R.layout.progress,
				null);
		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT, Gravity.CENTER);
		loadingLayout.setLayoutParams(param);
		loadingIV = (ImageView) loadingLayout.findViewById(R.id.loading_iv);

		anim = (AnimationDrawable) loadingIV.getBackground();
		addView(loadingLayout);
	}

	public LoadingView(Context context) {
		this(context, null);
	}

	/**
	 * �������ض��� �� ���������ImageView����֮ǰ������������
	 * ��������Activity.onCreate()������Activity�����ĺ����С�
	 * ��Activity.onCreate()ֱ�ӵ������޷����������ģ�ֻ����ʾ�����ĵ�һ��ͼƬ��
	 * */
	private void startAnimationBeforeDraw() {
		loadingIV.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {

					public boolean onPreDraw() {
						// TODO Auto-generated method stub
						if (firstStart) {
							loadingLayout.setVisibility(View.VISIBLE);
							anim.start();
							firstStart = false;
						}
						return true;
					}
				});
	}

	private void stopAnimation() {
		if (anim.isRunning()) {
			anim.stop();
			loadingLayout.setVisibility(View.GONE);
			firstStart=true;
		}
	}

	@Override
	public void setVisibility(int visibility) {
		// TODO Auto-generated method stub
		if (visibility == VISIBLE)
			startAnimationBeforeDraw();
		else
			stopAnimation();
		super.setVisibility(visibility);
	}

	public View getView(){
		return loadingLayout;
	}
}
