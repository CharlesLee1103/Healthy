package com.healthy.ui.friends;

import java.util.HashMap;
import java.util.Map;

import com.healthy.logic.RequestParam;

public class FriendsRequestParam extends RequestParam{
	
	public static final int TASK_REGISTER=0;//ִ��ע������
	public static final int TASK_LOGIN=1;//ִ�е�¼����
	public static final int TASK_LOGOUT=2;//�û��ǳ�
	public static final int TASK_GET_FRIENDS_BY_CALORIES=3;//��ȡ��������
	public static final int TASK_GET_PERSONS_NEARBY=4;//�鿴��Χ����
	public static final int TASK_UPLOAD_AVATAR=5;//�ϴ�ͷ��
	public static final int TASK_DOWNLOAD_AVATAR=6;//����ͷ��
	public static final int TASK_GET_FRIENDS_BY_KEYWORD=7;//ͨ���ؼ��ֻ�ȡ����
	public static final int TASK_ADD_FRIENDS=8;//������Ӻ�������
	public static final int TASK_ACCEPT_FRIENDS=9;//���ܺ�������
	public static final int TASK_REFUSE_FRIENDS=10;//�ܾ���������
	
	private Map<String, Object> mMap=new HashMap<String,Object>();
	private int mTaskCategory=-1;//ִ�����������
	
	public FriendsRequestParam(){}
	
	public FriendsRequestParam(int taskCategory){
		mTaskCategory=taskCategory;
	}
			
	@Override
	public Map<String, Object> getParams() {
		// TODO Auto-generated method stub
		return mMap;
	}
	
	/**
	 * ��Ӳ���
	 * @param key ����key
	 * @param value ����ֵ
	 * */
	public void addParam(String key, Object value){
		mMap.put(key,value);
	}
	
	/**
	 * ������������
	 * @param taskCategory �������� ��¼����ע��
	 * */
	public void setTaskCategory(int taskCategory){
		mTaskCategory=taskCategory;
	}
	
	public int getTaskCategory(){
		return mTaskCategory;
	}

}
