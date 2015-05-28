package com.healthy.ui.dashboard;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.FoodInDb;
import com.healthy.ui.base.LineChartView;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/** ��������֧����� */
public class CaloriesDetail {

	private View mCaloriesDetail;
	private Context mContext;
	private LineChartView mLineChart;
	private Calendar mCalendar;
	private ImageView prevDate;
	private ImageView nextDate;
	private TextView curDate;

	public CaloriesDetail(Context context) {
		mContext = context;
		mCaloriesDetail = LayoutInflater.from(mContext).inflate(
				R.layout.page_calories_detail, null);

		mLineChart = (LineChartView) mCaloriesDetail
				.findViewById(R.id.line_chart);
		prevDate = (ImageView) mCaloriesDetail.findViewById(R.id.prev_date);
		nextDate = (ImageView) mCaloriesDetail.findViewById(R.id.next_date);
		curDate = (TextView) mCaloriesDetail.findViewById(R.id.cur_date);

		init();
		setListener();

	}

	/** ��ʼ����ؼ������������ */
	public void init() {
		mCalendar = Calendar.getInstance();
		curDate.setText(DateFormat.format("yyyy��", mCalendar.getTime()));
		setCalotrieValue();
	}

	private void setListener() {
		prevDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO �Զ����ɵķ������
				mCalendar.add(Calendar.YEAR, -1);
				curDate.setText(DateFormat.format("yyyy��", mCalendar.getTime()));
				setCalotrieValue();
			}
		});
		nextDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO �Զ����ɵķ������
				mCalendar.add(Calendar.YEAR, 1);
				curDate.setText(DateFormat.format("yyyy��", mCalendar.getTime()));
				setCalotrieValue();
			}
		});
	}

	public View getView() {
		return mCaloriesDetail;
	}

	/**
	 * �����������������ֵ
	 */
	private void setCalotrieValue() {
		float[] calorieResult = new float[12];
		DecimalFormat df = new DecimalFormat("0.00");

		// �������ĵı���
		HashMap<String, HashMap<String, Object>> measurements;// ���һ�����ڵ�ȫ����¼
		HashMap<String, Object> measurement;// ĳһ���˶���
		float[] calorieBurned = new float[12];

		// ��������ı���
		float[] calorieEat = new float[12];
		List<FoodInDb> foodList;// һ�����ڵ�ȫ������ʳ������

		for (int i = 0; i < calorieBurned.length; i++) {// ��ʼ������
			calorieBurned[i] = 0;
			calorieEat[i] = 0;
		}

		for (int i = 0; i < 12; i++) {
			// ��λʱ��
			mCalendar.set(Calendar.MONTH, i);
			// ��ѯ���㵱������
			measurements = new HashMap<String, HashMap<String, Object>>();
			measurements = HealthyApplication.mDbUtil
					.getMonthActivityData(DateFormat.format("yyyy-MM-dd",
							mCalendar.getTime()).toString());
			if (measurements != null) {// ���ش��е���������������
				Iterator<Entry<String, HashMap<String, Object>>> iterator = measurements
						.entrySet().iterator();
				while (iterator.hasNext()) {
					Map.Entry<String, HashMap<String, Object>> entry = iterator
							.next();
					measurement = entry.getValue();
					calorieBurned[i] += Float.valueOf(measurement.get(
							"calories_burned").toString());
				}
			}

			// ��ѯ���㵱������
			foodList = HealthyApplication.mDbUtil.queryMonthFood(mCalendar
					.get(Calendar.YEAR) + "-" + mCalendar.get(Calendar.MONTH));
			if (foodList != null) {// ���ش���������������
				for (FoodInDb food : foodList) {
					calorieEat[i] += food.calorie * food.num / 100;
				}
			}

			calorieResult[i] = Float.valueOf(df.format(calorieBurned[i]
					- calorieEat[i]));// ��������-��������
		}
		mLineChart.setItemValues(calorieResult);
	}

}
