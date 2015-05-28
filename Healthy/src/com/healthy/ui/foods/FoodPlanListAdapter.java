package com.healthy.ui.foods;

import java.util.HashMap;
import java.util.List;
import com.healthy.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FoodPlanListAdapter extends BaseAdapter {
	private Context mContext;
	private List<HashMap<String, Object>> mList;
	private int selectTag = -1;
	private ViewHolder mHolder;
	private String[] levelText = { "��", "��ͨ", "����", "����" };
	private float[] eachDayText ;
	private int[] levelTextColor = {0xFF9ACD32,0xFF66CDAA,0xFFCD8500,0xFF8B2323};

	public FoodPlanListAdapter(Context context,
			List<HashMap<String, Object>> list, int tag,float[] eachdaycalorie) {
		mContext = context;
		mList = list;
		selectTag = tag;
		eachDayText = eachdaycalorie;
	}

	@Override
	public int getCount() {
		// TODO �Զ����ɵķ������
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO �Զ����ɵķ������
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO �Զ����ɵķ������
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO �Զ����ɵķ������
		if (convertView == null) {
			mHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_foodplan, null);
			
			mHolder.levelText = (TextView) convertView
					.findViewById(R.id.foodplan_leveltext);
			mHolder.eachDayText = (TextView) convertView
					.findViewById(R.id.foodplan_eachdaytext);
			mHolder.timeText = (TextView) convertView
					.findViewById(R.id.foodplan_timetext);
			mHolder.selectBtn = (ImageView) convertView
					.findViewById(R.id.foodplan_selectimage);
			convertView.setTag(mHolder);
		} else {
			mHolder = (ViewHolder) convertView.getTag();
		}
		mHolder.levelText.setText(levelText[position]);
		mHolder.levelText.setTextColor(levelTextColor[position]);
		mHolder.eachDayText.setText("ÿ��������"+eachDayText[position]+"ǧ��");
		mHolder.timeText
				.setText("�ƻ�����"+mList.get(position).get("duration").toString()+"��");
		if (selectTag == position) {
			mHolder.selectBtn
					.setBackgroundResource(R.drawable.ic_foodplan_selected);
		} else {
			mHolder.selectBtn
					.setBackgroundResource(R.drawable.ic_foodplan_unselected);
		}
		return convertView;
	}

	class ViewHolder {
		TextView levelText;
		TextView eachDayText;
		TextView timeText;
		ImageView selectBtn;
	}

	public void setTag(int tag) {
		selectTag = tag;
		notifyDataSetChanged();
	}

}
