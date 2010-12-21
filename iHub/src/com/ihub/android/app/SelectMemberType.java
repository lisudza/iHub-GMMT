package com.ihub.android.app;

import com.ihub.android.app.data.IhubDatabaseHelper;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem.Type;

public class SelectMemberType extends GDActivity implements OnClickListener {

	private Button btnGreenMembers, btnRedMembers, btnBlackMembers,
			btnAllMembers;
	private String MEMBER_TYPES_TO_DISPLAY;
	private IhubDatabaseHelper dbHelper;
	private String TAG = "SelectMemberType";
	private String RED_COUNT = "0", GREEN_COUNT = "0", BLACK_COUNT = "0";
	private int ALL_COUNT;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setActionBarContentView(R.layout.select_member_type);
		addActionBarItem(Type.Refresh);
		setTitle("Online members");
		
		dbHelper = new IhubDatabaseHelper(this);
		dbHelper.createDatabase();
		dbHelper.createTable();
		
		btnGreenMembers = (Button) findViewById(R.id.btn_green_members);
		btnRedMembers = (Button) findViewById(R.id.btn_red_members);
		btnBlackMembers = (Button) findViewById(R.id.btn_black_members);
		btnAllMembers = (Button) findViewById(R.id.btn_all_members);
		
		Cursor cursor = dbHelper.getOnlineMembersCount();

		int numRows = cursor.getCount();
		Log.w(TAG, "Number of rows - "+numRows);
		if (numRows >= 1) {
			cursor.moveToFirst();
			int i = 0;
			do {
				if (cursor.getString(1).equals("Green")) {
					GREEN_COUNT = cursor.getString(0);
				}
				if (cursor.getString(1).equals("Red")) {
					RED_COUNT = cursor.getString(0);
				}
				if (cursor.getString(1).equals("Black")) {
					BLACK_COUNT = cursor.getString(0);
				}
				Log.w(TAG, "Value at index "+i+" "+cursor.getString(0)+":: "+cursor.getString(1));
				ALL_COUNT += Integer.parseInt(cursor.getString(0));
				i++;
			} while(cursor.moveToNext());
		} 
		
		cursor.close();
		
		btnGreenMembers.setText("Green Member {"+GREEN_COUNT+"}");
		btnRedMembers.setText("Red Member {"+RED_COUNT+"}");
		btnBlackMembers.setText("Black Member {"+BLACK_COUNT+"}");
		btnAllMembers.setText("All Members {"+ALL_COUNT+"}");
		
		btnGreenMembers.setOnClickListener(this);
		btnRedMembers.setOnClickListener(this);
		btnBlackMembers.setOnClickListener(this);
		btnAllMembers.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_green_members) {
			MEMBER_TYPES_TO_DISPLAY = "Green";
		} else if (v.getId() == R.id.btn_red_members) {
			MEMBER_TYPES_TO_DISPLAY = "Red";
		} else if (v.getId() == R.id.btn_black_members) {
			MEMBER_TYPES_TO_DISPLAY = "Black";
		} else if (v.getId() == R.id.btn_all_members) {
			MEMBER_TYPES_TO_DISPLAY = "All";
		}
		
		Intent intent = new Intent(this, OnlineMembers.class);
		intent.putExtra("MemberType", MEMBER_TYPES_TO_DISPLAY);
		startActivity(intent);
	}
}
