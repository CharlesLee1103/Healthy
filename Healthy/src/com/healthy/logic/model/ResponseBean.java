package com.healthy.logic.model;

/**
 * ��Ӧ���ģ��
 * */
public class ResponseBean {
	
	public static final int ERROR = 0;// ʧ��
	public static final int SUCCESS = 1;// �ɹ�

	private int mResult = -1;// �������
	private String mInfo = "���������Ϣ";// �����Ϣ��ʾ

	public int getResult() {
		return mResult;
	}

	public void setResult(int result) {
		mResult = result;
	}

	public String getInfo() {
		return mInfo;
	}

	public void setInfo(String info) {
		mInfo = info;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return mInfo;
	}
}
