package com.ihub.android.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.ihub.android.app.data.IhubDatabaseHelper;
import com.ihub.android.app.data.ImageManager;
import com.ihub.android.app.qactions.ActionItem;
import com.ihub.android.app.qactions.QuickAction;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import greendroid.app.GDListActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ItemAdapter;
import greendroid.widget.LoaderActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.item.Item;
import greendroid.widget.item.ThumbnailItem;

public class OnlineMembers extends GDListActivity {

	private ListView list_view;
	private IhubDatabaseHelper ihubDatabaseHelper;
	private String TAG = "OnlineMembers";
	private ImageManager imageManager;
	private final Handler mHandler = new Handler();
	private List<Item> items;
	private Vector listContents;
	private String members_array[] = new String[1];
	private String MEMBER_TYPE_TO_DISPLAY;
	private HashMap selectedUserMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.online_members);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		MEMBER_TYPE_TO_DISPLAY = bundle.getString("MemberType");

		setTitle(MEMBER_TYPE_TO_DISPLAY + " members");

		addActionBarItem(Type.Refresh);
		items = new ArrayList<Item>();
		listContents = new Vector();
		imageManager = new ImageManager();
		ihubDatabaseHelper = new IhubDatabaseHelper(this);
		ihubDatabaseHelper.createDatabase();
		ihubDatabaseHelper.createTable();
		list_view = (ListView) findViewById(R.id.members_list);
		int count = populateList();
		if (count >= 1) {
			SimpleAdapter simpleAdapter = new SimpleAdapter(this, listContents,
					R.layout.my_list, new String[] { "image", "Name",
							"Occupation" }, new int[] { R.id.icon,
							R.id.txt_name, R.id.txt_occupation });
			list_view.setAdapter(simpleAdapter);
		} else {
			members_array[0] = "No members currently online.";
			ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, members_array);
			list_view.setAdapter(adapter1);
			adapter1.notifyDataSetChanged();
		}

		final ActionItem chart = new ActionItem();

		chart.setTitle("Kick");
		chart.setIcon(getResources().getDrawable(R.drawable.chart));
		chart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast
						.makeText(
								OnlineMembers.this,
								"Soon you will be able to kick a user out of the network. :)",
								Toast.LENGTH_LONG).show();
			}
		});

		final ActionItem profile = new ActionItem();

		profile.setTitle("Profile");
		profile.setIcon(getResources().getDrawable(R.drawable.production));
		profile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(OnlineMembers.this, ShowProfile.class);
				intent.putExtra("MemberID", selectedUserMap.get("MemberID").toString());
				intent.putExtra("Name", selectedUserMap.get("Name").toString());
				intent.putExtra("Image", selectedUserMap.get("image").toString());
				intent.putExtra("Occupation", selectedUserMap.get("Occupation").toString());
				startActivity(intent);
			}
		});

		list_view.setOnItemClickListener(new OnItemClickListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Class classType = list_view.getItemAtPosition(position)
						.getClass();
				Log.w(TAG, "Class Type - "+classType.getName());
				if (classType.equals(java.lang.String.class)) {
						Log.w(TAG, "No members currently online so we can't display actions.");
				} else {
					selectedUserMap = (HashMap) list_view
							.getItemAtPosition(position);
					Log.w(TAG, "Hash Mapc contents - " + selectedUserMap);
					QuickAction qa = new QuickAction(view);

					qa.addActionItem(chart);
					qa.addActionItem(profile);
					qa.setAnimStyle(QuickAction.ANIM_AUTO);
					qa.show();

				}
			}
		});

	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (position) {
		case 0:
			final LoaderActionBarItem loaderItem = (LoaderActionBarItem) item;
			mHandler.postDelayed(new Runnable() {
				public void run() {
					loaderItem.setLoading(false);
				}
			}, 2000);
			Toast
					.makeText(
							this,
							"Refreshing list of Online members.. {might take a while.}",
							Toast.LENGTH_LONG).show();
			break;
		default:
			return super.onHandleActionBarItemClick(item, position);
		}
		return true;
	}

	public int populateList() {
		Cursor cursor;
		if (MEMBER_TYPE_TO_DISPLAY.equals("All")) {
			cursor = ihubDatabaseHelper.getAllMembers();
		} else {
			cursor = ihubDatabaseHelper
					.getMembersofType(MEMBER_TYPE_TO_DISPLAY);
		}

		int numRows = cursor.getCount();
		HashMap<String, String> membersMap[] = new HashMap[numRows];
		Log.w(TAG, "Number of members currently in the building - " + numRows);
		if (numRows >= 1) {
			cursor.moveToFirst();
			int i = 0;
			do {
				membersMap[i] = new HashMap<String, String>();
				membersMap[i].put("image", ""
						+ imageManager.getProfilePic("ihub/pics/"
								+ cursor.getString(5)));
				membersMap[i].put("Name", cursor.getString(1) + " "
						+ cursor.getString(2));
				membersMap[i].put("Occupation", cursor.getString(6));
				membersMap[i].put("MemberID", cursor.getString(0));
				listContents.add(membersMap[i]);
				i++;
			} while (cursor.moveToNext());
			cursor.close();
		}
		cursor.close();
		return numRows;
	}
}
