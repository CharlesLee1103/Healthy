package com.healthy.ui.foods;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.FoodInDb;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;
import com.healthy.ui.foods.FoodListAdapter.ViewUpdateListener;
import com.healthy.util.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

/** ��������������� */
public class Foods implements ViewUpdateListener {

	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd");
	private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat(
			"HH:mm");
	private static final String TEXT_FORMAT = "<font color='#00bfff'>%s</font> kcal";
	private static final DecimalFormat DF = new DecimalFormat("0.00");

	private View mFoods;
	private Context mContext;
	private ImageView mFlipMenu;
	private OnOpenListener mOnOpenListener;

	// �ʱ��ĵ�������ʾ
	private ImageView mPreDate;
	private ImageView mNextDate;
	private TextView mCurDate;
	private Calendar mCalendar = Calendar.getInstance();
	private int isToday = 0;// ��Ϊ0��ʱ��Ϊ����

	// ʳ���б�
	private ExpandableListView mFoodsList;
	private List<Map<String, Object>> mGroup = new ArrayList<Map<String, Object>>();// �Է�ʱ��
	private List<List<FoodInDb>> mChild = new ArrayList<List<FoodInDb>>();// ʳ��
	private float mTotalCalorie;
	private String[] mGroupName;
	private String[] mGroupCriteria;
	private FoodListAdapter mAdapter;

	private ImageView mFoodPlan;
	private ImageView mFoodAdd;// ���ʳ�ﰴť

	// ��ʳ�ƻ�
	private String mPlanStartDate = "";// �洢��ʽ��yyyy-MM-dd��
	private int mPlanDuration;// �ƻ�����ʱ��

	// ��ʾ��ǰ����ʳ�������������������
	private TextView mRemainingCalorie;// ��ʾ�������������������ʳ��
	private float mCalorieOfPlan = 2000;// �ƻ�ÿ�����������
	private ImageView mFigCanStilEat;
	private int[] mFigIndexCanStilEat = { R.drawable.gauge_under,
			R.drawable.gauge_zone, R.drawable.gauge_over };
	private int mCurrentIndexCanStilEat = 0;

	public Foods(Context context) {
		mContext = context;
		mFoods = LayoutInflater.from(mContext).inflate(R.layout.page_foods,
				null);
		init();
		setListener();
	}

	public void init() {

		mPreDate = (ImageView) mFoods.findViewById(R.id.prev_date);
		mNextDate = (ImageView) mFoods.findViewById(R.id.next_date);
		mCurDate = (TextView) mFoods.findViewById(R.id.cur_date);
		mFoodsList = (ExpandableListView) mFoods.findViewById(R.id.foods_list);
		mFoodPlan = (ImageView) mFoods.findViewById(R.id.food_plan);
		mFoodAdd = (ImageView) mFoods.findViewById(R.id.food_add);
		mRemainingCalorie = (TextView) mFoods
				.findViewById(R.id.tip_can_stil_eat);
		mFigCanStilEat = (ImageView) mFoods
				.findViewById(R.id.fig_can_still_eat);

		mFlipMenu = (ImageView) mFoods.findViewById(R.id.flip_menu);

		// ��ȡ��ǰ���ڣ���ɳ�ʼ��
		Date date = new Date();
		mCalendar.setTime(date);
		mCurDate.setText(DateFormat.format("yyyy-MM-dd", date).toString());

		initData();
		showPlanCalori();

	}

	private void initData() {
		mGroupName = mContext.getResources().getStringArray(
				R.array.foods_group_names);
		mGroupCriteria = mContext.getResources().getStringArray(
				R.array.foods_group_criteria);  
		getGroupList();
		getChildList();
		mAdapter = new FoodListAdapter(mContext, mGroup, mChild);
		mAdapter.setViewUpdateListener(this);
		mFoodsList.setAdapter(mAdapter);
		for (int i = 0; i < mGroup.size(); i++) {// �����е���Ŀ¼ȫ����ʾ
			mFoodsList.expandGroup(i);
		}
		getTotalCalorie();
		queryFoodplan();
	}

	private void getGroupList() {
		for (int i = 0; i < mGroupName.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", mGroupName[i]);
			map.put("time", mGroupCriteria[i]);
			mGroup.add(map);
		}
	}

	private void getChildList() {

		// ��ѯ����ʳ��
		List<FoodInDb> list = HealthyApplication.mDbUtil
				.queryDayFood(DATE_FORMATTER.format(mCalendar.getTime()));

		List<FoodInDb> breakfast = new ArrayList<FoodInDb>();// ��Ͷ���
		List<FoodInDb> lunch = new ArrayList<FoodInDb>();// ��Ͷ���
		List<FoodInDb> dinner = new ArrayList<FoodInDb>();// ��Ͷ���
		List<FoodInDb> midSnack = new ArrayList<FoodInDb>();// ҹ������
		mChild.add(breakfast);
		mChild.add(lunch);
		mChild.add(dinner);
		mChild.add(midSnack);

		// ���ݲ�ͬ����ʳʱ�䣬��ӵ���ͬ����Ŀ��
		for (FoodInDb food : list) {
			if (compareTime(food.time, mGroupCriteria[0]) < 0)
				breakfast.add(food);
			else if (compareTime(food.time, mGroupCriteria[1]) < 0)
				lunch.add(food);
			else if (compareTime(food.time, mGroupCriteria[2]) < 0)
				dinner.add(food);
			else
				midSnack.add(food);
		}

		getTotalCalorie();
	}

