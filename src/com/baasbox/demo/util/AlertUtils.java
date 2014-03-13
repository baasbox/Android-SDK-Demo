package com.baasbox.demo.util;

import android.app.AlertDialog;
import android.content.Context;

import com.baasbox.android.BaasClientException;
import com.baasbox.android.BaasException;
import com.baasbox.android.BaasServerException;

public class AlertUtils {

	public static void showErrorAlert(Context context, BaasClientException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(true);
		builder.setTitle("Client Error");
		builder.setMessage("An unexpected app error: " + e.httpStatus + " (" + e.code + ") :" + e.getMessage());
		builder.setNegativeButton("Cancel", null);
		builder.create().show();
	}
	
	public static void showErrorAlert(Context context, BaasServerException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(true);
		builder.setTitle("Server Error");
		builder.setMessage("An unexpected server error: " + e.httpStatus + " (" + e.code + "):" + e.getMessage());
		builder.setNegativeButton("Cancel", null);
		builder.create().show();
	}
	
	public static void showErrorAlert(Context context, BaasException e) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(true);
		builder.setTitle("Error");
		builder.setMessage("An unexpected error: " + e.getMessage());
		builder.setNegativeButton("Cancel", null);
		builder.create().show();
	}
}
