package com.msdpe.pietalk;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.msdpe.pietalk.util.PieTalkLogger;

public class PieTalkBroadcastReceiver extends BroadcastReceiver {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;
	private Context mContext;
	private final String TAG = "PieTalkBroadcastReceiver";
	private PieTalkService mPieTalkService;
	private PieTalkApplication mPieTalkApplication;

	@Override
	public void onReceive(Context context, Intent intent) {				
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        mContext = context;
        mPieTalkApplication = (PieTalkApplication) mContext.getApplicationContext(); 
		mPieTalkService = mPieTalkApplication.getPieTalkService();
        
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            sendNotification("Send error: " + intent.getExtras().toString());
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            sendNotification("Deleted messages on server: " + 
                    intent.getExtras().toString());
        } else {
        		PieTalkLogger.i(TAG, "Message: " + intent.getStringExtra("message"));
        		String message = intent.getStringExtra("message");
        		processPush(message);
            //sendNotification("Received: " + intent.getExtras().toString());
        		//PieTalkLogger.i(TAG, "Message: " + intent.getStringExtra("message"));
        }
        setResultCode(Activity.RESULT_OK);
	}
	
	private void processPush(String message) {
		if (message.equals("Friend request received")) {
			if (mPieTalkApplication.getIsApplicationActive())
				mPieTalkService.getPies();
			else
				sendNotification("Pie received");
		} else
			sendNotification("Message received: " + message);
	}
	
	private void sendNotification(String msg) {
			
		mNotificationManager = (NotificationManager)
	              mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	
		
		Activity activity = (Activity) mPieTalkService.getActivityContext();

		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				new Intent(activity, Activity.class), 0);

		PieTalkLogger.i(TAG, "Push received: " + msg);
		
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(mContext)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("PieTalk Message")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}
