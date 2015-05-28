package com.healthy.ui.base;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

public class FlipperLayout extends ViewGroup {

	private final static int SCROLL_TIME = 400;// 250
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mWidth;

	/* �˵�����״̬ */
	public static final int MENU_STATE_CLOSE = 0;// �˵��ر�״̬
	public static final int MENU_STATE_OPEN = 1;// �˵���״̬
	private int mMenuState = 0;// ��¼��ǰ�˵��Ĵ�״̬

	/* ��ǰ����״̬ */
	public static final int TOUCH_STATE_RESTART = 0;// ��ʼ����
	public static final int TOUCH_STATE_SCROLLING = 1;// ��ǰ���ڽ��й���
	private int mTouchState = 0;

	/* �Ƿ�������� */
	public static final int SCROLL_STATE_NO_ALLOW = 0;
	public static final int SCROLL_STATE_ALLOW = 1;
	private int mScrollState = 0;

	private int mVelocityValue = 0;

	/*
	 * ���˵����ڴ�״̬ʱ���û�����ֱ�ӵ��������Ļ�ұ�mWidth���ڵ����� ����Զ����󻬶��ر����˵����ñ���Ϊ�û��Ƿ�ɹ�����ı�־λ
	 */
	private boolean mOnClick = false;

	public FlipperLayout(Context context) {
		super(context);
		mScroller = new Scroller(context);

		// �趨mWidth��ֵΪ54dip���ÿ��Ϊ�򿪲˵�ʱ�������滹������ʾ�Ŀ��
		// 54dipͬ�����û����Խ�����ק�ľ��������Ļ�߽���С���
		mWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				54, getResources().getDisplayMetrics());

	}

	public FlipperLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FlipperLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		// ����������ͼ���ǽ��л���
		// �ڸ�Ӧ���У�ֻ����������ͼ���ֱ������˵�����ҳ��
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			int height = child.getMeasuredHeight();
			int width = child.getMeasuredWidth();
			child.layout(0, 0, width, height);
		}
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		obtainVelocityTracker(event);// ���øú����Լ�¼�����ٶ�
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:// ��case����
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESTART
					: TOUCH_STATE_SCROLLING;
			if (mTouchState == TOUCH_STATE_RESTART) {
				int x = (int) event.getX();
				int screenWidth = getWidth();
				if (x <= mWidth
						&& mMenuState == MENU_STATE_CLOSE
						&& mTouchState == TOUCH_STATE_RESTART// ��ǰ�û����ڴ򿪲˵�
						|| x >= screenWidth - mWidth
						&& mMenuState == MENU_STATE_OPEN
						&& mTouchState == TOUCH_STATE_RESTART) {// ��ǰ�û����ڹرղ˵�
					// ���˵�����ʱ�򣬱�ʾ�û�����˿�����Ļ�ұ߽�mWidth���ڵ��������û������Ų�����ACTION_MOVE�Ĳ������������ACTION_UP�¼����Զ������رղ˵�
					if (mMenuState == MENU_STATE_OPEN) {
						mOnClick = true;
					}
					mScrollState = SCROLL_STATE_ALLOW;// ���������Ļ
				} else {
					mOnClick = false;
					mScrollState = SCROLL_STATE_NO_ALLOW;// ��ֹ������Ļ
				}
			} else {// �˵������ڹ���״̬���������κβ���
				return false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			if (mScrollState == SCROLL_STATE_ALLOW
					&& getWidth() - (int) event.getX() < mWidth) {
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			releaseVelocityTracker();
			if (mOnClick) {// ��ǰ�˵��Ѿ��򿪣���Ҫ�ر�
				mOnClick = false;
				mMenuState = MENU_STATE_CLOSE;
				mScroller.startScroll(getChildAt(1).getScrollX(), 0,
						-getChildAt(1).getScrollX(), 0, SCROLL_TIME);// 800
				invalidate();
				return true;
			}
			break;
		}
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		obtainVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESTART
					: TOUCH_STATE_SCROLLING;
			if (mTouchState == TOUCH_STATE_SCROLLING) {// ����û�н���
				return false;
			}
			break;

		case MotionEvent.ACTION_MOVE:
			mOnClick = false;
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			if (mScrollState == SCROLL_STATE_ALLOW
					&& Math.abs(mVelocityTracker.getXVelocity()) > 200) {
				return true;
			}
			break;

		case MotionEvent.ACTION_UP:
			releaseVelocityTracker();
			if (mScrollState == SCROLL_STATE_ALLOW
					&& mMenuState == MENU_STATE_OPEN) {
				return true;
			}
			break;
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		obtainVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESTART
					: TOUCH_STATE_SCROLLING;
			if (mTouchState == TOUCH_STATE_SCROLLING) {// ����û�н���
				return false;
			}
			break;

		case MotionEvent.ACTION_MOVE:// ��ʼ������Ļ
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			mVelocityValue = (int) mVelocityTracker.getXVelocity();

			/*
			 * Ϊʲô��-(int) event.getX(),������Ҫ����һ�º���scrollTo(int x, int y)�������ǽ���ǰ��ͼ
			 * ���ݵ����Ͻ�����ƫ����(x,y)������������������Ͻ�λ��(x , y)���괦�����������һ�����ʱ��������Ҫ������ͼ�Ŀ�
			 * ��ʾ�������������չ�������ڸ���ͼ�в�����ʾ�����һ��������¶���Ĳ��֣����û�ж�������͸���ģ�
			 * �����һ�������ζ�Ž�������������໬��
			 */
			getChildAt(1).scrollTo(-(int) event.getX(), 0);
			break;

		// ��ACTION_UP�¼��У����ж��Ƿ�򿪻��߹رղ˵�
		case MotionEvent.ACTION_UP:
			if (mScrollState == SCROLL_STATE_ALLOW) {
				if (mVelocityValue > 2000) {// ���ҿ��ٻ������򿪲˵�
					mMenuState = MENU_STATE_OPEN;
					mScroller
							.startScroll(
									getChildAt(1).getScrollX(),
									0,
									-(getWidth()
											- Math.abs(getChildAt(1)
													.getScrollX()) - mWidth),
									0, SCROLL_TIME);// 250
					invalidate();
				} else if (mVelocityValue < -2000) {// ������ٻ����رղ˵�
					mMenuState = MENU_STATE_CLOSE;
					mScroller.startScroll(getChildAt(1).getScrollX(), 0,
							-getChildAt(1).getScrollX(), 0, SCROLL_TIME);// 250
					invalidate();
				} else if (event.getX() < getWidth() / 2) {
					mMenuState = MENU_STATE_CLOSE;
					mScroller.startScroll(getChildAt(1).getScrollX(), 0,
							-getChildAt(1).getScrollX(), 0, SCROLL_TIME);// 800
					invalidate();
				} else {
					mMenuState = MENU_STATE_OPEN;
					mScroller
							.startScroll(
									getChildAt(1).getScrollX(),
									0,
									-(getWidth()
											- Math.abs(getChildAt(1)
													.getScrollX()) - mWidth),
									0, SCROLL_TIME);// 800
					invalidate();
				}
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	// ���ҹ����򿪲˵�
	public void open() {
		mTouchState = mScroller.isFinished() ? TOUCH_STATE_RESTART
				: TOUCH_STATE_SCROLLING;
		if (mTouchState == TOUCH_STATE_RESTART) {
			mMenuState = MENU_STATE_OPEN;
			mScroller.startScroll(getChildAt(1).getScrollX(), 0, -(getWidth()
					- Math.abs(getChildAt(1).getScrollX()) - mWidth), 0,
					SCROLL_TIME);// 800
			invalidate();
		}
	}

	// ��������رղ˵�
	public void changeContentView(View view) {
		if (mMenuState == MENU_STATE_OPEN) {
			mMenuState = MENU_STATE_CLOSE;
			mScroller.startScroll(getChildAt(1).getScrollX(), 0, -getChildAt(1)
					.getScrollX(), 0, SCROLL_TIME);// 800
			invalidate();
		}
		setContentView(view);
	}

	public void setContentView(View view) {
		removeViewAt(1);
		addView(view, 1, getLayoutParams());
	}

	private void obtainVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	public interface OnOpenListener {
		public abstract void open();
	}

	public interface OnCloseListener {
		public abstract void close();
	}

	/**
	 * ��ȡ��ǰ�Ĳ˵�״̬�������˵����߹رղ˵�
	 * 
	 * @return MENU_STATE_OPEN ���� MENU_STATE_CLOSE
	 * */
	public int getScreenState() {
		return mMenuState;
	}

	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			getChildAt(1).scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

}
