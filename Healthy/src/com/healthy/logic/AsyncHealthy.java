package com.healthy.logic;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.healthy.ui.friends.FriendsHelper;
import com.healthy.ui.friends.FriendsRequestParam;
import com.healthy.ui.friends.FriendsResponseBean;

/**
 * ִ���첽������� ʵ��ConnectionListener�ӿڣ��Ա������쳣�Ͽ�ʱ��������
 * 
 * @author knlnzhao
 * 
 * */
public class AsyncHealthy {

	private Executor mPool;

	private FriendsHelper mFriendsHelper = new FriendsHelper();
	private static AsyncHealthy mInstance;

	private AsyncHealthy() {
		mPool = Executors.newFixedThreadPool(5);
	}

	public synchronized static AsyncHealthy getInstance() {
		if (mInstance == null)
			mInstance = new AsyncHealthy();
		return mInstance;
	}

	/**
	 * ʵ���û���¼
	 * */
	public void login(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansynLogin(mPool, param, listener);
	}

	/**
	 * ʵ���û�ע��
	 * */
	public void register(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansynRegister(mPool, param, listener);
	}

	/**
	 * ע���û���¼
	 * */
	public void logout(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansynLogout(mPool, param, listener);
	}
	
	/**
	 * ʵ���û�ͷ���ϴ�
	 */
	public void uploadAvatar(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansynUploadAvatar(mPool, param, listener);
	}
	
	/**
	 * �����û�ͷ��
	 */
	public void downloadAvatar(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansynDownloadAvatar(mPool, param, listener);
	}

	/**
	 * ���ݵ��µ����������������ȡ��������
	 * */
	public void getFriendsByCalories(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansynGetFriendsByCalories(mPool, param, listener);
	}

	/**
	 * ��ȡ��������
	 * */
	public void getPersonsNearBy(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener) {
		mFriendsHelper.ansyGetPersonsNearby(mPool, param, listener);
	}

	/**
	 * ���ݹؼ��ֵõ�������Ϣ
	 */
	public  void getPersonsByKeyWord(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansyGetPersonsByKeyWord(mPool, param, listener);
	}
	
	/**
	 * ��Ӻ�����Ϣ
	 */
	public void addFriendsRequest(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansyAddFriendsRequest(mPool, param, listener);
	}
	
	/**
	 * ������Ӻ�������
	 */
	public void acceptFriendsRequest(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansyAcceptFriendsRequest(mPool, param, listener);
	}
	
	/**
	 * �ܾ���Ӻ�������
	 */
	public void refuseFriendsRequest(FriendsRequestParam param,
			RequestListener<FriendsResponseBean> listener){
		mFriendsHelper.ansyRefuseFriendsRequest(mPool, param, listener);
	}
	
}
