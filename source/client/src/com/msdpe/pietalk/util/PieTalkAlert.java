package com.msdpe.pietalk.util;

import com.msdpe.pietalk.R;

import android.app.Activity;
import android.app.AlertDialog;

public class PieTalkAlert {
	
	public static void showSimpleErrorDialog(Activity context, String errorMessage) {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
		alertBuilder.setMessage(errorMessage)
					.setTitle(R.string.error);
		alertBuilder.setPositiveButton(R.string.ok, null);
		AlertDialog dialog = alertBuilder.create();
		dialog.show();
	}
}
