package com.healthy.logic;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;

import com.healthy.classifier.Recognizer;
import com.healthy.logic.model.ActivityInDb;
import com.healthy.logic.model.SensorInDb;
import com.healthy.util.LogUtil;

public class BackgroundService extends Service implements SensorEventListener {

	private static final SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// ����ͬ�Ļ����Activities���л�Ķ���˳���������
	// ע�ⲻ����Browsing����û�����г�
	// private static final String[] activities = new String[] { "stationary",
	// "escalator", "lift", "walking", "jogging", "bicycling",
	// "ascendingStairs", "descendingStairs" };

	private static final String[] activities = new String[] { "stationary",
			"lift", "walking", "jogging", "bicycling", "ascendingStairs",
			"descendingStairs" };

	// ���Լ���ֻ���Ļ�仯���
	private PhoneStateReceiver psr;
	private String startBrowsingTime;
	private String endBrowsingTime;

	// Alarm receiver
	private ProcessingTask task;

	// ����������
	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private Sensor magneticSensor;

	// ��Ųɼ������ݵ�
	private SensorInDb sdTmp;
	private SensorInDb[] sdQueue = new SensorInDb[2];// ��ŵ�ǰҪ���д���Ĵ���������
	private int currentQueue = 0;// ��ǰҪ����Ĵ��������ݼ�

	// ��ʱ��������
	private AlarmManager am;
	private PendingIntent pi;

	// ���ڴ洢�ϴεĻ��Ϣ���ж�֮������Ƿ�������ݿ�
	private static ActivityInDb tempData = new ActivityInDb();
	// �ж��Ƿ񱣴���temp���ݣ�����Ѿ����棬���ж����ͣ���ͬ�ͽ�temp���룬�����䲽��
	private static boolean tempSaved = false;

	private static final SimpleDateFormat TIMESTAMP_FMT = new SimpleDateFormat(
			"[HH:mm:ss] ");

	private PowerManager.WakeLock wakeLock;

