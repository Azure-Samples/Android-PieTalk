package com.msdpe.pietalk;

public final class Constants {
	public static short MIN_PASSWORD_LENGTH = 7;
	public static short MIN_USERNAME_LENGTH = 4;
	
	public static String BROADCAST_FRIENDS_UPDATED = "friends.updated";
	public static String BROADCAST_PIES_UPDATED = "pies.updated";
	
	public static enum CameraUIMode {
        UI_MODE_PRE_PICTURE,
		UI_MODE_TAKING_PICTURE,
		UI_MODE_REVIEW_PICTURE,
		UI_MODE_REVIEW_VIDEO,
		UI_MODE_TAKING_VIDEO
    }
	
	public static enum PieType {
		PIE_TYPE_FRIEND_REQUEST_ACCEPTED,
		PIE_TYPE_FRIEND_REQUEST_UNACCEPTED,
		PIE_TYPE_PIE_SEEN,
		PIE_TYPE_PIE_UNSEEN,
	}
}
