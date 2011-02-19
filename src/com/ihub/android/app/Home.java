package com.ihub.android.app;

import java.util.HashMap;

import org.xmlrpc.android.XMLRPCException;

import com.ihub.android.app.data.IhubDatabaseHelper;
import com.ihub.android.app.net.FetchUserData;
import com.ihub.android.app.service.UpdateMembersInfo;
import com.ihub.android.app.service.UpdateMembersInfoService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 
 * @author lisudza
 * 
 */

public class Home extends Activity {

	private ImageView btn_signin, btn_memebers;
	private AlertDialog.Builder builder;
	private HashMap<String, String>[] params;
	private String RPC_METHOD;
	private FetchUserData fetchUserData;
	private Handler mHandler;
	private IhubDatabaseHelper iHubDatabaseHelper;
	private UpdateMembersInfo mService = null;
	private String TAG = "iHubHome";
	private boolean started;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		startService(new Intent("com.ihub.android.app.service.UpdateMembersInfoService"));
		Log.w(TAG, "Service has been started");
		iHubDatabaseHelper = new IhubDatabaseHelper(this);
		iHubDatabaseHelper.createDatabase();
		iHubDatabaseHelper.createTable();

		fetchUserData = new FetchUserData();
		builder = new AlertDialog.Builder(this);
		btn_signin = (ImageView) findViewById(R.id.btn_signin);
		btn_memebers = (ImageView) findViewById(R.id.btn_members);
		btn_signin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (v.getId() == R.id.btn_signin) {
					builder
							.setMessage(
									"Place the Green member's card under the camera to begin the scan...")
							.setIcon(R.drawable.dialog_icon).setTitle(
									"Scan Badge").setCancelable(false)
							.setPositiveButton("Cool",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											Intent intent = new Intent(
													"com.google.zxing.client.android.SCAN");
											intent.putExtra("SCAN_MODE",
													"QR_CODE_MODE");
											startActivityForResult(intent, 0);
										}
									}).setNegativeButton("Not now",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					AlertDialog alert = builder.create();
					alert.show();
				}

			}

		});

		btn_memebers.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Home.this, MembersIn.class);
				startActivity(intent);
			}
		});
	}

	/*private void startService(){
	     if (started) {
	       Toast.makeText(this, "Service already started", Toast.LENGTH_SHORT).show();
	     } else {
	       Intent i = new Intent();
	       i.setClassName("com.ihub.android.app.service", "UpdateMembersInfoService");
	       startService(i);
	       started = true;
	       // updateServiceStatus();
	       Log.d( getClass().getSimpleName(), "startService()" );
	      }
	                 
	  }*/

	
	@SuppressWarnings("unchecked")
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
		Toast toast;

		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				String qrCodeContents = intent.getStringExtra("SCAN_RESULT");
				String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				String toastMessage = "";
				// Handle successful scan
				qrCodeContents = "C568909";
				RPC_METHOD = "ihub.validateuser";
				params = new HashMap[1];
				params[0] = new HashMap<String, String>();
				params[0].put("qrCode", qrCodeContents);

				try {
					HashMap result = (HashMap) fetchUserData
							.sendDetailsToServer(RPC_METHOD, params);

					Log.w(TAG, "Successfully fetched user info from server.");
					Log.w(TAG, "The returned values - " + result);
					HashMap statusMap = (HashMap) result.get("requestStatus");

					int statusCode = (Integer) statusMap.get("status");
					if (statusCode == 2) {
						toastMessage = "user ID " + qrCodeContents
								+ " NOT FOUND.";
					} else {
						toastMessage = "Houston, we have a launch.";
						HashMap userDetails = (HashMap) result.get("userData");
						Intent newIntent = new Intent(Home.this,
								MemberSignIn.class);
						newIntent.putExtra("firstName", userDetails.get(
								"firstName").toString());
						newIntent.putExtra("lastName", userDetails.get(
								"lastName").toString());
						newIntent.putExtra("userID", userDetails.get("userID")
								.toString());
						newIntent.putExtra("isAllowed", userDetails.get(
								"isAllowed").toString());
						newIntent.putExtra("profilePic", userDetails.get(
								"profilePic").toString());
						newIntent.putExtra("hasProfileChanged", Boolean
								.valueOf(userDetails.get("hasProfileChanged")
										.toString()));
						newIntent.putExtra("telephone", userDetails.get(
								"telephone").toString());
						newIntent.putExtra("emailAdd", userDetails.get(
								"emailAdd").toString());
						newIntent.putExtra("occupation", userDetails.get(
								"occupation").toString());
						newIntent.putExtra("profilePicURL", userDetails.get(
								"profilePicURL").toString());
						newIntent.putExtra("country", userDetails
								.get("country").toString());
						newIntent.putExtra("qrCode", qrCodeContents);
						startActivity(newIntent);

					}

					toast = Toast.makeText(context, toastMessage, duration);

				} catch (XMLRPCException e) {
					toast = Toast.makeText(context, e.getMessage(), duration);
				}

				toast.show();

			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
				toast = Toast.makeText(context, "Sorry User Cancelled",
						duration);
				toast.show();
			}
		}
	}

	private ServiceConnection svcConn = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			mService = UpdateMembersInfo.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

	};
}