	private boolean allowActivityRecog = true;// �Ƿ�����ʶ��

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		// �Ѽ����������ݣ����ٶȺͷ������ݣ�
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			sdQueue[currentQueue].xAcc.add((double) event.values[0]);
			sdQueue[currentQueue].yAcc.add((double) event.values[1]);
			sdQueue[currentQueue].zAcc.add((double) event.values[2]);
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			sdQueue[currentQueue].MagxData.add((double) event.values[0]);
			sdQueue[currentQueue].MagyData.add((double) event.values[1]);
			sdQueue[currentQueue].MagzData.add((double) event.values[2]);
		}

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		System.out.println("��������");
		
		sdQueue[0] = new SensorInDb();
		sdQueue[1] = new SensorInDb();
		sdQueue[0].clearData();
		sdQueue[1].clearData();

		acquireWakelock();

		if (isScreenOn()) {// ��Ļ�ڳ�ʼ״̬�´�
			allowActivityRecog = false;// ��ֹ�ʶ�����������ݿ�
			startBrowsingTime = formatter.format(new Date(System
					.currentTimeMillis()));// ��¼�ֻ���Ļ������ʱ��
			// ���������¼��ʱ��
			HealthyApplication.mDbUtil.updateActivityEndTime(startBrowsingTime);
		}

		// �ڷ�����̴�����ע�ᴫ�������ɼ�����
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magneticSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		sensorManager.registerListener(this, accelerometerSensor,
				SensorManager.SENSOR_DELAY_GAME);
		sensorManager.registerListener(this, magneticSensor,
				SensorManager.SENSOR_DELAY_GAME);

		// ����ֻ���Ļ�Ŀ���
		psr = new PhoneStateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(psr, filter);

		task = new ProcessingTask();
		registerReceiver(task, new IntentFilter("ACTION_ALARM"));
		am = (AlarmManager) getSystemService(ALARM_SERVICE);
		pi = PendingIntent.getBroadcast(this, 0, new Intent("ACTION_ALARM"), 0);
		long now = System.currentTimeMillis() + 10000;
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, now, 10000, pi);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
		System.out.println("���ٷ���");

		// �ڷ�����̽���ʱ��ע��������
		sensorManager.unregisterListener(this, accelerometerSensor);
		sensorManager.unregisterListener(this, magneticSensor);

		// �ڷ�����̽���ʱ��ֹͣ���չ㲥�����ټ�¼��Ļ
		unregisterReceiver(psr);
		unregisterReceiver(task);
		am.cancel(pi);

		// �ڷ�����̽���ʱ���ر����ݿ�
		HealthyApplication.mDbUtil.closeDb();

		releaseWakelock();
		super.onDestroy();
	}

	/**
	 * ���������ֻ���Ļ�Ŀ����ر�״̬
	 * 
	 * */
	class PhoneStateReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {// �ֻ���Ļ����
				allowActivityRecog = false;// ��ֹ�ʶ�����������ݿ�
				startBrowsingTime = formatter.format(new Date(System
						.currentTimeMillis()));// ��¼�ֻ���Ļ������ʱ��
				// ���������¼��ʱ��
				HealthyApplication.mDbUtil
						.updateActivityEndTime(startBrowsingTime);
				tempSaved=false;//����ѻ���Ļ����
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {// �ֻ���Ļ�ر�
				sensorManager.unregisterListener(BackgroundService.this);
				sensorManager.registerListener(BackgroundService.this,
						accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
				sensorManager.registerListener(BackgroundService.this,
						magneticSensor, SensorManager.SENSOR_DELAY_GAME);

				allowActivityRecog = true;// �����ʶ�����������ݿ�
				endBrowsingTime = formatter.format(new Date(System
						.currentTimeMillis()));// ��¼�ֻ���Ļ�رյ�ʱ��

				// ����Browsing��¼
				ActivityInDb data = new ActivityInDb();
				data.kind = "browsing";
				data.start_time = startBrowsingTime;
				data.end_time = endBrowsingTime;
				data.strideCount = 0;
				HealthyApplication.mDbUtil.insertIntoActivity(data);
			}
		}
	}

	class ProcessingTask extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			if (allowActivityRecog) {// ����ʶ��
				// ����ǰ10s�е�����
				sdTmp = sdQueue[currentQueue];// ǳ����
				currentQueue = (currentQueue + 1) % 2;
				if (sdTmp.xAcc.size() != 0) {
					// ��ʼʶ������
					int result = Recognizer.recognize(sdTmp,
							BackgroundService.this);

					int strideCount = Recognizer.strideCount;
					// ����ʶ���ʱ�򣬲ſ��Խ�����������ݿ���
					// ���ڲ���Ȼ��Ҫ���д��жϵ�ԭ������Ļ�򿪣���allowActivityRecogΪfalseʱ�򣬳����Ѿ�ִ�е��˴�
					if(allowActivityRecog)
						insertIntoDB(activities[result - 1], strideCount);

					// ��ӡ��־��Ϣ
					System.out.println("activity----->"
							+ activities[result - 1] + "-----strideCount----->"
							+ strideCount);
					LogUtil.addLog(TIMESTAMP_FMT.format(new Date())
							+ activities[result - 1] + " " + strideCount);
				} else {
					System.out.println("There are no data");
					LogUtil.addLog(TIMESTAMP_FMT.format(new Date())
							+ "There are no data");
				}
				sdTmp.clearData();
			} else {
				sdQueue[currentQueue].clearData();
				currentQueue = (currentQueue + 1) % 2;
			}

		}

	}

	/**
	 * ��������Browsing�֮�����������뵽���ݿ��� ע��Browsing���ֱ��ͨ��DBUtil�������ݿ��е�
	 * 
	 * @param kind
	 *            ���ࣺ�ֻ�״̬���߻״̬
	 * @param flag
	 *            ��Ϊ0����Ϊ�ֻ���Ϣ����Ϊ�㣬��Ϊ�����
	 */
	public void insertIntoDB(String kind, int flag) {
		// TODO Auto-generated method stub
		if (activityKindChanged(kind) == 1) {// �����ݿ��е����һ����¼��ȣ�������仯

			if (kind.equals(activities[0]) || kind.equals(activities[1])) {// ���������Ϊ��ֹ���ߵ��ݣ������ݲ���
				ActivityInDb data = new ActivityInDb();
				data.kind = kind;
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss");
				Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
				String time = formatter.format(curDate);
				data.start_time = time;
				data.end_time = "---";
				data.strideCount = flag;
				HealthyApplication.mDbUtil.updateActivityEndTime(time);
				HealthyApplication.mDbUtil.insertIntoActivity(data);

			} else {// ����Ǹı�Ϊ�������������ж�

				if (!allowActivityRecog) {// ��ǰ�ֻ�����browsing״̬����tempData������ֱ�Ӳ������ݿ�
					if (tempSaved) {
						tempSaved = false;//����ѻ���Ļ����
					}
				} else {// ��ǰ�ֻ����ڷ�browsing״̬

					if (tempSaved) {// ���ж��Ƿ񱣴����ϴν������������ˣ�������ж�
						if (kind.equals(tempData.kind)) {// �жϸôν���뱣����ϴν���Ƿ���ͬ������ͬ��������ϴ����ݵĲ�����Ȼ��������ݿ�
							tempData.strideCount += flag;
							HealthyApplication.mDbUtil
									.updateActivityEndTime(tempData.start_time);
							HealthyApplication.mDbUtil
									.insertIntoActivity(tempData);
							tempSaved = false;
						} else {// ����ν�����ϴν����ͬ��������εĽ���滻�ϴεĽ��
							tempData.kind = kind;
							SimpleDateFormat formatter = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
							String time = formatter.format(curDate);
							tempData.start_time = time;
							tempData.end_time = "---";
							tempData.strideCount = flag;
							tempSaved = true;
						}

					} else {// ���û���棬����б���
						tempData.kind = kind;
						SimpleDateFormat formatter = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
						String time = formatter.format(curDate);
						tempData.start_time = time;
						tempData.end_time = "---";
						tempData.strideCount = flag;
						tempSaved = true;
					}
				}
			}
		} else if (activityKindChanged(kind) == 0) {// �û�з����仯��ֱ�Ӹ�ϸ���ݿ����һ������
			updateStrides(flag);
		} else {
			ActivityInDb data = new ActivityInDb();
			data.kind = kind;
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
			String time = formatter.format(curDate);
			data.start_time = time;
			data.end_time = "---";
			data.strideCount = flag;
			HealthyApplication.mDbUtil.insertIntoActivity(data);
		}
	}

	/**
	 * ����,���²�������flagΪ�㣬���ø���
	 * 
	 * @param flag
	 */
	private static void updateStrides(int flag) {
		// TODO Auto-generated method stub
		if (flag == 0) {
			return;
		}
		int strides = HealthyApplication.mDbUtil.getLastActivity().strideCount
				+ flag;
		HealthyApplication.mDbUtil.updateStride(strides);
	}

	/**
	 * 
	 * @param kind
	 * @return -1�������¼Ϊ�� 0������û�иı� 1�����෢���ı�
	 */
	private static int activityKindChanged(String kind) {
		// TODO Auto-generated method stub
		ActivityInDb lastActivity = HealthyApplication.mDbUtil
				.getLastActivity();
		if (lastActivity == null) {
			return -1;
		}
		if (lastActivity.kind.equals(kind)) {
			return 0;
		} else {
			return 1;
		}
	}

	public static void exitAPP(Context context) {
		// TODO Auto-generated method stub
		android.os.Process.killProcess(android.os.Process.myPid());
		// �ص�ȫ��activity

	}

	private void acquireWakelock() {
		if (wakeLock == null) {
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
					.getClass().getCanonicalName());
			wakeLock.acquire();
		}
	}

	private void releaseWakelock() {
		if (wakeLock != null && wakeLock.isHeld()) {
			wakeLock.release();
			wakeLock = null;
		}
	}

	/**
	 * �鿴��ǰ��Ļ�Ƿ��ڵ���״̬
	 * */
	private boolean isScreenOn() {
		PowerManager powerManager = (PowerManager) this
				.getSystemService(Context.POWER_SERVICE);
		return powerManager.isScreenOn();
	}
}
