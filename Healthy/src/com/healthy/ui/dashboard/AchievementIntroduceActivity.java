package com.healthy.ui.dashboard;

import java.util.ArrayList;
import java.util.List;

import com.healthy.R;
import com.healthy.logic.model.Introduce;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;

public class AchievementIntroduceActivity extends Activity{

	private ListView mIntroduce;
	private ImageView mBack;
	private List<Introduce> mIntroduceData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_introduce_achieve);
		mIntroduce = (ListView)findViewById(R.id.achieve_introduce_list);
		mBack = (ImageView)findViewById(R.id.back_introduce_btn);
		
		mBack.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
				overridePendingTransition(0,R.anim.roll_down);
			}
		});
		
		setListData();
	}
	
	private void setListData(){
		mIntroduceData = new ArrayList<Introduce>();
		mIntroduceData.add(new Introduce("����é®", "��һ��ʹ�ý������˻�óɾ�"));
		mIntroduceData.add(new Introduce("�켣����", "�ɹ���¼��һ���켣"));
		mIntroduceData.add(new Introduce("�켣��", "ӵ��50���켣"));
		mIntroduceData.add(new Introduce("�켣����", "ӵ��100���켣"));
		mIntroduceData.add(new Introduce("���ɵ���", "ӵ��500���켣"));
		mIntroduceData.add(new Introduce("���˶���", "���쾲ֹ����6Сʱ"));
		mIntroduceData.add(new Introduce("������ɽ", "���쾲ֹ����8Сʱ"));
		mIntroduceData.add(new Introduce("ɢ������", "������·����6000��"));
		mIntroduceData.add(new Introduce("����ɢ��", "������·������Сʱ"));
		mIntroduceData.add(new Introduce("��������", "������·����1��"));
		mIntroduceData.add(new Introduce("������·", "�ۼ��߹�800��"));
		mIntroduceData.add(new Introduce("�ܲ���", "�����ܲ���"));
		mIntroduceData.add(new Introduce("�ܲ���", "�����ܲ���Сʱ"));
		mIntroduceData.add(new Introduce("�桤�ܲ�", "�ۼ��ܲ�����100Сʱ"));
		
		IntroduceAdapter adapter = new IntroduceAdapter(this, mIntroduceData);
		mIntroduce.setAdapter(adapter);
	}
	
	@Override
	protected void onPause() {
		overridePendingTransition(0,R.anim.roll_down);
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	
	
}
