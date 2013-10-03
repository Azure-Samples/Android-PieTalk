package com.msdpe.pietalk.datamodels;

import java.util.Date;

public class Friend {
	
	@com.google.gson.annotations.SerializedName("fromUserId")
	private String mFromUserId;
	@com.google.gson.annotations.SerializedName("toUserId")
	private String mToUserId;
	@com.google.gson.annotations.SerializedName("toUsername")
	private String mToUsername;
	@com.google.gson.annotations.SerializedName("status")
	private String mStatus;
	@com.google.gson.annotations.SerializedName("createDate")
	private Date mCreateDate;
	@com.google.gson.annotations.SerializedName("id")
	private int mId;

	public Friend() {}

	public int getId() { return mId; } 
	//public final void setId(int id) { mId = id; }
	public String getFromUserId() { return mFromUserId; }
	public String getToUserId() { return mToUserId; }
	public String getToUsername() { return mToUsername; }
	public String getStatus() { return mStatus; }
	public Date getCreateDate() { return mCreateDate; }	
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Friend && ((Friend) o).mId == mId;
	}
}
