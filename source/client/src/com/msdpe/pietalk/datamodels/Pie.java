package com.msdpe.pietalk.datamodels;

import java.util.Date;

public class Pie {
	@com.google.gson.annotations.SerializedName("fromUserId")
	private String mFromUserId;
	@com.google.gson.annotations.SerializedName("toUserId")
	private String mToUserId;
	@com.google.gson.annotations.SerializedName("fromUsername")
	private String mFromUsername;
	@com.google.gson.annotations.SerializedName("type")
	private String mType;
	@com.google.gson.annotations.SerializedName("createDate")
	private Date mCreateDate;
	@com.google.gson.annotations.SerializedName("updateDate")
	private Date mUpdateDate;
	@com.google.gson.annotations.SerializedName("timeToLive")
	private int mTimeToLive;
	@com.google.gson.annotations.SerializedName("userHasSeen")
	private boolean mUserHasSeen;
	@com.google.gson.annotations.SerializedName("id")
	private int mId;

	public Pie() {}

	public int getId() { return mId; } 
	//public final void setId(int id) { mId = id; }
	public String getFromUserId() { return mFromUserId; }
	public String getToUserId() { return mToUserId; }
	public String getFromUsername() { return mFromUsername; }
	public String getType() { return mType; }
	public Date getCreateDate() { return mCreateDate; }	
	public Date getUpdateDate() { return mUpdateDate; }
	public int getTimeToLive() { return mTimeToLive; }
	public boolean getHasUserSeen() { return mUserHasSeen; }	
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Pie && ((Pie) o).mId == mId;
	}
}