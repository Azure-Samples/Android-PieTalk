package com.msdpe.pietalk;

public final class Constants {
	public static short MIN_PASSWORD_LENGTH = 7;
	public static short MIN_USERNAME_LENGTH = 4;
	
	public static String BROADCAST_FRIENDS_UPDATED = "friends.updated";
	public static String BROADCAST_PIES_UPDATED = "pies.updated";
	public static String BROADCAST_PIE_SENT = "pie.sent";
	
	public static int REQUEST_CODE_SEND_TO_FRIENDS = 1001;
		
	public static int RESULT_CODE_PIE_SENT = 9009;
	
	//Push notifications
	public static String SENDER_ID = "371430473080";
	public static String NOTIFICATION_HUB_CONNECTION_STRING = "Endpoint=sb://pietalk-ns.servicebus.windows.net/;SharedAccessKeyName=DefaultListenSharedAccessSignature;SharedAccessKey=YHf05x5idR7oeW3ZV/+kpCjppUxsS5vn1j++pCwsMxc=";
	public static String NOTIFICATIN_HUB_NAME = "pietalk";
	
	public static enum CameraUIMode {
        UI_MODE_PRE_PICTURE,
		UI_MODE_TAKING_PICTURE,
		UI_MODE_REVIEW_PICTURE,
		UI_MODE_REVIEW_VIDEO,
		UI_MODE_TAKING_VIDEO,
		UI_MODE_REPLYING
    }
	
	public static enum PieType {
		PIE_TYPE_FRIEND_REQUEST_ACCEPTED,
		PIE_TYPE_FRIEND_REQUEST_UNACCEPTED,
		PIE_TYPE_PIE_SEEN,
		PIE_TYPE_PIE_UNSEEN,
	}
}
