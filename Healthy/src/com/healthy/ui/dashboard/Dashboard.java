package com.healthy.ui.dashboard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.FoodInDb;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.ViewFlipper;
/**
 * Dashboard���棬����������ҳ��RecentActivity ��CaloriesDetail, zc
 * 
 */
public class Dashboard {

	private View mDashboard;
	private Context mContext;
	/*�����������뿨·����ʾ*/
	private TextView mConsumerText;
	private TextView mIntakeText;
	private TextView mSumText;
	
	private ViewFlipper mContent;//radiogroup��Ļ�л�ʵ��
	private RadioGroup mAllCategory;
	private DayActivities mTodayActivity;
	private AchievementActivities mAchieveActivity;
	
	private ImageView mFlipMenu;
	private OnOpenListener mOnOpenListener;

	public Dashboard(Context context) {

		mContext = context;
		mDashboard = LayoutInflater.from(mContext).inflate(
				R.layout.page_dashboard, null);
		
		mContent = (ViewFlipper)mDashboard.findViewById(R.id.dashboard_content_flipper);
		mAllCategory = (RadioGroup)mDashboard.findViewById(R.id.dashboard_radiogroup);
		mConsumerText = (TextView)mDashboard.findViewById(R.id.dashboard_consume_sum);
		mIntakeText = (TextView)mDashboard.findViewById(R.id.dashboard_intake_sum);
		mSumText = (TextView)mDashboard.findViewById(R.id.dashboard_sum);
		mFlipMenu = (ImageView)mDashboard.findViewById(R.id.flip_menu);
		
		mTodayActivity = new DayActivities(mContext);
		mAchieveActivity = new AchievementActivities(mContext);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT);
		
		mContent.addView(mTodayActivity.getView(), params);
		mContent.addView(mAchieveActivity.getView(), params);
		
		initSum();
		setListener();
		
	}

	public View getView() {
		return mDashboard;
	}
	
	private void setListener()
	{	
		mAllCategory.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
			
				switch(checkedId){
				case R.id.dashboard_movement_index_btn:
					mContent.setInAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_in_left));
					mContent.setOutAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_out_right));
					mContent.setDisplayedChild(0);
					break;
				case R.id.dashboard_achievement_btn:
					mContent.setInAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_in_right));
					mContent.setOutAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_out_left));
					mContent.setDisplayedChild(1);
					break;
				case R.id.dashboard_healthy_btn://��ʱ���ý�����ʾ
					mContent.setInAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_in_right));
					mContent.setOutAnimation(AnimationUtils
							.loadAnimation(mContext,
									R.anim.slide_out_left));
					mContent.setDisplayedChild(2);
					break;
				}
			}
		});
		mFlipMenu.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnOpenListener.open();
			}
		});
		
	}

	/**
	 * ���㵱ǰ�¿�·�����ĺ��������� ����ϼ�
	 */
	private void initSum() {
		Date date = new Date();
		DecimalFormat df = new DecimalFormat("0.00");
		// �������ĵı���
		HashMap<String, HashMap<String, Object>> measurements;// ���һ�����ڵ�ȫ����¼
		HashMap<String, Object> measurement;// ĳһ���˶���
		float caloriesBurned = 0;

		// ��������ı���
		List<FoodInDb> foodList;
		float caloriesEat = 0;

		// �ϼ�
		float caloriesResult = 0;
		// ��������
		measurements = HealthyApplication.mDbUtil
				.getMonthActivityData(DateFormat.format("yyyy-MM-dd", date)
						.toString());
		if (measurements == null) {
			mConsumerText.setText("0kcal");
		} else {
			Iterator<Entry<String, HashMap<String, Object>>> iterator = measurements
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, HashMap<String, Object>> entry = iterator.next();
				measurement = entry.getValue();
				caloriesBurned += Float.valueOf(measurement.get(
						"calories_burned").toString());
			}
			HealthyApplication.calories = caloriesBurned;
			mConsumerText.setText(df.format(caloriesBurned) + "kcal");
		}

		// ��������
		foodList = HealthyApplication.mDbUtil
				.queryMonthFood(new SimpleDateFormat("yyyy-MM-dd").format(date));// �����·ݴ�0��ʼ��Ҫ��1
		if (foodList.size() == 0) {
			mIntakeText.setText("0kcal");
		} else {
			for (FoodInDb food : foodList) {
				caloriesEat += food.calorie * food.num / 100;
			}
			mIntakeText.setText(caloriesEat + "kcal");
		}

		caloriesResult = Float.valueOf(df.format(caloriesBurned - caloriesEat));
		mSumText.setText(caloriesResult + "kcal");
	}
	
	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
	
	
}
