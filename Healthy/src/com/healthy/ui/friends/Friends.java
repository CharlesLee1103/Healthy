package com.healthy.ui.friends;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.RequestListener;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;
import com.healthy.util.AsyncImageDownLoader;
import com.healthy.util.Constants.ActivityRequestCode;
import com.healthy.util.HealthyUtil;
import com.healthy.util.Tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

/**
 * Friends����
 * 
 * @author zc
 * */
public class Friends {

	/* ���ѽ��� */
	private View mFriends;
	private Context mContext;
	private OnOpenListener mOnOpenListener;
	private ImageView mFlipMenu;
	private ViewFlipper mFriendsContent;

	/* ��¼���� */
	private EditText mUsernameEdit;
	private EditText mPasswordEdit;
	private Button mLoginBtn;
	private CheckBox mRememberPwd;
	private TextView mRegister;
	private SharedPreferences mUser;

	/* ������ҳ */
	private ImageView mMyPhoto;
	private TextView mMyName;
	private ImageView mAddFriends;
	private ViewFlipper mMyContent;
	private RadioGroup mFriendsGroup;
	private FriendsRanking mFriendsRanking;

	/* ͷ������ */
	private static final String IMAGE_FILE_NAME = "faceImage.jpg";

	private String[] items = { "ѡ�񱾵�ͼƬ", "������Ƭ" };// �Ի���ѡ��

	public Friends(Context context) {

		mContext = context;
		mFriends = LayoutInflater.from(mContext).inflate(R.layout.page_friends,
				null);
		mFlipMenu = (ImageView) mFriends.findViewById(R.id.flip_menu);
		mFriendsContent = (ViewFlipper) mFriends
				.findViewById(R.id.friends_content);
		mFriendsContent.setDisplayedChild(0);

		initLogin();
		initHomepage();
		setListener();
	}

	private void initLogin() {
		mLoginBtn = (Button) mFriends.findViewById(R.id.login);
		mUsernameEdit = (EditText) mFriends
				.findViewById(R.id.friends_username_edit);
		mPasswordEdit = (EditText) mFriends
				.findViewById(R.id.friends_password_edit);
		mRememberPwd = (CheckBox) mFriends
				.findViewById(R.id.friends_remember_pwd);
		mRegister = (TextView) mFriends.findViewById(R.id.friends_register);
		getLoginSharedPreference();
	}

	private void getLoginSharedPreference() {
		SharedPreferences sp = mContext.getSharedPreferences("user_info", 0);
		boolean opt = sp.getBoolean("isChecked", false);
		if (opt) {
			mUsernameEdit.setText(sp.getString("username", ""));
			mPasswordEdit.setText(sp.getString("password", ""));
		}
	}

	private void initHomepage() {
		mMyPhoto = (ImageView) mFriends.findViewById(R.id.friends_myphoto_view);
		mMyName = (TextView) mFriends.findViewById(R.id.friends_username);
		mAddFriends = (ImageView) mFriends.findViewById(R.id.friends_add);
		mMyContent = (ViewFlipper) mFriends
				.findViewById(R.id.friends_content_flipper);
		mFriendsGroup = (RadioGroup) mFriends
				.findViewById(R.id.friends_radiogroup);
	}