	/**
	 * �Ƚ�����ʱ����Ⱥ�
	 * 
	 * @param time1
	 * @param time2
	 * @return ���time1����������1������緢������-1�����򷵻�0
	 * 
	 * */
	private int compareTime(String time1, String time2) {
		try {
			Date date1 = TIME_FORMATTER.parse(time1);
			Date date2 = TIME_FORMATTER.parse(time2);
			if (date1.getTime() - date2.getTime() > 0)
				return 1;
			if (date1.getTime() - date2.getTime() < 0)
				return -1;
			return 0;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Healthy", "compareTime", e);
		}
		return 0;
	}

	/**
	 * ���㵱��calorie�ܺ�
	 */
	private void getTotalCalorie() {
		mTotalCalorie = 0;
		for (List<FoodInDb> list : mChild) {
			for (FoodInDb mData : list) {
				mTotalCalorie += (mData.calorie * mData.num / 100);
			}
		}
	}

	public View getView() {
		return mFoods;
	}

	private void setListener() {

		/* ǰһ�� */
		mPreDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCalendar.add(Calendar.DAY_OF_MONTH, -1);
				mCurDate.setText(DateFormat.format("yyyy-MM-dd",
						mCalendar.getTime()));
				updateFoodData(true);
				isToday--;
				if (isToday == 0)
					mFoodAdd.setVisibility(View.VISIBLE);
				else
					mFoodAdd.setVisibility(View.INVISIBLE);// ����鿴�Ĳ��ǵ����¼����ֹ���ʳ��
				showPlanCalori();
			}
		});

		/* ��һ�� */
		mNextDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mCalendar.add(Calendar.DAY_OF_MONTH, 1);
				mCurDate.setText(DateFormat.format("yyyy-MM-dd",
						mCalendar.getTime()));
				updateFoodData(true);
				isToday++;
				if (isToday == 0)
					mFoodAdd.setVisibility(View.VISIBLE);
				else
					mFoodAdd.setVisibility(View.INVISIBLE);// ����鿴�Ĳ��ǵ����¼����ֹ���ʳ��
				showPlanCalori();
			}
		});

		// �����ʳ�ƻ�
		mFoodPlan.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO �Զ����ɵķ������
				Intent newIntent = new Intent(mContext, FoodPlanActivity.class);
				((Activity) mContext).startActivityForResult(newIntent,
						Constants.ActivityRequestCode.FOODPLAN);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});

		// ���ʳ��
		mFoodAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, FoodEditActivity.class);
				((Activity) mContext).startActivityForResult(intent,
						Constants.ActivityRequestCode.FOODADD);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});

		mFlipMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnOpenListener.open();
			}
		});

		mFoodsList
				.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

					@Override
					public boolean onGroupClick(ExpandableListView parent,
							View v, int groupPosition, long id) {
						// TODO Auto-generated method stub
						return true;
					}
				});
	}

	/**
	 * ʣ�����������
	 * */
	private void showPlanCalori() {
		if (!checkPlanDuration()) {
			mCalorieOfPlan = 2000;
		}
		String tip_can_stil_eat;

		if (mTotalCalorie >= mCalorieOfPlan) {
			tip_can_stil_eat = String.format("�ѳ����ƻ� " + TEXT_FORMAT,
					DF.format(mTotalCalorie - mCalorieOfPlan));
			mCurrentIndexCanStilEat = 2;
		} else {
			if (mTotalCalorie < 2.0 / 3.0 * mCalorieOfPlan)
				mCurrentIndexCanStilEat = 0;
			else
				mCurrentIndexCanStilEat = 1;
			tip_can_stil_eat = String.format("���������� " + TEXT_FORMAT,
					DF.format(mCalorieOfPlan - mTotalCalorie));
		}
		Spanned spt = Html.fromHtml(tip_can_stil_eat);
		mRemainingCalorie.setText(spt);
		mFigCanStilEat
				.setImageResource(mFigIndexCanStilEat[mCurrentIndexCanStilEat]);
	}

	/**
	 * ��ѯ���ݿ��й�����ʳ�ƻ�����
	 */
	private void queryFoodplan() {
		Map<String, Object> tempMap = new HashMap<String, Object>();
		tempMap = HealthyApplication.mDbUtil.queryFoodPlan();
		if (!tempMap.isEmpty()) {
			mPlanStartDate = tempMap.get("start_time").toString();
			mPlanDuration = Integer
					.parseInt(tempMap.get("duration").toString());
			mCalorieOfPlan = Float
					.valueOf(tempMap.get("daycalorie").toString());
		}
	}

	/**
	 * ��������
	 */
	public void updateFoodData(boolean viewChange) {
		mChild.clear();
		getChildList();
		mAdapter.notifyDataSetChanged();
		queryFoodplan();
		showPlanCalori();
	}

	/**
	 * ������ʳ�ƻ�ʣ��ʱ��
	 */
	private boolean checkPlanDuration() {
		if (mPlanStartDate.equals("") || mPlanStartDate == null)
			return false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date tempDate = null;
		long timeDiff;
		try {
			tempDate = sdf.parse(mPlanStartDate);
		} catch (ParseException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		Calendar startCalendar = Calendar.getInstance();
		startCalendar.setTime(tempDate);
		timeDiff = (mCalendar.getTimeInMillis() - startCalendar
				.getTimeInMillis()) / (3600 * 1000 * 24);
		if (mPlanDuration >= timeDiff) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void updateView(boolean viewChange) {
		// TODO Auto-generated method stub
		updateFoodData(viewChange);
		Log.i("test", "update");
	}

	public void setPlanCalori(float calorieOfPlan) {
		mCalorieOfPlan = calorieOfPlan;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}
}
