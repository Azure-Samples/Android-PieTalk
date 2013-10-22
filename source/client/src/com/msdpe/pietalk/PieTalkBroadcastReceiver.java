package com.msdpe.pietalk;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class PieTalkBroadcastReceiver extends BroadcastReceiver {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        mContext = context;
        String messageType = gcm.getMessageType(intent);
        if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
            sendNotification("Send error: " + intent.getExtras().toString());
        } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
            sendNotification("Deleted messages on server: " + 
                    intent.getExtras().toString());
        } else {
            sendNotification("Received: " + intent.getExtras().toString());
        }
        setResultCode(Activity.RESULT_OK);
	}
	
	private void sendNotification(String msg) {
			
		mNotificationManager = (NotificationManager)
	              mContext.getSystemService(Context.NOTIFICATION_SERVICE);
	
		PieTalkApplication app = (PieTalkApplication) mContext.getApplicationContext(); 
		PieTalkService pieTalkService = app.getPieTalkService();
		Activity activity = (Activity) pieTalkService.getActivityContext();

		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				new Intent(activity, Activity.class), 0);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(mContext)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Notification Hub Demo")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
				.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

}
