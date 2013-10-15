package com.msdpe.pietalk.adapters;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.msdpe.pietalk.R;
import com.msdpe.pietalk.datamodels.Pie;

public class PiesArrayAdapter extends ArrayAdapter<Pie> {
	private Context mContext;
	private List<Pie> mPies;
	
	public PiesArrayAdapter(Context context, List<Pie> pies) {
		
		super(context, R.layout.list_row_pie, pies);
		mContext = context;
		mPies = pies;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		Pie pie = mPies.get(position);
		LayoutInflater inflater = (LayoutInflater) mContext
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.list_row_pie, parent, false);    
	    TextView lblFromUsername = (TextView) view.findViewById(R.id.lblFromUsername);
	    lblFromUsername.setText(pie.getFromUsername());
	    TextView lblDateSent = (TextView) view.findViewById(R.id.lblDateSent);
	    Date createDate = pie.getCreateDate();
	    lblDateSent.setText(DateFormat.getDateInstance().format(createDate));
	    TextView lblInstructions = (TextView) view.findViewById(R.id.lblInstructions);
	    
	    
	    ImageView imgIndicator = (ImageView) view.findViewById(R.id.imgIndicator);
	    if (pie.getType().equals("FriendRequest")) {
	    		imgIndicator.setImageResource(R.drawable.pie_friend_request);
	    		lblInstructions.setText(mContext.getResources().getString(R.string.instructions_friend_request));
	    }
	    else if (pie.getType().equals("Pie")) {
	    		if (pie.getHasUserSeen()) {
	    			imgIndicator.setImageResource(R.drawable.pie_seen);
	    			lblInstructions.setText(mContext.getResources().getString(R.string.instructions_seen_pie));
	    		}
	    		else {
	    			imgIndicator.setImageResource(R.drawable.pie_not_seen);
	    			lblInstructions.setText(mContext.getResources().getString(R.string.instructions_unseen_pie));
	    		}
	    }
	    return view;
	}
	
	
}
