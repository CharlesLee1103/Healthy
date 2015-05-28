package com.healthy.ui.tourtracker;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKGeneralListener;
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.healthy.R;
import com.healthy.logic.HealthyApplication;
import com.healthy.logic.model.LocationInDb;
import com.healthy.logic.model.TrackerListBean;
import com.healthy.ui.base.FlipperLayout.OnOpenListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * �˶����Ƹ��ٽ���
 * 
 * @author zc
 * */
public class TourTracker implements OnScrollListener {

	private View mTourTracker;
	private Context mContext;
	private OnOpenListener mOnOpenListener;
	private ImageView mFlipMenu;
	private LocationClient mLocationClient = null;// ��λ�������
	private static final String KEY = "39E3359ab5dccbb33dae0b5621dab52e";
	private BMapManager mBMapManager = null;
	private int mIsInsertAdr = 0;
	private ListView mListView;
	private Button mTrackerBtn;
	private ImageView mNodataView;
	private ImageView mRefreshView;
	private ProgressBar mRefreshProgress;
	private TrackerListAdapter mTrackerListAdapter;

	private int mCount = 0;
	public Boolean mIsStart;
	private List<TrackerListBean> listItems = new ArrayList<TrackerListBean>();
	private List<TrackerListBean> mListItems = new ArrayList<TrackerListBean>();
	private List<GeoPoint> mPointList = new ArrayList<GeoPoint>();
	private String mDistance;
	private boolean mToBottom = false;
	private int deleteId;// Ҫɾ����ID

	public TourTracker(Context context) {
		mContext = context;
		mTourTracker = LayoutInflater.from(mContext).inflate(
				R.layout.page_tour_tracker, null);
		mFlipMenu = (ImageView) mTourTracker
				.findViewById(R.id.tracker_flip_menu);
		mListView = (ListView) mTourTracker.findViewById(R.id.history_track);
		mTrackerBtn = (Button) mTourTracker.findViewById(R.id.start_track);
		mNodataView = (ImageView) mTourTracker.findViewById(R.id.nodata_view);
		mRefreshView = (ImageView) mTourTracker.findViewById(R.id.refresh_btn);
		mRefreshProgress = (ProgressBar) mTourTracker
				.findViewById(R.id.refresh_progress);
		mLocationClient = new LocationClient(mContext);
		mIsStart = false;

		initListView();
		setListener();

		mListView.setOnScrollListener(this);
	}

