package com.healthy.ui.base;

import com.healthy.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

public class LineChartView extends View {
	
	/*��������*/
	private String[] mItemTags = { "01��", "02��", "03��", "04��", "05��", "06��",
			"07��", "08��", "09��", "10��", "11��", "12��" };
	private float[] mItemValues = new float[12];

	/* ��ǰview��size��Ϣ */
	private int mWidth = 0;
	private int mHeight = 0;
	private Point mCenter = new Point();// ��������
	private Rect mContentLocation = new Rect();// Ҫ���Ƶ������ڸ���ͼ�е�λ�ã��ų���padding��Ӱ��

	/* ��������ͼ�����ƽ��л�������ر��� */
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private PointF mPrePoint = new PointF();
	private PointF mCurPoint = new PointF();
	private static final int FLING_MIN_VELOCITY = 6000;// ��С���ٻ����ٶ�
	private boolean mAdjusted = false;

	/* ͼ�������ر��� */
	private PathEffect mPathEffect = null;// �������ߵ�ʱ��ʹ��
	private Paint mPaint = new Paint();
	private NinePatch mArrowNinePatch;
	private NinePatch mCurDataNinePath;// ��ǰ��ѡ�������
	private Rect mLocation = new Rect();// ͼ����Ƶ�λ��
	private FontMetrics mFontMetrics = new FontMetrics();
	private Rect mTextBounds = new Rect();
	private int mLength = 0;// ���ڼ�¼���ߵĳ��ȣ�������Ϊ��λ

	/* ������� */
	private String mAvgTip = "";// ����ƽ��ֵ�ı��⣬���硰ƽ��ֵ��
	private float mAvgValue = 0;
	private String mStrAvg;// ƽ��ֵ��С��string��ʾ
	private PointF[] mItemPoses;// ÿ�����ݵ�����ͼ������Ӧ��λ��
	private int mCurSelectedPos = 0;// ��ǰ���е���Ŀ
	private float mMaxTagHeight = 0;// �������ǩ�ĵ����߶�
	private float mMaxTagWidth = 0;// �������ǩ�������
	private final static float TAG_SPACING = 20;// ������֮��ļ��

	public LineChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public LineChartView(Context context) {
		this(context, null, 0);
	}

