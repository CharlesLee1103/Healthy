package com.healthy.logic;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;

import com.healthy.db.DBUtil;

public class HealthyApplication extends Application {

	public static String APPLICATION_PATH;// ������Ĵ��·��
	public static DBUtil mDbUtil;
	public static AsyncHealthy mAsyncHealthy = AsyncHealthy.getInstance();
	public static Bitmap mapAvatar;//ͷ��
	public static List<String> keyResult;//�ؼ�������
	public static float calories;//�����Ŀ�·����
	public static String mRanking;//��·�����ķ����ַ���
	public static Map<String, SoftReference<Bitmap>> imageCache;
	public static float phoneScale;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		APPLICATION_PATH = getExternalFilesDir(null).getAbsolutePath();
		mDbUtil = new DBUtil(getApplicationContext());// �������ݿ⹤����
		imageCache = new HashMap<String, SoftReference<Bitmap>>();
		phoneScale = getResources().getDisplayMetrics().density;
		// �ڽ�����Ե�ʱ��û�д򿪷�����ʽ���Ե�ʱ��Ҫ�����޸ģ�����
		// ������̨����BackgroundService
		Intent intent = new Intent(this, BackgroundService.class);
		startService(intent);
	}

}
