package com.healthy.ui.tourtracker;

import java.util.ArrayList;
import java.util.List;
import com.healthy.R;
import com.healthy.logic.model.TrackerListBean;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TrackerListAdapter extends BaseAdapter{
	
	@SuppressWarnings("unused")
	private Context context;
	private LayoutInflater listContainer; //��ͼ����
	private List<TrackerListBean> mTrackerList = new ArrayList<TrackerListBean>();
	
	//�Զ���ؼ�����
	public final class ItemHolder
	{
		public ImageView timeView;//��ʾʱ��ͼ��
		public TextView time;//��ʾ��ʷ�켣ʱ��
		public TextView type;//��ʾ��ʷ�켣�����
		public TextView distance;//��ʾ��ʷ�켣����
		public ImageView locationView;//��λ��ʶ
		public TextView location;//��ʾ��ʷ�켣��ʼλ��
	}
	
	public TrackerListAdapter(Context context, List<TrackerListBean> trackerList)
	{
		this.context = context;
		mTrackerList=trackerList;
		listContainer = LayoutInflater.from(context);//������ͼ
		
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mTrackerList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mTrackerList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/**
	 * ����trackerList��һ��view
	 * positionΪ������items��λ�ã�convertViewΪ���ÿ��items�Ĳ���
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ItemHolder holder = null;
		TrackerListBean data=mTrackerList.get(position);
		if(convertView == null)
		{
			holder = new ItemHolder();
			convertView = listContainer.inflate(R.layout.item_tracker_history, null);
			holder.timeView = (ImageView)convertView.findViewById(R.id.tracker_watch_view);
			holder.time = (TextView)convertView.findViewById(R.id.tracker_times_text);
			holder.type = (TextView)convertView.findViewById(R.id.tracker_type_text);
			holder.distance = (TextView)convertView.findViewById(R.id.tracker_distance_text);
			holder.locationView = (ImageView)convertView.findViewById(R.id.tracker_location_view);
			holder.location = (TextView)convertView.findViewById(R.id.tracker_location_text);
			convertView.setTag(holder);
			
		}else
		{
			holder = (ItemHolder)convertView.getTag();
		}
		holder.distance.setText(data.getDistance());
		holder.location.setText(data.getLocation());
		holder.time.setText(data.getTime());
		holder.type.setText(data.getType());
		return convertView;
		
	}
	
	/**
	 * ����б�����
	 */
	public void addTrackerItems(TrackerListBean tracker)
	{
		mTrackerList.add(tracker);
	}
}
