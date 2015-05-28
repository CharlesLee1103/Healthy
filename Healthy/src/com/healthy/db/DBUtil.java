package com.healthy.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.healthy.logic.model.ActivityInDb;
import com.healthy.logic.model.FoodInDb;
import com.healthy.logic.model.LocationInDb;
import com.healthy.logic.model.TrackerListBean;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static com.healthy.util.Constants.FoodConstants.*;

/** �����ݿ���صĹ����� */
public class DBUtil {
	private Context mContext;
	private DBHelper dbHelper;
	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	/** ����Healthy���ݿ� */
	public DBUtil(Context context) {
		mContext = context;
		dbHelper = new DBHelper(context, "healthy.db", null, 1);
	}

	/**
	 * �ر����ݿ�
	 */
	public void closeDb() {
		dbHelper.close();
	}

	/**
	 * ���activity_info�в���һ������
	 * 
	 * @param data
	 */
	public void insertIntoActivity(ActivityInDb data) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("start_time", data.start_time);
		cv.put("end_time", data.end_time);
		cv.put("kind", data.kind);
		cv.put("strides", data.strideCount);
		System.out.println("Insert Into activity_info row ID--->"
				+ db.insert("activity_info", null, cv) + "-----" + data.kind);
		closeDb();
	}

	/**
	 * ������һ��ActivityData�������¼Ϊ�գ�����null;
	 * 
	 * @return
	 */
	public ActivityInDb getLastActivity() {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("activity_info", null, null, null, null, null,
				null);
		if (cursor.getCount() == 0) {
			cursor.close();
			closeDb();
			return null;
		} else {
			cursor.moveToLast();
			ActivityInDb adata = new ActivityInDb();
			adata.start_time = cursor.getString(1);
			adata.end_time = cursor.getString(1);
			adata.kind = cursor.getString(3);
			adata.strideCount = cursor.getInt(4);
			cursor.close();
			closeDb();
			return adata;
		}
	}

	public void updateStride(int stride) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("activity_info", null, null, null, null, null,
				null);
		cursor.moveToLast();
		int id = cursor.getInt(0);
		cursor.close();
		String sql = "update activity_info set strides = " + stride
				+ " where _id=" + id;
		db.execSQL(sql);
		closeDb();
		System.out.println("Update row ID--->" + id);
	}

	/**
	 * ���»��¼�Ľ���ʱ��
	 * */
	public void updateActivityEndTime(String endTime) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = db.query("activity_info", null, null, null, null, null,
				null);
		if (cursor.getCount() == 0) {
			cursor.close();
			closeDb();
			return;
		}
		cursor.moveToLast();
		if (cursor.getString(3).equalsIgnoreCase("Browsing"))// Browsing
																// �Ľ���ʱ����ֱ��д��ģ����������и���
			return;
		int id = cursor.getInt(0);
		cursor.close();
		String sql = "update activity_info set end_time = '" + endTime
				+ "' where _id=" + id;
		db.execSQL(sql);
		closeDb();
	}

	/**
	 * ����Ϊ��λ����Date������ÿ����ʱ�䡢��·�����ĵ���Ϣ
	 * 
	 * @param day
	 * @return HashMap<String, HashMao<String,Object>>
	 */
	public HashMap<String, HashMap<String, Object>> getDailyActivityData(
			String date) {
		String strSql = "select * from activity_info where strftime('%Y-%m-%d',start_time)=strftime('%Y-%m-%d',?);";// ��ѯ����ļ�¼
		return getActivityCalorieDataInATime(strSql, date);
	}

	/**
	 * ��ȡdate�����ڣ����ֻ��ͳ����Ϣ
	 * 
	 * @param date
	 *            �涨�Ĳ�ѯʱ��
	 * */
	public HashMap<String, HashMap<String, Object>> getMonthActivityData(
			String date) {
		String strSql = "select * from activity_info where strftime('%Y-%m',start_time) = strftime('%Y-%m',?) ";// ��ѯdate���¼�¼
		return getActivityCalorieDataInATime(strSql, date);
	}

	private HashMap<String, HashMap<String, Object>> getActivityCalorieDataInATime(
			String strSql, String date) {

		HashMap<String, HashMap<String, Object>> measurements = null;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(strSql, new String[] { date });
		if (cursor.getCount() - 1 > 0) {// �������һ����¼û�н���ʱ�䣬�����û���俼������
			measurements = new HashMap<String, HashMap<String, Object>>();
			cursor.moveToFirst();
			HashMap<String, Object> measurement;
			String activity, startTime, endTime;
			float duration = 0;
			int strides = 0;

			// ͳ�Ƹ�����ĳ���ʱ���ĳЩ��Ĳ���
			for (int i = 0; i < cursor.getCount() - 1; i++) {
				activity = cursor.getString(3);// ��ȡ�����
				// ��¼�û�ĳ���ʱ��
				startTime = cursor.getString(1);
				endTime = cursor.getString(2);
				if (!measurements.containsKey(activity)) {// ��ǰ���в�������acitivity
					measurement = new HashMap<String, Object>();
					duration = getDuration(startTime, endTime);
					if (duration == -1)// ʱ���ȡʧ��
						return null;
					measurement.put("duration", duration);

					// ��¼�û�Ĳ���
					strides = cursor.getInt(4);
					measurement.put("strides", strides);
					measurements.put(activity, measurement);

				} else {// ��ǰ���а�����activity,���³���ʱ���Լ�����ͳ��
					measurement = measurements.get(activity);// ��ȡ���Ӧ��activityͳ����Ϣ
					// ���²�������ʱ��
					duration = (Float) measurement.remove("duration")
							+ getDuration(startTime, endTime);
					measurement.put("duration", duration);
					// ���²���
					strides = (Integer) measurement.remove("strides")
							+ cursor.getInt(4);
					measurement.put("strides", strides);
					measurements.put(activity, measurement);
				}
				cursor.moveToNext();
			}

			// ��������ͳ�Ƴ���ԭʼ���ݣ�����distance,speed,calories burned
			SharedPreferences prefs_personal = mContext.getSharedPreferences(
					"personal_info", Context.MODE_PRIVATE);
			float weight = prefs_personal.getFloat("weight", 70); // Ĭ������Ϊ70kg
			float stride_length = prefs_personal.getFloat("stride", 1.2f) / 100;// ���˸������ȴ�ԼΪ1.2m
			float stair_length = prefs_personal.getFloat("stair_length", 0.2f);// ����¥�ݵĸ߶ȣ�Ĭ��Ϊ0.2m
			float bicycle_length = prefs_personal.getFloat("bicycle_length",
					0.7f);// ���г�����ֱ����Ĭ��Ϊ2m
			Iterator<Entry<String, HashMap<String, Object>>> iterator = measurements
					.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, HashMap<String, Object>> entry = iterator
						.next();
				activity = entry.getKey();
				measurement = entry.getValue();
				duration = (Float) measurement.get("duration");
				float distance = 0;
				float speed = 0;
				float mets = 0;
				if (activity.equalsIgnoreCase("stationary")
						|| activity.equalsIgnoreCase("lift")
						|| activity.equalsIgnoreCase("escalator")
						|| activity.equalsIgnoreCase("browsing")) {// �����ڵĻ
					distance = 0;
					speed = 0;
					mets = 1.5f;
				} else if (activity.equalsIgnoreCase("walking")) {
					distance = (Integer) measurement.get("strides")
							* stride_length;
					speed = distance / duration;
					mets = 0.0272f * speed * 60 + 1.2f;
				} else if (activity.equalsIgnoreCase("jogging")) {
					distance = (Integer) measurement.get("strides")
							* stride_length;
					speed = distance / duration;
					mets = 0.093f * speed * 60 - 4.7f;
				} else if (activity.equalsIgnoreCase("ascendingStairs")) {
					distance = (Integer) measurement.get("strides")
							* stair_length;
					speed = distance / duration;
					mets = 8.0f;
				} else if (activity.equalsIgnoreCase("descendingStairs")) {
					distance = (Integer) measurement.get("strides")
							* stair_length;
					speed = distance / duration;
					mets = 3.0f;
				} else if (activity.equalsIgnoreCase("bicycling")) {
					distance = (Integer) measurement.get("strides")
							* bicycle_length;
					speed = distance / duration;
					mets = 5.5f;
				}
				measurement.put("distance", distance);
				measurement.put("speed", speed);
				measurement.put(
						"calories_burned",
						getCaloriesBurned(mets, duration / 60.0f / 60.0f,
								weight));
			}
		}
		cursor.close();
		closeDb();
		return measurements;
	}

	/**
	 * ������������
	 * 
	 * @param mets
	 *            METSֵ
	 * @param duration
	 *            �����ʱ�䣬��λΪh
	 * @param weight
	 *            �������� ��λΪkg
	 * @return �������� ��λΪkcal
	 * */
	private float getCaloriesBurned(float mets, float duration, float weight) {
		return (float) (1.05 * mets * duration * weight);
	}

	/**
	 * ��������ʱ��ڵ�֮���ʱ�䳤��,ʱ���ַ�����ʽΪ"yyyy-MM-dd HH:mm:ss" ��λΪs
	 * 
	 * @param start
	 *            ��ʼʱ��
	 * @param end
	 *            ����ʱ��
	 * */
	private float getDuration(String start, String end) {
		try {
			Date dStart = FORMATTER.parse(start);
			Date dEnd = FORMATTER.parse(end);
			float diff = (dEnd.getTime() - dStart.getTime()) / 1000;
			return diff;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("com.healty", "getActivityData", e);
			return -1;
		}
	}

	// =========================�켣����������ݿ����==========================//

	/**
	 * ��tracker_info���в���һ������
	 */
	public void insertNullData() {
		int id = getLastTrackerID() + 1;
		Log.i("tag", "tracker��id:" + id + "");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String date = formatter.format(curDate);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("_id", id);
		cv.put("start_time", date);
		db.insert("tracker_info", null, cv);
		closeDb();
	}

	/**
	 * �õ�tracker_info���е�ǰ���һ�����ݵ�id���
	 * 
	 * @return
	 */
	public int getLastTrackerID() {
		int i = 0;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("tracker_info", null, null, null, null, null,
				null);
		if (cursor.moveToFirst() == false) {
			cursor.close();
			closeDb();
			return i;
		} else {
			cursor.moveToLast();
			i = cursor.getInt(0);// ��һ�У�����Ϊ0
			cursor.close();
			closeDb();
			return i;
		}
	}

	/**
	 * ��location_info���в���һ������
	 */
	public void insertIntoLocation(LocationInDb mLocation) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("_id", mLocation.getId());
		cv.put("longitude", mLocation.getLongitude());
		cv.put("latitude", mLocation.getLatitude());
		cv.put("time", mLocation.getTime());
		db.insert("location_info", null, cv);
		closeDb();
	}

	/**
	 * ��ǰ���¼�¼��Ӧid�Ƿ��ж�Ӧλ����Ϣ
	 */
	public boolean IsStartLocation() {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id =" + id;
		Cursor cursor = db.query("location_info", null, where, null, null,
				null, null);
		if (!cursor.moveToFirst()) {
			cursor.close();
			closeDb();
			return false;
		} else {
			cursor.close();
			closeDb();
			return true;
		}
	}

	/**
	 * ɾ����ǰ���²���tracker_info��id������
	 */
	public void deleteTracker() {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id = " + id;
		db.delete("tracker_info", where, null);
		closeDb();
	}
	
	/**
	 * ɾ��ָ��tracker_info����id�����ݣ�����ɾ��Location_info���ж�Ӧid��λ������
	 */
	public void deleteTrackerById(int id){
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id = " + id;
		Log.i("tag", "dbutil-----"+id);
		db.delete("tracker_info", where, null);
		db.delete("location_info", where, null);
		int lastId = getLastTrackerID();
		Log.i("tag", "���һ����¼�ѱ��----->" + lastId);
		closeDb();
	}

	/**
	 * ȡ��location_info������id�ľ�γ����Ϣ���浽List��
	 */
	public List<GeoPoint> getFromLocation() {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id =" + id;
		String order = "time";
		Cursor cursor = db.query("location_info", null, where, null, null,
				null, order);
		List<GeoPoint> pointList = new ArrayList<GeoPoint>();
		if (cursor.moveToFirst())// �Ѿ��Ƶ���һ��������
		{

			do {
				int longitude = (int) (cursor.getDouble(1) * 1E6);
				int latitude = (int) (cursor.getDouble(2) * 1E6);
				GeoPoint temp = new GeoPoint(longitude, latitude);
				pointList.add(temp);
			} while (cursor.moveToNext());
		}
		cursor.close();
		closeDb();
		return pointList;
	}

	/**
	 * ����id�ţ���ѯlocation_info��Ϣ����ʱ������
	 */
	public List<GeoPoint> queryLocationById(int id) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id =" + id;
		String order = "time";
		Cursor cursor = db.query("location_info", null, where, null, null,
				null, order);
		List<GeoPoint> pointList = new ArrayList<GeoPoint>();
		if (cursor.moveToFirst())// �Ѿ��Ƶ���һ��������
		{

			do {
				int longitude = (int) (cursor.getDouble(1) * 1E6);
				int latitude = (int) (cursor.getDouble(2) * 1E6);
				GeoPoint temp = new GeoPoint(longitude, latitude);
				pointList.add(temp);
			} while (cursor.moveToNext());
		}else {
			pointList = null;
		}
		cursor.close();
		closeDb();
		return pointList;
	}

	/**
	 * �������õ���distance���뵽��Ӧ��ŵ�tracker_info����
	 */
	public void updateTrackerDistance(String distance) {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("tracker_distance", distance);
		String whereto = "_id = " + id;
		db.update("tracker_info", cv, whereto, null);
		closeDb();
	}

	/**
	 * ���µ�ǰ����һ��tracker_info���ݵ�start_address�ֶ� ���Ĳ������ݿ⣬adb��ѯΪ���룬��Ӱ�������
	 */
	public void updateTrackerAdr(String adr) {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("start_address", adr);
		String where = "_id = " + id;
		db.update("tracker_info", cv, where, null);
		closeDb();
	}

	/**
	 * ����tracker_info��end_time�ֶ�
	 */
	public void updateTrackerEndTime(String endtime){
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("end_time", endtime);
		String where = "_id =" + id;
		db.update("tracker_info", cv, where, null);
		closeDb();
	}
	/**
	 * �ж�һ�ι켣���ٻ���ͣ�ȡʱ���ڳ���ʱ����ģ�����tracker_info��tracker_type�ֶ�
	 */
	public void getTrackerType() {
		int id = getLastTrackerID();
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "_id =" + id;
		Cursor cursor = db.query("tracker_info", null, where, null, null,
				null, null);
		String type = null, activity = null, start, end, starttime = null, endtime = null;
		cursor.moveToFirst();
		starttime = cursor.getString(5);
		endtime = cursor.getString(6);
		cursor.close();
		String strSql = "SELECT * FROM activity_info WHERE start_time > ? and start_time < ? ORDER BY start_time";
		Cursor cursorto = db.rawQuery(strSql,
				new String[] { starttime, endtime });
		if (cursorto.moveToFirst())// �Ѿ��Ƶ���һ��������
		{

			do {
				Log.i("type",
						cursorto.getString(1) + "," + cursorto.getString(2)
								+ "," + cursorto.getString(3));
			} while (cursorto.moveToNext());
		}
		float duration;
		HashMap<String, Float> measurement = new HashMap<String, Float>();
		if (!cursorto.moveToFirst()) {// ����Ӧʱ���û�л��¼

			type = "stationary";
		} else if (cursorto.getCount() == 1) {
			cursorto.moveToFirst();
			type = cursorto.getString(3);
		} else if (cursorto.getCount() > 1) {
			cursorto.moveToFirst();// ���ƶ�����һ����¼��
			// ͳ��ʱ����ڸ��ֻ�ĳ���ʱ��
			for (int i = 0; i < cursorto.getCount() - 1; i++) {
				activity = cursorto.getString(3);
				start = cursorto.getString(1);
				end = cursorto.getString(2);
				if (!measurement.containsKey(activity))// ��ǰ���в������˻���࣬д��
				{
					duration = getDuration(start, end);
					if (duration == -1) {
						Log.i("tag", "��ȡ����ʱ��ʧ�ܣ�+getTrackerType");
					}
					measurement.put(activity, duration);
				} else// ��ǰ�����Դ洢�˻�����������ʱ��
				{
					duration = measurement.get(activity)
							+ getDuration(start, end);
					measurement.put(activity, duration);
				}
				cursorto.moveToNext();
			}
			ByValueComparator bvc = new ByValueComparator(measurement);
			List<String> keys = new ArrayList<String>(measurement.keySet());// �õ�HashMap�����е�key
			Collections.sort(keys, bvc);
			for (String key : keys) {
				int i = 0;
				if (i == 0) {
					type = key;
					System.out.println("�Ƚ�����������YES");
				}
				i++;
				break;
			}
		}
		System.out.println("����ʶ��Ľ��type:" + type);
		ContentValues cv = new ContentValues();
		cv.put("activity_type", type);
		String whereto = "_id = " + id;
		db.update("tracker_info", cv, whereto, null);
		cursorto.close();
		closeDb();

	}
	/**
	 * ��tracker_info��ȡ����Ӧ�ֶ����ݣ����µ�trackerList
	 * 
	 */
	public List<TrackerListBean> getTrackerToList() {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		List<TrackerListBean> listData = new ArrayList<TrackerListBean>();
		Cursor cursor = db.query("tracker_info", null, null, null, null, null,
				"_id");
		cursor.moveToLast();
		if (cursor.moveToLast()) {
			do {
				TrackerListBean trackerData = new TrackerListBean();
				trackerData.setId(cursor.getInt(0));
				trackerData.setType(changeTypeToReadable(cursor.getString(2)));
				trackerData.setLocation(cursor.getString(3));
				trackerData.setDistance(cursor.getString(4));
				trackerData.setTime(changeTimeLayout(cursor.getString(5)));
				listData.add(trackerData);
			} while (cursor.moveToPrevious());
		}
		return listData;
	}

	/**
	 * ��tracker_info���е�ʱ���ʽת��ΪtrackerList��ʾ�ĸ�ʽ
	 */
	private String changeTimeLayout(String time) {
		String showTime = null;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = null;
		try {
			date = formatter.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		showTime = formatter.format(date);
		return showTime;
	}

	/**
	 * ��tracker_info���е�����Ӣ��ת��Ϊ��������
	 */
	public String changeTypeToReadable(String type) {
		try {
			Class<?>[] classes = Class.forName("com.healthy.R")
					.getDeclaredClasses();
			for (Class<?> c : classes) {// �Գ�Ա�ڲ�����з���
				if (c.getName().contains("string")) {
					return mContext.getResources().getString(
							c.getField(type).getInt(null));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Healthy", "changeTypeToChinese", e);
		}
		return "error";
	}

	/***
	 * ʵ��Comparator<>�ӿڣ����ڱȽ�hashmap��ֵ����value�Ĵ�С
	 * 
	 * @return 0 1 -1;
	 * 
	 */
	static class ByValueComparator implements Comparator<String> {
		private HashMap<String, Float> datas = new HashMap<String, Float>();

		public ByValueComparator(HashMap<String, Float> datas) {
			this.datas = datas;
		}

		@Override
		public int compare(String lhs, String rhs) {
			if (!datas.containsKey(rhs) || !datas.containsKey(lhs)) {
				return 0;
			}

			if (datas.get(lhs) < datas.get(rhs)) {
				return 1;
			} else if (datas.get(lhs) == datas.get(rhs)) {
				return 0;
			} else {
				return -1;
			}
		}

	}

	// ======================ʳ�����ݿ�������=============================//
	/**
	 * ��food_info�����һ������
	 */
	public void insertIntoFoodInfo(FoodInDb food) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("name", food.name);
		cv.put("num", food.num);
		cv.put("calorie", food.calorie);
		cv.put("time", food.time);
		cv.put("date", food.date);
		db.insert("food_info", null, cv);
		closeDb();
	}

	/**
	 * ��food_type���һ������
	 */
	public void insertIntoFoodType(FoodInDb data) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("name", data.name);
		cv.put("calorie", data.calorie);
		cv.put("iscommon", 0);
		db.insert("food_type", null, cv);
		closeDb();
	}

	/**
	 * ��ѯһ��ʱ���ڵ���ʳ��Ϣ
	 * */
	public List<FoodInDb> queryFoodInATime(String strSql, String date) {
		List<FoodInDb> list = new ArrayList<FoodInDb>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(strSql, new String[] { date });
		FoodInDb food;
		while (cursor.moveToNext()) {
			food = new FoodInDb();
			food.name = cursor.getString(cursor.getColumnIndex(NAME));
			food.num = cursor.getInt(cursor.getColumnIndex(NUM));
			food.calorie = cursor.getInt(cursor.getColumnIndex(CALORIE));
			food.time = cursor.getString(cursor.getColumnIndex(TIME));
			food.date = cursor.getString(cursor.getColumnIndex(DATE));
			list.add(food);
		}
		cursor.close();
		db.close();
		return list;
	}
	
	/**
	 * ��ѯĳ�����ʳ��Ϣ
	 */
	public List<FoodInDb> queryDayFood(String date) {
		String strSql = "select * from food_info where strftime('%Y-%m-%d',date)=strftime('%Y-%m-%d',?);";
		return queryFoodInATime(strSql, date);
	}
	
	/**
	 * ��ѯ����ʳ������
	 * */
	public List<FoodInDb> queryMonthFood(String date) {
		String strSql = "select * from food_info where strftime('%Y-%m',date)=strftime('%Y-%m',?);";
		return queryFoodInATime(strSql, date);
	}

	/**
	 * ɾ��ĳ����ʳ��¼
	 */
	public void deleteFood(FoodInDb food) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		String where = "strftime('%Y-%m-%d',date)=strftime('%Y-%m-%d',?) and " +
				"strftime('%H-%M',time)=strftime('%H-%M',?) " +
				"and name=?;";
		db.delete("food_info", where, new String[] { food.date, food.time, food.name });
		db.close();
	}

	/*
	 * ��ѯ����ʳ��
	 */
	public List<Map<String, Object>> queryFoodType() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map;
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cur = db.query("food_type", new String[] { "*" }, null, null,
				null, null, null);
		while (cur.moveToNext()) {
			map = new HashMap<String, Object>();
			map.put("name", cur.getString(cur.getColumnIndex("name")));
			map.put("calorie", cur.getFloat(cur.getColumnIndex("calorie")));
			list.add(map);
		}
		cur.close();
		db.close();
		return list;
	}

	/**
	 * ������ʳ�ƻ�ʱ��
	 * 
	 * @param time
	 * @param duration
	 */
	public void updateFoodPlan(String time, int duration, float dayCalorie) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		db.delete("food_plan", "", new String[] {});// ɾ��ԭ�мƻ�
		ContentValues cv = new ContentValues();
		cv.put("start_time", time);
		cv.put("duration", duration);
		cv.put("daycalorie", dayCalorie);
		db.insert("food_plan", null, cv);
		closeDb();
	}

	/**
	 * ��ѯ��ʳ�ƻ�
	 */
	public Map<String, Object> queryFoodPlan() {
		Map<String, Object> map = new HashMap<String, Object>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cur = db.query("food_plan", new String[] { "*" }, null, null,
				null, null, null);
		while (cur.moveToNext()) {
			map.put("start_time",
					cur.getString(cur.getColumnIndex("start_time")));
			map.put("duration", cur.getInt(cur.getColumnIndex("duration")));
			map.put("daycalorie",
					cur.getFloat(cur.getColumnIndex("daycalorie")));
		}
		cur.close();
		db.close();
		return map;
	}
}
