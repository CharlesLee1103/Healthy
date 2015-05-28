package com.healthy.db;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.healthy.logic.model.FoodInDb;
import com.healthy.ui.foods.FoodXMLHandler;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private Context mContext;
	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		// ��������û����Ϣ
		// ���ݿ��е�����1 2013-5-27 11:36:16 2013-5-27 11:36:26 escalator 0
		String sql = "create table if not exists activity_info"
				+ "(_id integer PRIMARY KEY AUTOINCREMENT,start_time vchar(30),end_time vchar(30),"
				+ "kind vchar(20), strides integer)";
		db.execSQL(sql);

		
		//����,����û��켣��Ϣ
		sql="create table if not exists tracker_info"+"(_id integer PRIMARY KEY AUTOINCREMENT, user_id vchar(30),activity_type vchar(10),start_address vchar(40),tracker_distance vchar(20), start_time vchar(20), end_time vchar(20))";
		db.execSQL(sql);
		//����,����û�λ����Ϣ
		sql="create table if not exists location_info"+"(_id integer , longitude double, latitude double, time vchar(30))";
		db.execSQL(sql);
		
		
		//�������ʳ������
		sql = "create table if not exists food_type"+"(_id integer PRIMARY KEY AUTOINCREMENT,name vchar(30),calorie float,iscommon integer)";
		db.execSQL(sql);
		// �������ʳ����Ϣ
		// date ��ʳ��¼��ӵľ�������
		// time ��ʳ��¼��ӵľ���ʱ�䣬��ȷ������
		sql = "create table if not exists food_info"+"(_id integer PRIMARY KEY AUTOINCREMENT,name vchar(30),num integer,calorie float,date vchar(30),time vchar(30))";
		db.execSQL(sql);
		//���������ʳ�ƻ�
		sql = "create table if not exists food_plan"+"(_id integer PRIMARY KEY , start_time vchar(30),duration integer,daycalorie float)";
		db.execSQL(sql);
		
		//����xml�ļ���ʼ��ʳ�����ݿ�
		List<FoodInDb> mFoodList=null;
		SAXParserFactory mFactory = SAXParserFactory.newInstance();
		SAXParser mParser;
		try {
			mParser = mFactory.newSAXParser();
			XMLReader mReader = mParser.getXMLReader();
			FoodXMLHandler foodHandler = new FoodXMLHandler();
			mReader.setContentHandler(foodHandler);
			mReader.parse(new InputSource(mContext.getAssets().open("normalfood.xml")));
			mFoodList = foodHandler.getFoodList();
		} catch (ParserConfigurationException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		for(FoodInDb curFoodData:mFoodList){
			sql = "insert into food_type(name,calorie,iscommon) values('"+curFoodData.name+"',"+curFoodData.calorie+",1)";
			db.execSQL(sql);
			Log.i("food", curFoodData.name);
		}
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