	private void setListener() {

		mFlipMenu.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mOnOpenListener != null) {
					mOnOpenListener.open();
				}
			}
		});

		mLoginBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String username = mUsernameEdit.getText().toString().trim();
				String password = mPasswordEdit.getText().toString().trim();
				if (username.equals("") || password.equals("")) {
					Toast.makeText(mContext, "�û��������벻��Ϊ��", Toast.LENGTH_SHORT)
							.show();
				} else {
					mFriendsContent.setDisplayedChild(2);
					FriendsRequestParam param = new FriendsRequestParam(
							FriendsRequestParam.TASK_LOGIN);
					param.addParam("name", username);
					param.addParam("password", password);
					HealthyApplication.mAsyncHealthy.login(param, listener);
				}
			}
		});

		mRegister.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, RegisterActivity.class);
				mContext.startActivity(intent);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});

		mMyPhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialog();
			}
		});

		mAddFriends.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(mContext, FriendsAdd.class);
				((Activity) mContext).startActivity(intent);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});

		mFriendsGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.friends_list_radiobtn:
					mMyContent.setInAnimation(AnimationUtils.loadAnimation(
							mContext, R.anim.slide_in_left));
					mMyContent.setOutAnimation(AnimationUtils.loadAnimation(
							mContext, R.anim.slide_out_right));
					mMyContent.setDisplayedChild(0);
					break;
				case R.id.friends_ranking_radiobtn:
					mMyContent.setInAnimation(AnimationUtils.loadAnimation(
							mContext, R.anim.slide_in_right));
					mMyContent.setOutAnimation(AnimationUtils.loadAnimation(
							mContext, R.anim.slide_out_left));
					mMyContent.setDisplayedChild(1);
					break;
				}
			}
		});

	}

	public View getView() {
		if (HealthyUtil.getInstance().getLoginedUser() == null) {// δ��¼��ִ��һЩ��ʼ������
			mAddFriends.setVisibility(View.GONE);
			mFriendsContent.setInAnimation(null);
			mFriendsContent.setOutAnimation(null);
			mFriendsContent.setDisplayedChild(0);
			HealthyApplication.mRanking = null;
		} else {// ��½��ִ��һЩ��ʼ������
			Log.i("tag", "ִ�е�½��ĳ�ʼ��");
			mFriendsContent.setInAnimation(null);
			mFriendsContent.setOutAnimation(null);
			mFriendsContent.setDisplayedChild(1);
		}
		return mFriends;
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:// ��ʼ������ʾ���ؽ�����

				break;
			case 1:// ������������ؼ��ؽ�������ִ����������߼�
				FriendsResponseBean bean = (FriendsResponseBean) msg.obj;
				if (bean.getResult() == FriendsResponseBean.ERROR) {// ����ִ��ʧ��
					if (mFriendsContent.getDisplayedChild() == 2) {// ��ǰ��ʾ���ڼ���
						mFriendsContent.setDisplayedChild(0);
					}
					Toast.makeText(mContext, bean.toString(),
							Toast.LENGTH_SHORT).show();
				} else {

					if (bean.toString().equalsIgnoreCase("�ϴ��ɹ�")) {
						Toast.makeText(mContext, "�ϴ�ͷ��ɹ���", Toast.LENGTH_SHORT)
								.show();
					} else if (bean.toString().equalsIgnoreCase("���سɹ�")) {
						if (HealthyApplication.mapAvatar == null) {// ������ûͷ����Ϣ
																	// �ϴ�Ĭ��ͷ��
							Bitmap bitmap = BitmapFactory.decodeResource(
									mContext.getResources(),
									R.drawable.friend_ava_empty);
							uploadFriendsAvatar(bitmap, 30);
						} else {
							mMyPhoto.setImageBitmap(AsyncImageDownLoader
									.toRoundBitmap(
											HealthyApplication.mapAvatar,
											mMyPhoto.getLayoutParams().width - 4,
											mMyPhoto.getLayoutParams().height - 4));
						}
					} else if (bean.toString().equalsIgnoreCase("��¼�ɹ�")) {
						SharedPreferences sp = mContext.getSharedPreferences(
								"user_info", 0);
						sp.edit()
								.putString(
										"username",
										mUsernameEdit.getText().toString()
												.trim()).commit();
						sp.edit()
								.putString(
										"password",
										mPasswordEdit.getText().toString()
												.trim()).commit();
						if (mRememberPwd.isChecked()) {
							sp.edit().putBoolean("isChecked", true).commit();
						} else {
							sp.edit().putBoolean("isChecked", false).commit();
						}
						initMyFriends();
						if (mFriendsRanking == null||HealthyApplication.mRanking==null) {//������а�Ϊ�յĻ�����������������˳����ε�¼�����
							mFriendsRanking = new FriendsRanking(mContext);
							ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
									ViewGroup.LayoutParams.FILL_PARENT,
									ViewGroup.LayoutParams.FILL_PARENT);
							mMyContent.addView(mFriendsRanking.getView(),
									params);
						}

						mFriendsContent.setDisplayedChild(1);

					}
				}
				break;
			}
		}

	};

	private void initMyFriends() {
		mAddFriends.setVisibility(View.VISIBLE);
		mUser = mFriends.getContext().getSharedPreferences("user_info", 0);
		mMyName.setText(mUser.getString("username", ""));
		loadFriendsAvatar();
	}

	private void showDialog() {
		new AlertDialog.Builder(mContext)
				.setTitle("����ͷ��")
				.setItems(items, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						switch (which) {
						case 0:
							Intent intentFromGallery = new Intent();
							intentFromGallery.setType("image/*"); // �����ļ�����
							intentFromGallery
									.setAction(Intent.ACTION_GET_CONTENT);
							((Activity) mContext).startActivityForResult(
									intentFromGallery,
									ActivityRequestCode.IMAGE);
							break;
						case 1:

							Intent intentFromCapture = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							// �жϴ洢���Ƿ�����ã����ý��д洢
							if (Tools.hasSdcard()) {

								intentFromCapture.putExtra(
										MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(new File(Environment
												.getExternalStorageDirectory(),
												IMAGE_FILE_NAME)));
							}
							((Activity) mContext).startActivityForResult(
									intentFromCapture,
									ActivityRequestCode.CAMERA);
							break;

						}
					}
				})
				.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	/**
	 * �ü�ͼƬ����ʵ��
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// ���òü�
		intent.putExtra("crop", "true");
		// aspectX aspectY �ǿ�ߵı���
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY �ǲü�ͼƬ���
		intent.putExtra("outputX", 188);
		intent.putExtra("outputY", 188);
		intent.putExtra("return-data", true);
		((Activity) mContext).startActivityForResult(intent,
				ActivityRequestCode.CANCEL);
	}

	/**
	 * ����ü�֮���ͼƬ����
	 * 
	 * @param picdata
	 */
	public void getImageToView(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			Bitmap roundPhoto = AsyncImageDownLoader.toRoundBitmap(photo,
					mMyPhoto.getLayoutParams().width - 4,
					mMyPhoto.getLayoutParams().height - 4);
			mMyPhoto.setImageBitmap(roundPhoto);
			uploadFriendsAvatar(photo, 30);
		}
	}

	/**
	 * �ϴ�ͷ�񵽷����
	 * 
	 * @param bm
	 *            Ϊͷ��ͼƬ qualityΪѹ����
	 */
	private void uploadFriendsAvatar(Bitmap bm, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, quality, baos);
		InputStream uploadAvatar = new ByteArrayInputStream(baos.toByteArray());
		FriendsRequestParam param = new FriendsRequestParam(
				FriendsRequestParam.TASK_UPLOAD_AVATAR);
		param.addParam("avatar", uploadAvatar);
		if (param.getTaskCategory() == FriendsRequestParam.TASK_UPLOAD_AVATAR)
			HealthyApplication.mAsyncHealthy.uploadAvatar(param, listener);

	}

	/**
	 * �����û���ͷ��
	 */
	private void loadFriendsAvatar() {
		FriendsRequestParam param = new FriendsRequestParam(
				FriendsRequestParam.TASK_DOWNLOAD_AVATAR);
		param.addParam("username", mUser.getString("username", ""));
		if (param.getTaskCategory() == FriendsRequestParam.TASK_DOWNLOAD_AVATAR)
			HealthyApplication.mAsyncHealthy.downloadAvatar(param, listener);
	}

	RequestListener<FriendsResponseBean> listener = new RequestListener<FriendsResponseBean>() {

		@Override
		public void onStart() {
			handler.sendEmptyMessage(0);
		}

		@Override
		public void onComplete(FriendsResponseBean bean) {
			// TODO Auto-generated method stub
			Message msg = Message.obtain(handler);
			msg.what = 1;
			msg.obj = bean;
			msg.sendToTarget();
		}
	};
}
