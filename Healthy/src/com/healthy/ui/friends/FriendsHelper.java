package com.healthy.ui.friends;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.Executor;

import org.jivesoftware.smack.XMPPException;

import android.util.Log;

import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import com.healthy.util.HealthyException;
import com.healthy.util.HealthyUtil;

import static com.healthy.ui.friends.FriendsRequestParam.*;
import static com.healthy.ui.friends.FriendsResponseBean.*;

/**
 * �����û�ע�ᣬ��¼��ע�����˻�������
 * 
 * @author knlnzhao
 * */
public class FriendsHelper {

	/**
	 * �û���¼
	 * */
	public void ansynLogin(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}

	/**
	 * �û��ǳ�
	 * */
	public void ansynLogout(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}

	/**
	 * �û�ע��
	 * */
	public void ansynRegister(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * �û�ͷ���ϴ�
	 * @author zc
	 */
	public void ansynUploadAvatar(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * �û�ͷ������
	 * @author zc
	 */
	public void ansynDownloadAvatar(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	

	/**
	 * �첽��ȡ�û�״̬�б�
	 * */
	public void ansynGetFriendsByCalories(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}

	/**
	 * ���Ҹ�������
	 * */
	public void ansyGetPersonsNearby(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * ���ݹؼ��ֲ����û�
	 * 
	 */
	public void ansyGetPersonsByKeyWord(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * ��Ӻ���
	 * @return
	 */
	public void ansyAddFriendsRequest(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * ������Ӻ�������
	 * @return
	 */
	public void ansyAcceptFriendsRequest(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}
	
	/**
	 * �ܾ���Ӻ�������
	 * @return
	 */
	public void ansyRefuseFriendsRequest(Executor pool,
			final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener){
		ansynExecute(pool, param, listener);
	}

	public String getLoginedUser() {
		return HealthyUtil.getInstance().getLoginedUser();
	}

	/**
	 * �첽ִ��
	 * */
	private void ansynExecute(Executor pool, final FriendsRequestParam param,
			final RequestListener<FriendsResponseBean> listener) {
		pool.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				listener.onStart();
				FriendsResponseBean bean = execute(param);
				listener.onComplete(bean);
			}

		});
	}

	/**
	 * ִ������
	 * */
	public FriendsResponseBean execute(FriendsRequestParam param) {
		Map<String, Object> map = param.getParams();
		FriendsResponseBean bean = new FriendsResponseBean();
		try {
			switch (param.getTaskCategory()) {
			case TASK_LOGIN:// ִ�е�¼����
				String name = (String) map.get("name");
				String password = (String) map.get("password");
				HealthyUtil.getInstance().login(name, password);
				bean.setResult(SUCCESS);
				bean.setInfo("��¼�ɹ�");
				break;
			case TASK_REGISTER:// ִ��ע�����
				name = (String) map.get("name");
				password = (String) map.get("password");
				HealthyUtil.getInstance().register(name, password);
				bean.setResult(SUCCESS);
				bean.setInfo("ע��ɹ�");
				break;
			case TASK_LOGOUT:// ִ�еǳ�����
				HealthyUtil.getInstance().logout();
				bean.setResult(SUCCESS);
				bean.setInfo("�ǳ��ɹ�");
				break;
			case TASK_GET_FRIENDS_BY_CALORIES:// ��ȡ�û�����
				int p = (Integer)map.get("p");
				int psize = (Integer)map.get("psize");
				float calories = (Float)map.get("calories");
				HealthyApplication.mRanking = HealthyUtil.getInstance().getFriendsByCalories(p, psize, calories);
				bean.setResult(SUCCESS);
				bean.setInfo("�������");
				break;
			case TASK_GET_PERSONS_NEARBY:// ���Ҹ�������
				int longitude = (Integer)map.get("longitude");
				int latitude = (Integer)map.get("latitude");
				String info = HealthyUtil.getInstance().getPersonsNearby(longitude, latitude, 3000, 0, 20);
				Log.i("tag", info);
				bean.setInfo(info);//�������������ɸĶ�
				bean.setResult(SUCCESS);
				break;
			case TASK_UPLOAD_AVATAR://�ϴ�ѡ��ͷ��
				InputStream avatarStream = (InputStream)map.get("avatar");
				try {
					HealthyUtil.getInstance().uploadUserAvatra(avatarStream);
				} catch (XMPPException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				bean.setResult(SUCCESS);
				bean.setInfo("�ϴ��ɹ�");
				break;
			case TASK_DOWNLOAD_AVATAR://�����û�ͷ��
				name = (String)map.get("username");
				try {
					HealthyApplication.mapAvatar = HealthyUtil.getInstance().getUserAvatar(name);
				} catch (XMPPException e) {
					e.printStackTrace();
				}
				bean.setResult(SUCCESS);
				bean.setInfo("���سɹ�");
				break;
			case TASK_GET_FRIENDS_BY_KEYWORD://�ؼ��ֲ���
				String keyword = (String)map.get("keyword");
				HealthyApplication.keyResult = HealthyUtil.getInstance().searchUser(keyword);
				bean.setResult(SUCCESS);
				bean.setInfo("�������");
				break;
			case TASK_ADD_FRIENDS://��Ӻ���
				String username = (String)map.get("username");
				HealthyUtil.getInstance().addFriend(username, "");
				bean.setResult(SUCCESS);
				bean.setInfo("���������ѷ���");
				break;
			case TASK_ACCEPT_FRIENDS:
				String acceptname = (String)map.get("username");
				HealthyUtil.getInstance().acceptFriendRequest(acceptname);
				bean.setResult(SUCCESS);
				bean.setInfo("���ܺ�������");
				break;
			case TASK_REFUSE_FRIENDS:
				String refusename = (String)map.get("username");
				HealthyUtil.getInstance().rejectFriendRequest(refusename);
				bean.setResult(SUCCESS);
				bean.setInfo("�ܾ���������");
				break;
			}
			
		} catch (HealthyException e) {
			bean.setResult(ERROR);
			bean.setInfo(e.getMessage());
		}
		return bean;
	}

}
