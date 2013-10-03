package com.msdpe.pietalk.adapters;

import java.util.List;

import android.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.msdpe.pietalk.PieTalkService;
import com.msdpe.pietalk.datamodels.Friend;

public class ViewFriendsListArrayAdapter extends ArrayAdapter<Friend> {
	private PieTalkService mPieTalkService;
	
	
	public ViewFriendsListArrayAdapter(Context context, 
			PieTalkService pieTalkService, List<Friend> friends) {
		super(context, 0, friends);
		mPieTalkService = pieTalkService;
	}
	
	public ViewFriendsListArrayAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext()
	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.list_content, parent, false);    
	    TextView text1 = (TextView) view.findViewById(R.id.text1);
	    text1.setText(mPieTalkService.getLocalFriends().get(position).getToUsername());        
	    return view;
	}

}
