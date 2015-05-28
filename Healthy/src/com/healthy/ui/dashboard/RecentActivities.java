package com.healthy.ui.dashboard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.Measurement;
import com.healthy.ui.base.PieChartView;
import com.healthy.ui.base.PieChartView.OnCompleteRotating;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/** �����ֲ���� */
public class RecentActivities {

	private View mRecentActivity;
	private View mChartLayout;
	private ImageView mEmptyChart;
	private Context mContext;
	private PieChartView mPieChartView;// ��ֲ��ı�ͼ
	private RecentActivityAdapter mAdapter;
	private ListView mDisplay;
	private String[] mActivities;// �����
	private float[] mRatios;// ���ֻ��ռ�ı���

	// �ʱ��ĵ�������ʾ
	private ImageView mPreDate;
	private ImageView mNextDate;
	private TextView mCurDate;
	private Calendar mCalendar = Calendar.getInstance();

	private HashMap<String, HashMap<String, Object>> mMeasurements;// �ͳ������

	public RecentActivities(Context context) {
		mContext = context;
		mRecentActivity = LayoutInflater.from(mContext).inflate(
				R.layout.page_recent_activities, null);
		mPieChartView = (PieChartView) mRecentActivity
				.findViewById(R.id.pie_chart);
		mChartLayout = mRecentActivity.findViewById(R.id.chart_layout);
		mEmptyChart = (ImageView) mRecentActivity
				.findViewById(R.id.empty_chart);
		mDisplay = (ListView) mRecentActivity.findViewById(R.id.display);
		mPreDate = (ImageView) mRecentActivity.findViewById(R.id.prev_date);
		mNextDate = (ImageView) mRecentActivity.findViewById(R.id.next_date);
		mCurDate = (TextView) mRecentActivity.findViewById(R.id.cur_date);
		init();
	}

	public View getView() {
		return mRecentActivity;
	}

	/** ���ݵĽ��г�ʼ�� */
	public void init() {
		// ��ȡ��ǰ���ڣ���ɳ�ʼ��
		Date date = new Date();
		mCalendar.setTime(date);
		mCurDate.setText(DateFormat.format("yyyy-MM", date).toString());
		initData(date);
		setListener();
	}

	private void initData(Date date) {
		// �����ݿ��ж�ȡ���ݣ����󶨿ؼ�
		if (!getActivityData(DateFormat.format("yyyy-MM-dd", date).toString())) {// δ�ҵ������������
			mChartLayout.setVisibility(View.GONE);
			mEmptyChart.setVisibility(View.VISIBLE);
		} else {// ��ѯ���������
			mChartLayout.setVisibility(View.VISIBLE);
			mEmptyChart.setVisibility(View.GONE);
		}
	}

	/**
	 * @return ����û��������ݣ�����false�����򷵻�true
	 * */
	private boolean getActivityData(String date) {
		mMeasurements = HealthyApplication.mDbUtil.getMonthActivityData(date);
		if (mMeasurements == null)
			return false;
		mActivities = new String[mMeasurements.size()];
		String[] activities = new String[mMeasurements.size()];
		mRatios = new float[mMeasurements.size()];
		Iterator<Entry<String, HashMap<String, Object>>> iterator = mMeasurements
				.entrySet().iterator();
		int i = 0;
		long sum = 0;
		while (iterator.hasNext()) {
			Entry<String, HashMap<String, Object>> entry = iterator.next();
			String key = entry.getKey();// ��ȡ����
			mActivities[i] = new String(key);
			
			activities[i] = HealthyApplication.mDbUtil.changeTypeToReadable(new String(key));
			HashMap<String, Object> measurement = (HashMap<String, Object>) entry// ��Ӧ�����
					.getValue();
			mRatios[i] = (Float) measurement.get("duration");
			sum += mRatios[i];
			i++;
		}
		for (i = 0; i < mMeasurements.size(); i++) {
			mRatios[i] /= sum;
		}
		mPieChartView.initData(activities, mRatios);
		return true;
	}

	private void setListener() {
		mPieChartView.setOnCompleteRotatingListener(new OnCompleteRotating() {
			@Override
			public void onCompleteRotating(int pos) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain(handler);
				msg.arg1 = pos;
				msg.what = 0;
				msg.sendToTarget();
			}
		});

		/* ��һ���� */
		mPreDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCalendar.add(Calendar.MONTH, -1);
				mCurDate.setText(DateFormat.format("yyyy-MM",
						mCalendar.getTime()));
				initData(mCalendar.getTime());
			}
		});

		/* ��һ���� */
		mNextDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCalendar.add(Calendar.MONTH, 1);
				mCurDate.setText(DateFormat.format("yyyy-MM",
						mCalendar.getTime()));
				initData(mCalendar.getTime());
			}
		});
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			List<Measurement> measurementList = new ArrayList<Measurement>();
			HashMap<String, Object> map = mMeasurements
					.get(mActivities[msg.arg1]);

			// ����ͳ��
			measurementList.add(new Measurement("����:", map.get("strides")
					.toString()));

			// �����ʱ�䣬��СʱΪ��λ
			measurementList.add(new Measurement("ʱ��:", String.format(
					"%.2f", (Float) map.get("duration") / 3600.0f) + " h"));

			// ����룬��mΪ��λ
			measurementList.add(new Measurement("����:", String.format(
					"%.2f", map.get("distance")) + " m"));

			// �ٶ�
			measurementList.add(new Measurement("�ٶ�:", String.format("%.2f",
					map.get("speed")) + " m/s"));

			// ��������
			measurementList.add(new Measurement("��·������:", String.format(
					"%.2f", map.get("calories_burned")) + " kcal"));
			mAdapter = new RecentActivityAdapter(mContext, measurementList);
			mDisplay.setAdapter(mAdapter);
		}

	};

}