	public LineChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	private void init() {
		
		mScroller = new Scroller(getContext());
		mPathEffect = new DashPathEffect(new float[] { 8.0f, 8.0f }, 0);// �������ߣ��հ׺�ʵ�ߵĳ��Ⱦ�Ϊ10
		/* �����м�ļ�ͷ��ʾ����ʹ��NinePatch���� */
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg_arrow_tag);
		mArrowNinePatch = new NinePatch(bitmap, bitmap.getNinePatchChunk(),
				null);
		/* ��ǰ��ѡ��ĵ�ı��� */
		bitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.bg_calories_detail);
		mCurDataNinePath = new NinePatch(bitmap, bitmap.getNinePatchChunk(),
				null);
		mAvgTip = getResources().getString(R.string.tip_avg_value);
		
		/**
		 * ע�⣺����������ʹ����Ҫ�������仰ɾ����ת������
		 * initData()��������ʼ������
		 * */
		mAvgValue = getDataAvgValue();
		mStrAvg = new java.text.DecimalFormat("0.00").format(mAvgValue);// ƽ��ֵ���ַ�����ʾ,������λС��
		
	}

	/**
	 * ��ʼ������
	 * 
	 * @param itemTags
	 *            �������ǩ
	 * @param itemValues
	 *            ���������ֵ
	 * */
	public void initData(String[] itemTags, float[] itemValues) {
		if (itemTags == null || itemValues == null)
			return;
		mItemTags = itemTags;
		mItemValues = itemValues;
		mAvgValue = getDataAvgValue();
		mStrAvg = new java.text.DecimalFormat("0.00").format(mAvgValue);// ƽ��ֵ���ַ�����ʾ,������λС��
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		drawCoordinate(canvas);// ��������ϵ��Ϣ
		drawDataPoint(canvas);// �������ݵ㼰��֮�������
		if (!mAdjusted && mScroller.isFinished()) {
			setPointerHoming();
			mAdjusted = true;
		}
	}

	/** ��������ϵ */
	private void drawCoordinate(Canvas canvas) {
		int sc = canvas.save();
		/* �������ƶ���getScrollX()�ٻ�������ϵ,�Ա����û�����ʱ������ϵ�Ա��ֲ��� */
		canvas.translate(getScrollX(), 0);
		/*
		 * �����м�ĺ������ߣ������߱�ʾ���ݵ�ƽ��ֵ������λ����ͼ�����м䣬���Դ����yֵΪ0 ����yֵ�������½���
		 */
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(2.5f);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(getResources().getColor(R.color.light_gray));
		mPaint.setPathEffect(mPathEffect);
		canvas.drawLine(mContentLocation.left, mCenter.y,
				mContentLocation.right, mCenter.y, mPaint);
		/* ����λ���м������ʵ�� */
		mPaint.setPathEffect(null);
		canvas.drawLine(mCenter.x, mContentLocation.top, mCenter.x,
				mContentLocation.bottom, mPaint);
		/* �����м�ļ�ͷ��ʾ���� */
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setColor(0xffffffff);// �趨��ɫΪ��ɫ
		mPaint.setTextSize(19.0f);
		mPaint.setTextAlign(Align.CENTER);
		/* ��ȡ���ִ�С��Ϣ */
		mPaint.getFontMetrics(mFontMetrics);
		int fontHeight = (int) (mFontMetrics.bottom - mFontMetrics.top);// ��ȡ���ָ߶�
		/* �������ִ�Сȷ����ʾ��Ϣ�ľ����С��λ�� */
		mLocation.left = mContentLocation.left + 10;
		mLocation.top = mCenter.y - fontHeight;
		mLocation.bottom = mCenter.y + fontHeight;
		mLocation.right = mLocation.left
				+ 15
				+ (int) Math.max(mPaint.measureText(mAvgTip),
						mPaint.measureText(mStrAvg)) + 25;
		mArrowNinePatch.draw(canvas, mLocation);// ������ʾ��Ϣ����
		/* ������ʾ��Ϣ���� */
		canvas.drawText(mAvgTip, mLocation.left
				+ (mLocation.right - mLocation.left - 15) / 2, mCenter.y
				- mFontMetrics.bottom, mPaint);
		canvas.drawText(mStrAvg, mLocation.left
				+ (mLocation.right - mLocation.left - 15) / 2, mCenter.y
				- mFontMetrics.top, mPaint);
		canvas.restoreToCount(sc);
	}

	/** ����������Ϣ */
	private void drawDataPoint(Canvas canvas) {

		mPaint.reset();
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setTextSize(25.0f);
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(5.0f);
		mPaint.setColor(getResources().getColor(R.color.red));
		mPaint.setStyle(Style.STROKE);

		mPaint.getFontMetrics(mFontMetrics);// ��ȡ����Ĵ�С
		/* ����·�� */
		for (int i = 1; i < mItemPoses.length; i++) {
			canvas.drawLine(mItemPoses[i - 1].x, mItemPoses[i - 1].y,
					mItemPoses[i].x, mItemPoses[i].y, mPaint);
		}

		/* �������ݵ�������ǩ */
		for (int i = 0; i < mItemValues.length; i++) {
			if (i == mCurSelectedPos) {// ��ǰ��Ϊ��ѡ��ĵ� �������ݵ�
				mPaint.setStrokeWidth(6.0f);
				mPaint.setColor(getResources().getColor(android.R.color.white));
				mPaint.setStyle(Style.FILL);
				canvas.drawCircle(mItemPoses[i].x, mItemPoses[i].y, 10.0f,
						mPaint);
				mPaint.setColor(getResources().getColor(R.color.red));
				mPaint.setStyle(Style.STROKE);
				canvas.drawCircle(mItemPoses[i].x, mItemPoses[i].y, 10.0f,
						mPaint);
				/* ���ƽ�����ο� */
				mPaint.setStyle(Style.FILL);
				canvas.drawRect(mItemPoses[i].x - mMaxTagWidth / 2 - 5,
						mContentLocation.bottom - mFontMetrics.bottom
								+ mFontMetrics.top - 20, mItemPoses[i].x
								+ mMaxTagWidth / 2 + 5,
						mContentLocation.bottom - 20, mPaint); /* ���ƺ������ǩ */
				mPaint.setColor(getResources().getColor(android.R.color.white));
				canvas.drawText(mItemTags[i], mItemPoses[i].x,
						mContentLocation.bottom - mFontMetrics.bottom - 20,
						mPaint);
				/* ���Ƶ�ǰѡ������ݵ�ĵ����� */
				mPaint.setColor(getResources().getColor(R.color.red));
				float hwidth = mPaint.measureText(mItemValues[i] + "") / 2;
				float fontheight = mFontMetrics.bottom - mFontMetrics.top;
				mLocation.set((int) (mItemPoses[i].x - hwidth - 10),
						(int) (mItemPoses[i].y - 20 - fontheight - 15),
						(int) (mItemPoses[i].x + hwidth + 10),
						(int) (mItemPoses[i].y - 20));
				mCurDataNinePath.draw(canvas, mLocation);
				canvas.drawText(mItemValues[i] + "",
						(mLocation.left + mLocation.right) / 2,
						mLocation.bottom - mFontMetrics.bottom - 15, mPaint);
			} else {
				/* �������ݵ� */
				mPaint.setColor(getResources().getColor(R.color.red));
				mPaint.setStyle(Style.FILL);
				canvas.drawCircle(mItemPoses[i].x, mItemPoses[i].y, 8.0f,
						mPaint);
				/* ���ƺ������ǩ */
				mPaint.setColor(getResources().getColor(android.R.color.black));
				canvas.drawText(mItemTags[i], mItemPoses[i].x,
						mContentLocation.bottom - mFontMetrics.bottom - 20,
						mPaint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		obtainVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mCurPoint.set(event.getX(), event.getY());
			return true;
		case MotionEvent.ACTION_MOVE:
			mPrePoint.set(mCurPoint);
			mCurPoint.set(event.getX(), event.getY());
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			float xVelocity = mVelocityTracker.getXVelocity();
			if (Math.abs(xVelocity) > FLING_MIN_VELOCITY
					&& mScroller.isFinished()) {
				int distance = 0;
				if (xVelocity < 0) {// �������,�����������Ϊ�˱�֤����������������
					distance = (int) Math.min(getScrollX() - xVelocity / 15,
							100 + mLength) - getScrollX();
				} else {// ���ҹ���
					distance = (int) Math.max(getScrollX() - xVelocity / 15,
							-100) - getScrollX();
				}
				mScroller.startScroll(getScrollX(), 0, distance, 0, 1000);
			} else {
				int distance = (int) ((mPrePoint.x - mCurPoint.x) / 2);
				if (xVelocity < 0 && getScrollX() + distance < 100 + mLength// �������,����û�г�������������
						|| xVelocity > 0 && getScrollX() + distance > -100) {// ���ҹ���������û�г�������������
					scrollBy(distance, 0);
				}
			}

			break;
		case MotionEvent.ACTION_UP:
			mAdjusted = false;
			postInvalidate();
			releaseVelocityTracker();
			break;
		}
		return super.onTouchEvent(event);
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

	/**
	 * �趨�������ʾ��Ϣ
	 * */
	public void setArrowTip(String tip) {
		mAvgTip = tip;
	}

	/** ��ȡ���ݵ�ƽ��ֵ */
	public float getDataAvgValue() {
		if (mItemValues == null)
			return 0;
		float sum = 0;
		for (int i = 0; i < mItemValues.length; i++) {
			sum += mItemValues[i];
		}
		return sum /= mItemValues.length;
	}

	/**
	 * ������ֵͶ�䵽��ͼ�ϵ�ĳ��λ��
	 * 
	 * @param textPaint
	 *            ���ƺ������ǩ��ʹ�õĻ���
	 */
	private void value2Pos(Paint textPaint) {

		if (mItemValues == null)
			return;
		mItemPoses = new PointF[mItemValues.length];
		float min = 0, max = 0;

		/* ���������е����ֵ����Сֵ */
		for (int i = 0; i < mItemValues.length; i++) {
			if (min > mItemValues[i])
				min = mItemValues[i];
			else if (max < mItemValues[i])
				max = mItemValues[i];
		}

		/* ����ÿһ�����ݵ�����ͼ�������� */
		for (int i = 0; i < mItemValues.length; i++) {
			mItemPoses[i] = new PointF();
			if(max!=min){
				mItemPoses[i].y = (1 - (mItemValues[i] - min) / (max - min))
						* (mContentLocation.bottom - mContentLocation.top - 220)
						+ 120;
			}else{
				mItemPoses[i].y = mContentLocation.bottom - mContentLocation.top - 220 + 120;
			}
		}

		/* ��ȡ����ַ�����Ⱥ͸߶� */
		mMaxTagHeight = 0;
		mMaxTagWidth = 0;
		float height = 0;
		float width = 0;
		for (int i = 0; i < mItemTags.length; i++) {
			textPaint.getTextBounds(mItemTags[i], 0, mItemTags[i].length(),
					mTextBounds);
			height = mTextBounds.bottom - mTextBounds.top;
			width = mTextBounds.right - mTextBounds.left;
			if (mMaxTagHeight < height)
				mMaxTagHeight = height;
			if (mMaxTagWidth < width)
				mMaxTagWidth = width;
		}
		/* ����ÿһ�����ݵ�ĺ������λ�� */
		for (int i = 0; i < mItemTags.length; i++) {
			mItemPoses[i].x = mCenter.x + (mMaxTagWidth + TAG_SPACING) * i;
		}
		mLength = (int) (mItemPoses[mItemPoses.length - 1].x - mItemPoses[0].x);// ���������Ҳ�İ�Բ�뾶û�м�������
	}

	/** ��ȡָ�뵱ǰӦ��ָ��ĵ���Ŀ */
	public int getPointerPos() {

		int x = mWidth / 2 + getScrollX();
		if (x <= mItemPoses[0].x)// �����������
			return 0;
		if (x >= mItemPoses[mItemPoses.length - 1].x)// ���������Ҳ�
			return mItemPoses.length - 1;
		int i = mItemPoses.length - 1;
		for (; i >= 0; i--) {
			if (x >= mItemPoses[i].x)
				break;
		}
		if (x - mItemPoses[i].x < (mItemPoses[i + 1].x - mItemPoses[i].x) / 2)
			return i;
		else
			return i + 1;

	}

	/**
	 * ʹָ��ָ��ǰ������Ŀ
	 * 
	 * */
	private void setPointerHoming() {
		mCurSelectedPos = getPointerPos();
		int distance = (int) ((mItemPoses[mCurSelectedPos].x - mWidth / 2) - getScrollX());
		if (mScroller.isFinished()) {
			// Scroller has nothing to do with the UI - it's just a helper class
			// that helps to compute position based on initial position and
			// initial velocity
			mScroller.startScroll(getScrollX(), 0, distance, 0, 500);
			invalidate();
		}

	}

	@Override
	public void computeScroll() {
		// TODO Auto-generated method stub
		super.computeScroll();
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		mWidth = getWidth();
		mHeight = getHeight();
		mCenter.set(mWidth / 2, mHeight / 2);

		mContentLocation.set(getPaddingLeft(), getPaddingTop(), mWidth
				- getPaddingRight(), mHeight - getPaddingBottom());
		
		/* Ͷ��ÿ����������ͼ�е�λ�� */
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(25.0f);
		mPaint.setTextAlign(Align.CENTER);
		value2Pos(mPaint);
	
	}
	
	public void setItemValues(float[] itemValues){
		mItemValues = itemValues;
		mAvgValue = getDataAvgValue();
		mStrAvg = new java.text.DecimalFormat("0.00").format(mAvgValue);// ƽ��ֵ���ַ�����ʾ,������λС��
		invalidate();
	}

}
