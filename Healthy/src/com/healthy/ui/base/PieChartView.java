package com.healthy.ui.base;

import com.healthy.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.ImageView;

@SuppressLint("FloatMath")
public class PieChartView extends ImageView implements Runnable {

	private static final int FLING_MIN_VELOCITY = 3500;// ��С���ٻ����ٶ�

	private int[] mItemColors;// ��Ŀ��ɫ
	private float[] mItemRatios;// ��Ŀ��ռ�ı���
	private String[] mItemNames;// ��Ŀ����
	private float[] mItemPos;// ÿ����Ŀ����ʼλ��

	/* ���Ʊ�ͼ����ľ������� */
	private RectF mPieChartArea = new RectF();

	// Բ������
	private PointF mCenter = new PointF();

	private float mStartAngle = 0;// ���Ʊ�ͼ�ĳ�ʼ�Ƕ�
	private float mDeltaAngle = 0;// ��ͼ��ת�ĽǶ�

	private Paint mPaint;
	private Paint mTextPaint;// �����ַ����õ�paint.

	/* �����ͼ�����ƹ�������ر��� */
	private VelocityTracker mVelocityTracker;// ���ڼ�¼�û��Ļ����ٶ�
	private PointF mPrePoint = new PointF();// ����������
	private PointF mCurPoint = new PointF();
	private PointF mOriginalPoint = new PointF();// ��ָ���µĵ�����
	private float mAcceleration = 0;// ����ʱ��ļ��ٶ�
	private float mSpeed = 0;// ���ٹ���ʱ���˲ʱ�ٶ�
	private float mPreSpeed = 0;// ��һʱ�̵�˲ʱ�ٶȣ�����������Ϊ��������ֹͣת��ת�̡�
	private boolean mFastRotating = false;// �жϵ�ǰ��ͼ�Ƿ������ڿ�����ת״̬
	private int mClockWise = 0;// ˳ʱ�뷽���־λ,Ϊ�˷�ֹ��Ϊ�û������������жϴ������ﲢû��ʹ��boolean���͡�

	private OnCompleteRotating mListener;

	public PieChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init() {
		mItemColors = getResources().getIntArray(R.array.pie_chart_colors);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mTextPaint = new Paint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setFakeBoldText(true);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(23.0f);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		/* ��û��Ʊ�ͼ����Ҫ�ľ������� */
		mPieChartArea.left = 0;
		mPieChartArea.top = 0;
		mPieChartArea.right = getWidth();
		mPieChartArea.bottom = getHeight();
		mCenter.set(getWidth() / 2, getHeight() / 2);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		/*
		 * Ϊ�˽�ʡ��Դ�������ƶ������ķ�ʽ����תͼ�� ������ù̶���������תͼ��ķ�����������ֿ��ٲ��ҵ�֡�����
		 */
		canvas.save();
		// ���ڻ������ԣ���ת�Ƕ�Ϊ��ʱ�򻭲�������ʱ�뷽��ת����
		canvas.rotate(mDeltaAngle, mCenter.x, mCenter.y);
		/* ����ÿ����Ŀ�ڱ�ͼ������Ӧ������ */
		for (int i = 0; i < mItemNames.length; i++) {
			mPaint.setColor(mItemColors[i]);
			float sweepAngle = (float) (360 * mItemRatios[i]);
			canvas.drawArc(mPieChartArea, mStartAngle, sweepAngle, true, mPaint);
			mStartAngle += sweepAngle;
		}
		mStartAngle = 0;
		canvas.restore();
		super.onDraw(canvas);
		/* �������֣�����Ҫˮƽ��ֱ���� */
		FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		String name = mItemNames[getPointerPos()];
		String ratio = String
				.format("%.2f", mItemRatios[getPointerPos()] * 100) + "%";
		/* ������� */
		canvas.drawText(name, mCenter.x, mCenter.y - fontMetrics.bottom,
				mTextPaint);
		/* ������ռ�ٷֱ� */
		canvas.drawText(ratio, mCenter.x, mCenter.y - fontMetrics.top,
				mTextPaint);
	}

