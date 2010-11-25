package com.ihub.android.app;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import com.ihub.android.app.data.IhubDatabaseHelper;
import com.ihub.android.app.data.ImageManager;
import com.ihub.android.app.qactions.ActionItem;
import com.ihub.android.app.qactions.QuickAction;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MembersIn extends Activity {
	private ListView list_view;
	private Vector<HashMap<String, String>> listContents;
	private IhubDatabaseHelper ihubDatabaseHelper;
	private String TAG = "MembersIn";
	private ImageManager imageManager;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.members);
		imageManager = new ImageManager();
		ihubDatabaseHelper = new IhubDatabaseHelper(this);
		ihubDatabaseHelper.createDatabase();
		ihubDatabaseHelper.createTable();
		listContents = new Vector<HashMap<String, String>>();
		populateList();
		list_view = (ListView) findViewById(R.id.list_view);
		SimpleAdapter simpleAdapter = new SimpleAdapter(this, listContents,
				R.layout.my_list,
				new String[] { "image", "Name", "Occupation" }, new int[] {
						R.id.icon, R.id.txt_name, R.id.txt_occupation });
		list_view.setAdapter(simpleAdapter);

		final ActionItem chart = new ActionItem();

		chart.setTitle("Kick");
		chart.setIcon(getResources().getDrawable(R.drawable.chart));
		chart.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast
						.makeText(
								MembersIn.this,
								"Soon you will be able to kick a user out of the network. :)",
								Toast.LENGTH_LONG).show();
			}
		});

		final ActionItem production = new ActionItem();

		production.setTitle("Profile");
		production.setIcon(getResources().getDrawable(R.drawable.production));
		production.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MembersIn.this, "Profile selected",
						Toast.LENGTH_LONG).show();
			}
		});

		list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				QuickAction qa = new QuickAction(view);

				qa.addActionItem(chart);
				qa.addActionItem(production);
				qa.setAnimStyle(QuickAction.ANIM_AUTO);

				qa.show();
			}
		});

	}

	public void populateList() {
		Cursor cursor = ihubDatabaseHelper.getAllMembers();
		int numRows = cursor.getCount();
		HashMap<String, String> membersMap[] = new HashMap[numRows];
		Log.w(TAG, "Number of members currently in the building - " + numRows);
		if (cursor.moveToFirst()) {
			int i = 0;
			do {
				membersMap[i] = new HashMap<String, String>();
				membersMap[i].put("image", ""
						+ imageManager.getProfilePic("ihub/pics/"+cursor.getString(5)));
				membersMap[i].put("Name", cursor.getString(1) + " "
						+ cursor.getString(2));
				membersMap[i].put("Occupation", cursor.getString(6));
				listContents.add(membersMap[i]);
				i++;
			} while (cursor.moveToNext());
			cursor.close();
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}
}
