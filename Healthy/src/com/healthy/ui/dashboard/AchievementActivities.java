package com.healthy.ui.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.Achievement;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class AchievementActivities {

	private Context mContext;
	private View mAchievement;
	private ListView mAchievementList;
	private ImageView mAchievementBtn;
	
	private List<Achievement> mAchievementData;
	
	private HashMap<String, HashMap<String, Object>> mDayMeasurements;//����ͳ������
	private HashMap<String,Object> mMeasurement;//ĳ����ĸ�������ͳ��
	
	public AchievementActivities(Context context)
	{
		mContext = context;
		mAchievement = LayoutInflater.from(mContext).inflate(R.layout.page_achievement, null);
		mAchievementList = (ListView)mAchievement.findViewById(R.id.achievement_list);
		mAchievementBtn = (ImageView)mAchievement.findViewById(R.id.achievement_imagebtn);
		mAchievementData  = new ArrayList<Achievement>();
		
		mAchievementBtn.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext,AchievementIntroduceActivity.class);
				mContext.startActivity(intent);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});
		initFirst();
		initTrackerAchieve();
		initMovement();
		initListView();
	}
	
	public View getView()
	{
		return mAchievement;
	}
	
	/**
	 * �Ƿ��һ��ʹ��--����é®
	 * ��ʱÿ�ζ�����
	 */
	private void initFirst()
	{
		Achievement achievement = new Achievement("��ӭ��������������-", "����é®");
		mAchievementData.add(achievement);
	}
	
	/**
	 * ��켣��صĳɾ�
	 * 1.�ɹ���¼��һ���켣---�켣����
	 * 2.ӵ��50���켣---�켣��
	 * 3.ӵ��100���켣---�켣����
	 * 4.ӵ��500���켣---���ɵ���
	 */
	private void initTrackerAchieve(){
		int count=0;
		count = HealthyApplication.mDbUtil.getLastTrackerID();
		if(count > 0){
			Achievement achievement = new Achievement("��ϲ������˳ɾ�-", "�켣����");
			mAchievementData.add(achievement);
			if(count >= 50){
				Achievement achievementTwo = new Achievement("��ϲ������˳ɾ�-", "�켣��");
				mAchievementData.add(achievementTwo);
				if(count >= 100){
					Achievement achievementThree = new Achievement("��ϲ������˳ɾ�-", "�켣����");
					mAchievementData.add(achievementThree);
					if(count >= 500){
						Achievement achievementFour = new Achievement("��ϲ������˳ɾ�-", "���ɵ���");
						mAchievementData.add(achievementFour);
					}
				}
			}
		}
		
	}
	
	/**
	 * �˶�ϵ�ɾ�
	 * 1.��ֹ����8Сʱ---������ɽ
	 * 2.��ֹ����6Сʱ---���˶���
	 * 2.��·����6000��---ɢ�����ˣ�ÿ�죩
	 * 3.��·������Сʱ---����ɢ����ÿ�죩
	 * 4.��·����1��---�������(��������±���˵��)
	 * 5.��·����800��---������·(�������ѧ)
	 * 6.��һ���ܲ�---�ܲ���
	 * 7.�ܲ���Сʱ---�ܲ��أ�ÿ�죩
	 * 8.�ܲ��ۼƳ���100Сʱ---�桤�ܲ�
	 * ע���ۼƵ���Ҫ�Ķ�������ʱ��϶̣�ֱ�Ӳ�ѯ�������ݿ�
	 */
	private void initMovement()
	{
		Date date = new Date();
		mDayMeasurements = new HashMap<String, HashMap<String,Object>>();
		mDayMeasurements = HealthyApplication.mDbUtil.getDailyActivityData(DateFormat.format("yyyy-MM-dd", date).toString());
		if(mDayMeasurements!=null){
			if(mDayMeasurements.containsKey("stationary")){
				mMeasurement = mDayMeasurements.get("stationary");
				float time = Float.valueOf(mMeasurement.get("duration").toString());
				if(time>21600){
					Achievement achievement = new Achievement("������������˳ɾ�-", "���˶���");
					mAchievementData.add(achievement);
				}
				if(time>28800){
					Achievement achievement = new Achievement("������������˳ɾ�-", "������ɽ");
					mAchievementData.add(achievement);
				}
			}
			if(mDayMeasurements.containsKey("walking")){
				mMeasurement = mDayMeasurements.get("walking");
				float stride = Float.valueOf(mMeasurement.get("strides").toString());
				if(stride>6000){
					Achievement achievement = new Achievement("��ϲ������˳ɾ�-", "ɢ������");
					mAchievementData.add(achievement);
				}
				if(stride>10000){
					Achievement achievement = new Achievement("��ϲ������˳ɾ�-", "��������");
					mAchievementData.add(achievement);
				}
			}
			if(mDayMeasurements.containsKey("jogging")){
				mMeasurement = mDayMeasurements.get("jogging");
				float time = Float.valueOf(mMeasurement.get("duration").toString());
				if(time>0){
					Achievement achievement = new Achievement("��ϲ������˳ɾ�-", "�ܲ���");
					mAchievementData.add(achievement);
				}
				if(time>1800){
					Achievement achievement = new Achievement("��ϲ������˳ɾ�-", "�ܲ���");
					mAchievementData.add(achievement);
				}
			}
		}
	}
	
	private void initListView()
	{
		Collections.reverse(mAchievementData);//list����
		AchievementActivityAdapter adapter = new AchievementActivityAdapter(mContext, mAchievementData);
		mAchievementList.setAdapter(adapter);
	}
	
}
