package com.ihub.android.app;

import java.util.HashMap;

import com.ihub.android.app.net.FetchUserData;
import com.ihub.android.app.service.UpdateMembersInfoService;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBar.Type;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class Home extends GDActivity implements OnClickListener {

	private static final int ABOUT = Menu.FIRST + 1;
	private static final int SETTINGS = Menu.FIRST + 2;

	private ImageView btnSignin, btnMembers;
	private AlertDialog.Builder builder;
	private AlertDialog alert;
	private String SCANNED_QR_CODE = "", RPC_METHOD = "",
			SCANNED_QR_FORMAT = "";
	private HashMap requestParams[];
	private HashMap resultMap;
	private String TAG = "iHubHome";
	private FetchUserData fetchUserData;
	private Handler mHandler;
	private static final int DIALOG_PROMPT = 0;
	private static final int REQUEST_CODE_SETTINGS = 1;
	private ProgressDialog progressDialog;

	public Home() {
		super(Type.Dashboard);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.main);
		btnSignin = (ImageView) findViewById(R.id.btn_signin);
		btnMembers = (ImageView) findViewById(R.id.btn_members);
		
		mHandler = new Handler();
		btnMembers.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				/*
				 * Start the Members Activity  
				 */
				Intent intent = new Intent(Home.this, SelectMemberType.class);
				startActivity(intent);
			}
		});

		btnSignin.setOnClickListener(this);
		// load settings
		Log.w(TAG, "The length of the URL - "+UpdateMembersInfoService.URL_STRING.length());
		if (UpdateMembersInfoService.URL_STRING.length() == 0) {
			Log.w(TAG, "About to load settings");
			Context con = getApplicationContext();
			UpdateMembersInfoService.loadSettings(con);
			Log.w(TAG, "Just finished loading shared settings");
			Log.w(TAG, "The length of the URL - "+UpdateMembersInfoService.URL_STRING.length());
		}

		SharedPreferences sharedPreferences = getSharedPreferences(
				"iHubService", 0);
		String url = sharedPreferences.getString("WebServiveUrl", "");
		boolean b  = sharedPreferences.getBoolean("", false); 
		Log.w(TAG, "Stored URL - " + url);
		
		// check if URL has been set
		if (UpdateMembersInfoService.URL_STRING.length() == 0) {
			// means this is a new install or the settings have been corrupted,
			// prompt them!
			mHandler.post(mDisplayPrompt);
		}
		fetchUserData = new FetchUserData();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	final Runnable mDisplayPrompt = new Runnable() {
		public void run() {
			showDialog(DIALOG_PROMPT);
		}
	};

	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				resultMap = (HashMap) msg.getData().getParcelable("hashmap");
				break;
			}
			progressDialog.dismiss();
		}
	};

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_signin) {
			builder = new AlertDialog.Builder(this);
			builder
					.setMessage(
							"Place the Green member's card under the camera to begin the scan...")
					.setIcon(R.drawable.signin_dialog_icon_hov).setTitle(
							"Scan Badge").setCancelable(false)
					.setPositiveButton("Cool",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									/*
									 * Invoke the Barcode scanner intent to scan the QRCODE
									 */
									Intent intent = new Intent(
											"com.google.zxing.client.android.SCAN");
									intent
											.putExtra("SCAN_MODE",
													"QR_CODE_MODE");
									
									startActivityForResult(intent, 0);
								}
							}).setNegativeButton("Not now",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			alert = builder.create();
			alert.show();
		}
	}

	@SuppressWarnings("unchecked")
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		/*
		 * Function called once the Scanner activity has returned with the QR contents.
		 */
		int duration = Toast.LENGTH_LONG;
		Toast toast;
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				SCANNED_QR_CODE = intent.getStringExtra("SCAN_RESULT");
				SCANNED_QR_FORMAT = intent.getStringExtra("SCAN_RESULT_FORMAT");
				String toastMessage = "";
				// Handle successful scan

				try {
					// Handle successful scan
					SCANNED_QR_CODE = "C568909";
					RPC_METHOD = "ihub.validateuser";
					requestParams = new HashMap[1];
					requestParams[0] = new HashMap<String, String>();
					requestParams[0].put("qrCode", SCANNED_QR_CODE);

					resultMap = (HashMap) fetchUserData.sendDetailsToServer(
							RPC_METHOD, requestParams);

					Log.w(TAG, "Successfully fetched user info from server.");
					Log.w(TAG, "The returned values - " + resultMap);

					HashMap statusMap = (HashMap) resultMap
							.get("requestStatus");

					int statusCode = (Integer) statusMap.get("status");
					if (statusCode == 2) {
						toastMessage = "user ID " + SCANNED_QR_CODE
								+ " NOT FOUND.";
					} else {

						HashMap userDetails = (HashMap) resultMap
								.get("userData");
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
						newIntent.putExtra("qrCode", SCANNED_QR_CODE);
						newIntent.putExtra("memberType", userDetails.get("memberType").toString());
						startActivity(newIntent);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the scan activity. Do something.
			}
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROMPT: {
			AlertDialog dialog = (new AlertDialog.Builder(this)).create();
			dialog.setTitle("iHub Setup");
			dialog
					.setMessage("Oops Look like its you just installed iHub. Run setup");
			dialog.setButton2("Ok", new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Intent launchPreferencesIntent = new Intent().setClass(
							Home.this, Settings.class);
					startActivity(launchPreferencesIntent);
					dialog.dismiss();
				}
			});
			dialog.setCancelable(false);
			return dialog;
		}

		}
		return null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		populateMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		populateMenu(menu);

		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// applyMenuChoice(item);

		return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
	}

	public boolean onContextItemSelected(MenuItem item) {

		return (applyMenuChoice(item) || super.onContextItemSelected(item));
	}

	private void populateMenu(Menu menu) {
		MenuItem i;
		i = menu.add(Menu.NONE, ABOUT, Menu.NONE, R.string.menu_about);
		i.setIcon(R.drawable.ihub_about);
		
		i = menu.add(Menu.NONE, SETTINGS, Menu.NONE, R.string.menu_settings);
		i.setIcon(R.drawable.ihub_settings);

		

	}

	private boolean applyMenuChoice(MenuItem item) {
		Intent launchIntent;
		switch (item.getItemId()) {
		case ABOUT:
			Toast toast = null;
			Context context = getApplicationContext();
			toast = Toast.makeText(context,
					"About activity under-construction.", Toast.LENGTH_LONG);
			toast.show();
			return true;
			
		case SETTINGS:
			launchIntent = new Intent().setClass(Home.this, Settings.class);

			// Make it a subactivity so we know when it returns
			startActivityForResult(launchIntent, REQUEST_CODE_SETTINGS);
			setResult(RESULT_OK);
			return true;
		

		}
		return false;
	}
}