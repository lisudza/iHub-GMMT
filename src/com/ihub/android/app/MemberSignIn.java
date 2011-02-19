package com.ihub.android.app;

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.Item;
import greendroid.widget.item.SubtextItem;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xmlrpc.android.XMLRPCException;

import com.ihub.android.app.data.IhubDatabaseHelper;
import com.ihub.android.app.data.ImageManager;
import com.ihub.android.app.net.FetchUserData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MemberSignIn extends GDListActivity {

	private String TAG = "MemberSignIn";
	private String firstName, lastName, cloudUserID, isAllowed, profilePic,
			profilePicURL, telephone, emailAddress, qrCode, occupation,
			country, memberType;
	private Person person;
	private IhubDatabaseHelper ihubDatabaseHelper;
	private boolean isMemberNew, hasProfileChanged;
	private ImageManager imageManager;
	private TextView txtNames;
	private TextView txtOccupation;
	private ImageView imgProfilePic;
	private Button btn_signin;
	private Button btn_cancel;
	private String RPC_METHOD;
	private HashMap<String, String> params[];
	private FetchUserData fetchUserData;
	private Context context;
	private int duration = Toast.LENGTH_LONG;
	private Toast toast;
	private String toastMessage;
	private ListView member_details_list;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.signin);
		setTitle("Sign In/Out");
		context = getApplicationContext();
		ihubDatabaseHelper = new IhubDatabaseHelper(this);
		ihubDatabaseHelper.createDatabase();
		ihubDatabaseHelper.createTable();

		imageManager = new ImageManager();
		fetchUserData = new FetchUserData();

		Intent intent = getIntent();
		Bundle b = intent.getExtras();

		firstName = b.getString("firstName");
		lastName = b.getString("lastName");
		profilePic = b.getString("profilePic");
		hasProfileChanged = b.getBoolean("hasProfileChanged");
		telephone = b.getString("telephone");
		emailAddress = b.getString("emailAdd");
		isAllowed = b.getString("isAllowed");
		cloudUserID = b.getString("userID");
		occupation = b.getString("occupation");
		profilePicURL = b.getString("profilePicURL");
		country = b.getString("country");
		qrCode = b.getString("qrCode");
		memberType = b.getString("memberType");

		Log.w(TAG, "FirstName - " + firstName);
		Log.w(TAG, "User ID - " + cloudUserID);
		person = new Person();
		person.setFirstName(firstName);
		person.setLastName(lastName);
		person.setEmailAddress(emailAddress);
		person.setProfilePic(profilePic);
		person.setTelephone(telephone);
		person.setCloudUserID(cloudUserID);
		person.setProfilePicURL(profilePicURL);
		person.setQrCode(qrCode);
		person.setOccupation(occupation);
		person.setCountry(country);
		person.setMemberType(memberType);

		isMemberNew = checkIfUserExists();
		Log.w(TAG, "Value of isMemberNew - " + isMemberNew);
		syncUserInfo();

		txtNames = (TextView) findViewById(R.id.txt_name);
		txtOccupation = (TextView) findViewById(R.id.txt_occupation);
		imgProfilePic = (ImageView) findViewById(R.id.profile_pic);
		Log.w(TAG, "NAmes: " + person.getFirstName() + " "
				+ person.getLastName());
		txtNames.setText(person.getFirstName());

		member_details_list = (ListView) findViewById(R.id.member_details_list);
		List<Item> items = new ArrayList<Item>();

		items.add(new SubtextItem("Name ", person.getFirstName()+" "+person.getLastName()));
		items.add(new SubtextItem("Telephone", person.getTelephone()));
		items.add(new SubtextItem("Occupation", person.getOccupation()));
		items.add(new SubtextItem("Email Address", person.getEmailAddress()));
		final ItemAdapter adapter = new ItemAdapter(this, items);
		
		member_details_list.setAdapter(adapter);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 2;
		Bitmap bm = BitmapFactory.decodeFile("/sdcard/ihub/pics/"
				+ person.getProfilePic(), options);

		imgProfilePic.setImageBitmap(bm);
		btn_signin = (Button) findViewById(R.id.btn_signin);
		btn_cancel = (Button) findViewById(R.id.btn_cancel);

		btn_signin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String alternateBtnText = "";
				if (btn_signin.getText().equals("Sign In")) {
					RPC_METHOD = "ihub.signin";
					alternateBtnText = "Sign Out";
				} else {
					RPC_METHOD = "ihub.signout";
					alternateBtnText = "Sign In";
				}

				params = new HashMap[1];
				params[0] = new HashMap<String, String>();
				params[0].put("userID", person.getCloudUserID());

				try {
					fetchUserData.sendDetailsToServer(RPC_METHOD, params);
					btn_signin.setText(alternateBtnText);
				} catch (XMLRPCException e) {
					toastMessage = "Hi " + person.getFirstName() + ", "
							+ btn_signin.getText()
							+ " operation failed. Please try again later.";
					Log
							.w(TAG, "Operation " + btn_signin.getText()
									+ " for user " + person.getFirstName()
									+ " Failed.");
					e.printStackTrace();
					toast = Toast.makeText(context, toastMessage, duration);
					toast.show();
				}
			}
		});	

		btn_cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
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
			Log.w(TAG, "User ID - " + cloudUserID + " " + person.getFirstName()
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
						.getCloudUserID(), person.getMemberType(), person.getTelephone(), person.getEmailAddress());
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
			Log.w(TAG, "User ID - " + cloudUserID + " " + person.getFirstName()
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
						.getMemberType(), person.getTelephone(), person.getEmailAddress());
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
}