	@Override
	public void run() {// ���ٹ����߳�
		// TODO Auto-generated method stub
		while (mFastRotating) {// �ٶ�û�м��ٵ�0
			if (mPreSpeed * mSpeed > 0) {
				rotatePie(mSpeed);
				mPreSpeed = mSpeed;
				mSpeed += mAcceleration;
			} else {// �ٶ��Ѿ����ٵ�0��ֹͣת��
				mFastRotating = false;
				setPointerHoming();
			}
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * ʹָ��ָ��ǰ������Ŀ���м�λ��
	 * 
	 * */
	private void setPointerHoming() {

		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				int pos = getPointerPos();
				float startAngle = (90 - mDeltaAngle + 360) % 360;
				float endAngle = (mItemPos[pos] + (mItemPos[pos + 1] - mItemPos[pos]) / 2) * 360;
				float deltaAngle = endAngle - startAngle;
				float preDeltaAngle = deltaAngle;
				while (preDeltaAngle * deltaAngle > 0) {// ���ٻ�����ָ��λ��
					float speed = deltaAngle > 0 ? 3 : -3;
					rotatePie(-speed);
					preDeltaAngle = deltaAngle;
					deltaAngle -= speed;
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (mListener != null) {
					mListener.onCompleteRotating(pos);
				}
			}

		}.start();

	}

	/**
	 * ��ȡ��ǰָ���λ��
	 * 
	 * @return ��ǰָ����ָ�����Ŀ����
	 * */
	public int getPointerPos() {
		/* ����ָ���ָ��ʹ��ָ��ָ��ĳ��Ԫ�ص����м� */
		float pointerPos = (90 - mDeltaAngle + 360) % 360;// ��ԭ��ͼ��0�ȳ�ʼλ��Ϊ��㣬��ǰָ��ĽǶ�
		int pos = 0;
		for (; pos < mItemNames.length; pos++) {
			if (pointerPos < mItemPos[pos] * 360)
				break;
		}
		return --pos;
	}

	/**
	 * ������
	 * 
	 * @param itemName
	 *            ��Ŀ����
	 * @param itemRatio
	 *            ��Ŀ��ռ����
	 * */
	public void initData(String[] itemNames, float[] itemRatios) {
		mItemNames = itemNames;
		mItemRatios = itemRatios;
		/* ����ÿ����Ŀ����ʼλ�� */
		mItemPos = new float[itemNames.length + 1];
		mItemPos[0] = 0;
		mItemPos[itemNames.length] = 1;
		for (int i = 1; i < mItemNames.length; i++) {
			mItemPos[i] = mItemPos[i - 1] + mItemRatios[i - 1];
		}
		setPointerHoming();// ��ָ��ָ��ĳһ����Ŀ�����м�
	}

	/**
	 * ���ո����ĽǶ���ת��ͼ
	 * 
	 * @param deltaAngle
	 *            ��ת�ĽǶȣ�����ʾ˳ʱ����ת������ʾ��ʱ����ת
	 * */
	public void rotatePie(double deltaAngle) {
		if (deltaAngle == 0)
			return;
		mDeltaAngle += deltaAngle;
		mDeltaAngle %= 360;
		this.postInvalidate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		obtainVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mOriginalPoint.set(event.getX(), event.getY());
			mCurPoint.set(mOriginalPoint);
			mPrePoint.set(mOriginalPoint);
			mFastRotating = false;// �����ǰ��ͼ��������ת״̬��Ӧֹͣת��
			mClockWise = 0;
			return true;
		case MotionEvent.ACTION_MOVE:
			mVelocityTracker.computeCurrentVelocity(1000,
					ViewConfiguration.getMaximumFlingVelocity());
			float xVelocity = mVelocityTracker.getXVelocity();
			float yVelocity = mVelocityTracker.getYVelocity();
			mPrePoint.set(mCurPoint);
			mCurPoint.set(event.getX(), event.getY());
			boolean clockWise = currentDirection(mPrePoint, mCurPoint);// �ж���˳ʱ�뷽������ʱ�뷽��
			mClockWise = clockWise ? mClockWise + 1 : mClockWise - 1;
			if (Math.abs(xVelocity) + Math.abs(yVelocity) > FLING_MIN_VELOCITY) {// �û������˿��ٻ����Ĳ���
				mFastRotating = true;
			}
			rotatePie(getDeltaAngle(mPrePoint, mCurPoint));
			break;
		case MotionEvent.ACTION_UP:
			if (mFastRotating) {// �ȼ��ٹ���
				/* ��û���˲ʱ�����ٶ� */
				float velocity = (float) Math.sqrt(Math.pow(
						mVelocityTracker.getXVelocity(), 2)
						+ Math.pow(mVelocityTracker.getYVelocity(), 2));
				mSpeed = velocity / 300;
				mSpeed = mClockWise > 0 ? Math.abs(mSpeed) : -Math.abs(mSpeed);
				mPreSpeed = mSpeed;
				mAcceleration = -mSpeed / 50;
				new Thread(this).start();
			} else {
				setPointerHoming();
			}
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
	 * ���ݴ����㣬���㻬���ĽǶ�
	 * */
	private double getDeltaAngle(PointF startPoint, PointF endPoint) {
		// Converts rectangular coordinates (x, y) to polar coordinate (r,
		// theta) and returns theta (-pi~pi).
		double endAngle = 0;
		double startAngle = 0;
		double deltaAngle = 0;
		endAngle = Math.atan2(endPoint.y - mCenter.y, endPoint.x - mCenter.x)
				* 180 / Math.PI;
		startAngle = Math.atan2(startPoint.y - mCenter.y, startPoint.x
				- mCenter.x)
				* 180 / Math.PI;
		/* �����û���ָ����180���������� */
		if (startPoint.y < mCenter.y && endPoint.y > mCenter.y// ������ʱ�뻬��ʱ����������ٽ��л�������
				&& endAngle - startAngle > 180) {// ���������㶼��view�������
			deltaAngle = endAngle - startAngle - 360;
		} else if (startPoint.y > mCenter.y && endPoint.y < mCenter.y// ����˳ʱ�뻬��ʱ����������ٽ��л�������
				&& endAngle - startAngle < -180) {// ���������㶼��view�������
			deltaAngle = endAngle - startAngle + 360;
		} else
			deltaAngle = endAngle - startAngle;
		return deltaAngle;
	}

	/**
	 * �������ڵ����������㣬�жϵ�ǰ��������
	 * 
	 * @return �����˳ʱ�뷽�򷵻�true�����򷵻�false
	 * */
	private boolean currentDirection(PointF startPoint, PointF endPoint) {
		if (getDeltaAngle(startPoint, endPoint) > 0)
			return true;
		return false;
	}

	public interface OnCompleteRotating {
		/**
		 * Բ��ֹͣת��ʱ�����øú���
		 * 
		 * @param pos
		 *            ָ�뵱ǰλ��
		 * */
		public void onCompleteRotating(int pos);
	}

	public void setOnCompleteRotatingListener(OnCompleteRotating listener) {
		mListener = listener;
	}
}
