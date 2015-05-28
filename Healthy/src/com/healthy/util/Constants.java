package com.healthy.util;

/**
 * ������
 * */
public class Constants {

	public static class FoodConstants {

		public final static String NAME = "name";
		public final static String NUM = "num";
		public final static String CALORIE = "calorie";
		public final static String TIME = "time";
		public final static String DATE="date";

	}
	
	public static class ActivityRequestCode
	{
		public final static int FOODADD=2;
		public final static int FOODPLAN=3;
		public final static int LOGIN=4;
		public final static int IMAGE=5;
		public final static int CAMERA=6;
		public final static int CANCEL=7;
	}
	public static class ActivityResultCode{
		public final static int ERROR=0;
		public final static int SUCCESS=1;
	}
	
	/**
	 * �����������ʱ����ʹ�õ��������
	 * */
	public static class RequestCode{
		public static int GET_FRENDS_BY_CALORIES = 0;// ����������Ϣ����ȡ��������
		public static int GET_PERSONS_NEARBY=1;// ���ҵĸ�������
	}
	
}
