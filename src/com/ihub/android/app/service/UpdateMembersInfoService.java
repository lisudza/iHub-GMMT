package com.ihub.android.app.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.xmlrpc.android.XMLRPCException;

import com.ihub.android.app.Person;
import com.ihub.android.app.data.IhubDatabaseHelper;
import com.ihub.android.app.data.ImageManager;
import com.ihub.android.app.net.FetchUserData;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class UpdateMembersInfoService extends Service {

	private String TAG = "UpdateMembersInfoService";
	private Handler serviceHandler;
	private Task myTask;
	private String RPC_METHOD;
	private HashMap<String, String> params[];
	private FetchUserData fetchUserData;
	private Person person;
	private HashMap<String, Object> results[];
	private HashMap<String, String> result;
	private IhubDatabaseHelper ihubDatabaseHelper;
	private ImageManager imageManager;
	private boolean isMemberNew, hasProfileChanged;

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
		serviceHandler.postDelayed(myTask, 1000L);
		Log.d(getClass().getSimpleName(), "onStart()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		serviceHandler.removeCallbacks(myTask);
		serviceHandler = null;
		Log.w(TAG, "onDestroy() --- My service has just been killed.");
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

	void updateMembersInfo() {
		RPC_METHOD = "ihub.validateuser";
		params = new HashMap[1];
		params[0] = new HashMap<String, String>();
		// params[0].put("qrCode", );
		try {
			results = (HashMap<String, Object>[]) fetchUserData
					.sendDetailsToServer(RPC_METHOD, params);

			for (int i = 0; i < results.length; i++) {
				result = (HashMap) results[i];
				person = new Person();
				person.setFirstName(result.get("firstName"));
				person.setLastName(result.get("lastName"));
				person.setOccupation(result.get("occupation"));
				person.setProfilePic(result.get("profilePic"));
				person.setProfilePicURL(result.get("profilePicURL"));
				person.setQrCode(result.get("qrCode"));
				person.setTelephone(result.get("telephone"));
				person.setCountry(result.get("country"));
				person.setEmailAddress(result.get("emailAddress"));
				person.setCloudUserID(result.get("userID"));
				hasProfileChanged = Boolean.parseBoolean(result
						.get("hasProfileChanged"));

				isMemberNew = checkIfUserExists();

				syncUserInfo();

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
						.getCloudUserID());
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
						.getProfilePic(), person.getOccupation());
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

}
