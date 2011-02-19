package com.ihub.android.app.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import com.ihub.android.app.Home;
import com.ihub.android.app.Person;
import com.ihub.android.app.data.IhubDatabaseHelper;
import com.ihub.android.app.data.ImageManager;
import com.ihub.android.app.net.FetchUserData;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class UpdateMembersInfoService extends Service {

	public static final String PREFS_NAME = "iHubService";
	private static String TAG = "UpdateMembersInfoService";
	private Handler serviceHandler;
	private Task myTask;
	private String RPC_METHOD;
	private HashMap<String, String> params[];
	private FetchUserData fetchUserData;
	private Person person;
	private HashMap<String, Object> response;
	private HashMap results;
	private HashMap<String, String> result;
	private HashMap statusMap;
	private IhubDatabaseHelper ihubDatabaseHelper;
	private ImageManager imageManager;
	private boolean isMemberNew, hasProfileChanged;
	public static int SLEEP_TIME;
	// public static String URL_STRING = "http://codediva.co.ke/ihub/";
	public static String URL_STRING = "";
	public static int AutoUpdateInterval;
	public static String savePath = "/sdcard/ihub/pics/";
	public static boolean isRunning = false;
	public static boolean autoSync = false;

	@Override
	public void onCreate() {
		super.onCreate();
		myTask = new Task();
		fetchUserData = new FetchUserData();
		ihubDatabaseHelper = new IhubDatabaseHelper(this);
		ihubDatabaseHelper.createDatabase();
		ihubDatabaseHelper.createTable();
		imageManager = new ImageManager();
		Log.w(TAG, "onCreate().");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		serviceHandler = new Handler();
		serviceHandler.postDelayed(myTask, 7000L);
		loadSettings(this);
		Log.d(getClass().getSimpleName(),
				"onStart() the Webservcie that am consuming is - "
						+ URL_STRING);
		isRunning = true;
		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		serviceHandler.removeCallbacks(myTask);
		serviceHandler = null;
		Log.w(TAG, "onDestroy() --- My service has just been killed.");
		isRunning = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (UpdateMembersInfo.class.getName().equals(intent.getAction())) {
			return ubinder;
		}
		return null;
	}

	private final UpdateMembersInfo.Stub ubinder = new UpdateMembersInfo.Stub() {

		@Override
		public Map fetchMembers() throws RemoteException {
			// TODO Auto-generated method stub
			return (getMembersState());
		}
	};

	synchronized private Map getMembersState() {
		// List l[] = (List[]) new HashMap[2];
		Map map = (Map) new HashMap();
		return map;
	}

	@SuppressWarnings("unchecked")
	void updateMembersInfo() {
		RPC_METHOD = "ihub.getMembersOnline";
		params = new HashMap[1];
		params[0] = new HashMap<String, String>();

		try {
			response = (HashMap<String, Object>) fetchUserData
					.sendDetailsToServer(RPC_METHOD, params);

			statusMap = (HashMap) response.get("status");
			int status = Integer.parseInt(statusMap.get("status").toString());

			Log.w(TAG, "Returned map - " + response);
			if (status != 1) {
				Log.w(TAG,
						"There are no members currently online... Returned status - "
								+ status);
			} else {
				results = (HashMap) response.get("userData");

				for (int i = 0; i < results.size(); i++) {
					result = (HashMap<String, String>) results.get("response"
							+ i);
					person = new Person();
					person.setFirstName(result.get("firstName"));
					person.setLastName(result.get("lastName"));
					person.setOccupation(result.get("occupation"));
					person.setProfilePic(result.get("profilePic"));
					person.setProfilePicURL(result.get("profilePicURL"));
					person.setQrCode(result.get("qrCode"));
					person.setTelephone(result.get("telephone"));
					person.setCountry(result.get("country"));
					person.setEmailAddress(result.get("emailAdd"));
					person.setCloudUserID(result.get("userID"));
					hasProfileChanged = Boolean.parseBoolean(result
							.get("hasProfileChanged"));
					person.setMemberType(result.get("memberType"));

					isMemberNew = checkIfUserExists();

					syncUserInfo();

				}
			}

		} catch (XMLRPCException e) {
			Log.w(TAG, "Failed to fetch online members from server.");
			e.printStackTrace();
		}

	}

	public long getUserID() {
		long userID = 0;
		Cursor cursor = ihubDatabaseHelper.getMemberUserID(person
				.getCloudUserID());
		int numRows = cursor.getCount();

		if (numRows >= 1) {
			userID = cursor.getLong(7);
			cursor.close();
		} else {

		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return userID;
	}

	@SuppressWarnings("static-access")
	public void syncUserInfo() {
		if (!isMemberNew) {
			Log.w(TAG, "User ID - " + person.getCloudUserID() + " "
					+ person.getFirstName()
					+ " does not exist in the local database. Create entry");
			// download the profile picture.
			try {
				byte[] imageBuffer;
				Log.w(TAG, "Fetching profile picture for user "
						+ person.getFirstName() + " from server...");
				imageBuffer = imageManager
						.fetchImage(person.getProfilePicURL());
				Log.w(TAG, "Saving the profile picture for user "
						+ person.getFirstName() + " to the SCARD.");
				imageManager.writeImage(imageBuffer, person.getProfilePic());

				long insertID = ihubDatabaseHelper.addMember(person
						.getFirstName(), person.getLastName(), person
						.getCountry(), person.getQrCode(), person
						.getProfilePic(), person.getOccupation(), person
						.getCloudUserID(), person.getMemberType(), person
						.getTelephone(), person.getEmailAddress());
				Log.w(TAG, "User " + person.getFirstName()
						+ " has been logged into the database. Unique ID - "
						+ insertID);

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				Log.w(TAG, "Failed to fetch the image from the server. URL - "
						+ person.getProfilePicURL());
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log
						.w(TAG,
								"Failed to create the image on the SCARD. Please ensure you have an SDCARD.");
				e.printStackTrace();
			}

		} else if (hasProfileChanged) {
			// fetch the image and update the profile.
			// download the profile picture.
			Log.w(TAG, "User ID - " + person.getCloudUserID() + " "
					+ person.getFirstName()
					+ " Profile has changed. Update local copy.");
			try {
				byte[] imageBuffer;
				Log.w(TAG, "Fetching profile picture for user "
						+ person.getFirstName() + "from server...");
				imageBuffer = imageManager
						.fetchImage(person.getProfilePicURL());
				Log.w(TAG, "Saving the profile picture for user "
						+ person.getFirstName() + "to the SCARD.");
				imageManager.writeImage(imageBuffer, person.getProfilePic());

				long rowId = getUserID();

				ihubDatabaseHelper.updateMemberDetails(rowId, person
						.getFirstName(), person.getLastName(), person
						.getCountry(), person.getQrCode(), person
						.getProfilePic(), person.getOccupation(), person
						.getMemberType(), person.getTelephone(), person
						.getEmailAddress());
				Log.w(TAG, person.getFirstName()
						+ "'s profile has been updated.");
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				Log.w(TAG, "Failed to fetch the image from the server. URL - "
						+ person.getProfilePicURL());
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log
						.w(TAG,
								"Failed to create the image on the SCARD. Please ensure you have an SDCARD.");
				e.printStackTrace();
			}

		} else {
			Log.w(TAG, person.getFirstName()
					+ " Profile has not changed. proceed and render.");
		}
	}

	public boolean checkIfUserExists() {
		boolean memberExists;
		Cursor cursor = ihubDatabaseHelper.getMemberUserID(person
				.getCloudUserID());
		int numRows = cursor.getCount();
		Log
				.w(TAG, "checkIfUserExists --- number of rows returned - "
						+ numRows);
		if (numRows >= 1) {
			cursor.close();
			memberExists = true;
		} else {
			memberExists = false;
		}

		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return memberExists;
	}

	class Task implements Runnable {

		@Override
		public void run() {
			// fetch and update DB.
			updateMembersInfo();
			serviceHandler.postDelayed(this, 2000);
		}

	}

	public static void saveSettings(Context context) {
		final SharedPreferences settings = context.getSharedPreferences(
				PREFS_NAME, 0);
		final SharedPreferences.Editor editor = settings.edit();
		editor.putInt("AutoUpdateInterval", SLEEP_TIME);
		editor.putString("WebServiveUrl", URL_STRING);
		editor.commit();
	}

	public static void loadSettings(Context context) {
		final SharedPreferences settings = context.getSharedPreferences(
				PREFS_NAME, MODE_WORLD_READABLE );
		URL_STRING = settings.getString("WebServiveUrl", "");
		Log.w(TAG, "The URL String from  the shared prefferences ...." + URL_STRING);
		AutoUpdateInterval = settings.getInt("AutoUpdateInterval", 5);
		// make sure folder exists
		final File dir = new File(UpdateMembersInfoService.savePath);
		boolean hasCreated = dir.mkdirs();
		Log.w(UpdateMembersInfoService.TAG,
				"LoadSettings - We tried creating foler structure  and we got - "
						+ hasCreated);

	}

}