	private void initLocation() {
		mLocationClient.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceivePoi(BDLocation arg0) {

			}

			@Override
			public void onReceiveLocation(BDLocation location) {
				if (location == null)
					Toast.makeText(mContext, "��ȡλ����Ϣʧ�ܣ��������磡",
							Toast.LENGTH_SHORT).show();
				if (mIsInsertAdr == 0) {
					// ����һ�ζ�λ��ַ���뵽tracker_info���е�start_address�ֶ�
					String adr = location.getAddrStr();
					HealthyApplication.mDbUtil.updateTrackerAdr(adr);
					mIsInsertAdr++;
				}
				Log.i("tag",
						location.getLongitude() + "-----"
								+ location.getLatitude());
				int id = HealthyApplication.mDbUtil.getLastTrackerID();
				LocationInDb locationInDb = new LocationInDb();
				locationInDb.setId(id);
				locationInDb.setLongitude(location.getLongitude());
				locationInDb.setLatitude(location.getLatitude());
				locationInDb.setTime(getNowtime());
				HealthyApplication.mDbUtil.insertIntoLocation(locationInDb);
			}
		});
	}

	private void setListener() {

		mFlipMenu.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (mOnOpenListener != null) {
					mOnOpenListener.open();
				}
			}
		});

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int id = listItems.get(arg2).getId();
				Intent intent = new Intent();
				intent.setClass(mContext, HistoryTrackerActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("id", id);
				intent.putExtras(bundle);
				mContext.startActivity(intent);
				((Activity) mContext).overridePendingTransition(R.anim.roll_up,
						R.anim.roll);
			}
		});

		mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				deleteId = listItems.get(arg2).getId();
				showDialog();
				return true;
			}
		});

		mTrackerBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!mIsStart) {
					HealthyApplication.mDbUtil.insertNullData();
					mTrackerBtn.setText(R.string.stop_tracker);
					requestLocation();
					initBMap();
					mIsStart = true;
				} else {
					mTrackerBtn.setText(R.string.start_tracker);
					mIsStart = false;
					if (!HealthyApplication.mDbUtil.IsStartLocation()) {
						HealthyApplication.mDbUtil.deleteTracker();
						Toast.makeText(mContext, "�ף���û��ʼ��¼�ͽ����ˣ�̫���˰ɣ�",
								Toast.LENGTH_SHORT).show();
					} else {
						mLocationClient.stop();
						hd.sendEmptyMessage(0);
					}
					mIsInsertAdr = 0;
				}
			}
		});

		mRefreshView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				refreshTrackerData();
			}
		});
	}

	private void requestLocation() {
		initLocation();
		setLocationOption();
		mLocationClient.start();
		mLocationClient.requestLocation();
	}

	public void setOnOpenListener(OnOpenListener onOpenListener) {
		mOnOpenListener = onOpenListener;
	}

	private void refreshTrackerData() {

		mRefreshView.setVisibility(View.GONE);
		mRefreshProgress.setVisibility(View.VISIBLE);
		listItems.clear();
		mListItems = HealthyApplication.mDbUtil.getTrackerToList();
		mCount = mListItems.size();
		if (mCount == 0) {
			mListView.setVisibility(View.GONE);
			mNodataView.setVisibility(View.VISIBLE);
		} else {
			mListView.setVisibility(View.VISIBLE);
			mNodataView.setVisibility(View.GONE);
			if (mCount >= 5) {
				for (int i = 0; i < 5; i++)
					listItems.add(mListItems.get(i));
			} else if (0 < mCount && mCount < 5) {
				for (int i = 0; i < mCount; i++)
					listItems.add(mListItems.get(i));
			}
			mTrackerListAdapter = new TrackerListAdapter(mContext, listItems);
			mListView.setAdapter(mTrackerListAdapter);
			mTrackerListAdapter.notifyDataSetChanged();
		}

		mRefreshView.setVisibility(View.VISIBLE);
		mRefreshProgress.setVisibility(View.GONE);
	}

	Handler hd = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:// ֹͣ��¼�켣
				HealthyApplication.mDbUtil.updateTrackerEndTime(getNowtime());
				mPointList = HealthyApplication.mDbUtil.getFromLocation();
				HealthyApplication.mDbUtil.getTrackerType();
				mDistance = computeDistance(mPointList);
				HealthyApplication.mDbUtil.updateTrackerDistance(mDistance);
				refreshTrackerData();
				break;
			case 1:
				loadMoreData();
				mTrackerListAdapter.notifyDataSetChanged();
				break;
			}
		}
	};

	public View getView() {
		return mTourTracker;
	}

	/**
	 * �õ���ǰ��ʱ�䣬��ʽyyyy-MM-dd HH:mm:ss
	 * 
	 * @return ��ǰʱ���ַ���
	 */
	public String getNowtime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		String date = formatter.format(curDate);
		return date;
	}

	// ���ö�λ��ز���
	public void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// ��GPS
		option.setAddrType("all");// ���ص�ַ��Ϣ
		option.setCoorType("bd09ll");// ���صĽ��Ϊ�ٶȾ�γ��
		option.setScanSpan(10000);// ���÷���λ���ʱ��Ϊ10S
		mLocationClient.setLocOption(option);
	}

	/**
	 * �˶��켣listview��ʼ��
	 */
	private void initListView() {
		listItems.clear();
		mListItems = HealthyApplication.mDbUtil.getTrackerToList();
		mCount = mListItems.size();
		Log.i("tag", "mCount:" + mCount);
		if (mCount == 0) {
			mListView.setVisibility(View.GONE);
			mNodataView.setVisibility(View.VISIBLE);
		} else if (mCount >= 5) {
			for (int i = 0; i < 5; i++)
				listItems.add(mListItems.get(i));
		} else if (0 < mCount && mCount < 5) {
			for (int i = 0; i < mCount; i++)
				listItems.add(mListItems.get(i));
		}
		mTrackerListAdapter = new TrackerListAdapter(mContext, listItems);
		mListView.setAdapter(mTrackerListAdapter);
	}

	/**
	 * ���� ��������
	 */
	private void loadMoreData() {
		int count = mTrackerListAdapter.getCount();
		if (count + 5 <= mCount) {
			for (int i = count; i < count + 5; i++) {
				mTrackerListAdapter.addTrackerItems(mListItems.get(i));
			}
		} else {
			for (int i = count; i < mCount; i++) {
				mTrackerListAdapter.addTrackerItems(mListItems.get(i));
			}
		}
		if (mTrackerListAdapter.getCount() == mListItems.size()) {
		}
		// �����listview�ײ��¼�
	}

	/**
	 * �ٶȵ�ͼ��ʼ��
	 */
	public void initBMap() {
		if (mBMapManager == null) {
			mBMapManager = new BMapManager(mContext);
			mBMapManager.init(KEY, new MKGeneralListener() {

				@Override
				public void onGetPermissionState(int error) {
					// TODO Auto-generated method stub
					if (error == MKEvent.ERROR_PERMISSION_DENIED) {
						// ��ȨKey����
						Toast.makeText(mContext,
								"���� DemoApplication.java�ļ�������ȷ����ȨKey��",
								Toast.LENGTH_LONG).show();
					}
				}

				@Override
				public void onGetNetworkState(int error) {
					// TODO Auto-generated method stub
					if (error == MKEvent.ERROR_NETWORK_CONNECT) {
						Toast.makeText(mContext, "���������������", Toast.LENGTH_LONG)
								.show();
					} else if (error == MKEvent.ERROR_NETWORK_DATA) {
						Toast.makeText(mContext, "������ȷ�ļ���������",
								Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}

	/**
	 * ���ڼ���һ�ι켣׷�ٸ����ٶȾ�γ��ֱ�ӵľ���
	 */
	public String computeDistance(List<GeoPoint> point) {
		double distance = 0;
		String sDistance = "0��";
		for (int i = 0; i < point.size(); i++) {
			Log.i("geopoint", point.get(i).getLongitudeE6() + ","
					+ point.get(i).getLatitudeE6());
		}
		if (mPointList.size() <= 1) {
			return sDistance;
		} else {
			for (int i = 0; i < mPointList.size() - 1; i++) {
				GeoPoint mPointOne = new GeoPoint(mPointList.get(i)
						.getLongitudeE6(), mPointList.get(i).getLatitudeE6());
				GeoPoint mPointTwo = new GeoPoint(mPointList.get(i++)
						.getLongitudeE6(), mPointList.get(i++).getLatitudeE6());
				double temp = DistanceUtil.getDistance(mPointOne, mPointTwo);
				distance += temp;
				Log.i("distance", "��࣡������������������>" + temp + "");
			}
		}
		if (distance < 1000)// �жϲ��������1000��ʾ��λΪ�� ������һǧ��λΪǧ��
		{
			sDistance = (int) distance + "��";
		} else {
			double distanceKM = distance / 1000;
			BigDecimal bg = new BigDecimal(distanceKM);// ����С����һλ
			double distanceTwo = bg.setScale(1, BigDecimal.ROUND_HALF_UP)
					.doubleValue();
			sDistance = distanceTwo + "ǧ��";
		}

		return sDistance;

	}

	private void showDialog() {
		new AlertDialog.Builder(mContext).setTitle("ɾ���켣")
				.setMessage("��ȷ��ɾ�������켣��")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.i("tag", "Ҫɾ����¼�����:" + deleteId);
						HealthyApplication.mDbUtil.deleteTrackerById(deleteId);//ɾ��������Ŀ����
						
						dialog.dismiss();
						mTrackerListAdapter.notifyDataSetChanged();
					}
				})
				.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		int lastVisibleItemIndex = firstVisibleItem + visibleItemCount - 1;// ��ȡ���һ���ɼ�Item������(0-based)
		if (totalItemCount - 1 == lastVisibleItemIndex) {
			mToBottom = true;
		} else {
			mToBottom = false;
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE && mToBottom) {
			hd.sendEmptyMessage(1);
		}
	}

	public void overTracker() {
		if (!HealthyApplication.mDbUtil.IsStartLocation()) {
			HealthyApplication.mDbUtil.deleteTracker();
		} else {
			mLocationClient.stop();
			hd.sendEmptyMessage(0);
		}
	}
}
